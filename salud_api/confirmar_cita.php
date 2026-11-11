<?php
include 'conexion.php';

$cita_id = $_POST['cita_id'] ?? '';
$estado = $_POST['estado'] ?? 'confirmada';
$notas = trim($_POST['notas'] ?? '');

$permitidos = ['confirmada', 'completada', 'cancelada', 'inasistencia'];

if (empty($cita_id) || !in_array($estado, $permitidos)) {
    echo json_encode(["success" => false, "message" => "Datos inválidos"]);
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

// Solo se puede completar o marcar inasistencia en una cita confirmada
if (($estado === 'completada' || $estado === 'inasistencia') && $cita['estado'] !== 'confirmada') {
    echo json_encode(["success" => false, "message" => "La cita debe estar confirmada"]);
    exit();
}

$sql = $conexion->prepare("UPDATE citas SET estado = ? WHERE id = ?");
$sql->bind_param("si", $estado, $cita_id);

if (!$sql->execute()) {
    echo json_encode(["success" => false, "message" => "Error al actualizar"]);
    exit();
}

if ($estado === 'completada') {
    $ins = $conexion->prepare("INSERT INTO historial_atenciones (cita_id, paciente_id, medico_id, notas_medicas) VALUES (?, ?, ?, ?)");
    $ins->bind_param("iiis", $cita_id, $cita['paciente_id'], $cita['medico_id'], $notas);
    $ins->execute();
}

$mensaje = $estado === 'inasistencia'
    ? "Cita registrada como inasistencia"
    : "Cita actualizada correctamente";

echo json_encode(["success" => true, "message" => $mensaje]);
$conexion->close();
?>