<?php
include 'conexion.php';
date_default_timezone_set('America/Lima');

$cita_id = $_POST['cita_id'] ?? '';
$fecha = $_POST['fecha_cita'] ?? '';
$hora = $_POST['hora_cita'] ?? '';

if (empty($cita_id) || empty($fecha) || empty($hora)) {
    echo json_encode(["success" => false, "message" => "Complete todos los campos"]);
    exit();
}

$q = $conexion->prepare("SELECT paciente_id, medico_id, estado FROM citas WHERE id = ?");
$q->bind_param("i", $cita_id);
$q->execute();
$cita = $q->get_result()->fetch_assoc();

if (!$cita) {
    echo json_encode(["success" => false, "message" => "Cita no encontrada"]);
    exit();
}

if (!in_array($cita['estado'], ['pendiente', 'confirmada'])) {
    echo json_encode(["success" => false, "message" => "Solo se pueden reprogramar citas pendientes o confirmadas"]);
    exit();
}

$paciente_id = $cita['paciente_id'];
$medico_id = $cita['medico_id'];

// RN-002: mínimo 2 horas de anticipación
$momento = strtotime($fecha . ' ' . $hora);
if ($momento === false || ($momento - time()) < 2 * 3600) {
    echo json_encode(["success" => false, "message" => "La cita debe reprogramarse con al menos 2 horas de anticipación"]);
    exit();
}

// RN-001: una cita por día con el mismo médico (sin contar esta misma)
$c1 = $conexion->prepare("SELECT id FROM citas WHERE paciente_id = ? AND medico_id = ? AND fecha_cita = ? AND id <> ? AND estado NOT IN ('cancelada','inasistencia')");
$c1->bind_param("iisi", $paciente_id, $medico_id, $fecha, $cita_id);
$c1->execute();
$c1->store_result();
if ($c1->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Ya tiene otra cita ese día con este médico"]);
    exit();
}

// RN-004: máximo de citas por día
$m = $conexion->prepare("SELECT max_citas_dia FROM medicos WHERE id = ?");
$m->bind_param("i", $medico_id);
$m->execute();
$fm = $m->get_result()->fetch_assoc();
$max = $fm ? intval($fm['max_citas_dia']) : 20;

$c2 = $conexion->prepare("SELECT COUNT(*) AS total FROM citas WHERE medico_id = ? AND fecha_cita = ? AND id <> ? AND estado NOT IN ('cancelada','inasistencia')");
$c2->bind_param("isi", $medico_id, $fecha, $cita_id);
$c2->execute();
$total = $c2->get_result()->fetch_assoc()['total'];
if ($total >= $max) {
    echo json_encode(["success" => false, "message" => "El médico alcanzó el máximo de citas para ese día"]);
    exit();
}

// Horario libre
$c3 = $conexion->prepare("SELECT id FROM citas WHERE medico_id = ? AND fecha_cita = ? AND hora_cita = ? AND id <> ? AND estado NOT IN ('cancelada','inasistencia')");
$c3->bind_param("issi", $medico_id, $fecha, $hora, $cita_id);
$c3->execute();
$c3->store_result();
if ($c3->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Ese horario ya está ocupado"]);
    exit();
}

// Reprogramar: vuelve a pendiente para que el médico la confirme
$u = $conexion->prepare("UPDATE citas SET fecha_cita = ?, hora_cita = ?, estado = 'pendiente' WHERE id = ?");
$u->bind_param("ssi", $fecha, $hora, $cita_id);

if ($u->execute()) {
    echo json_encode(["success" => true, "message" => "Cita reprogramada. Queda pendiente de confirmación"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al reprogramar"]);
}

$conexion->close();
?>