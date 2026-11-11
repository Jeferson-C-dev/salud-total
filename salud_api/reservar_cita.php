<?php
include 'conexion.php';
date_default_timezone_set('America/Lima');

$paciente_id = $_POST['paciente_id'] ?? '';
$medico_id = $_POST['medico_id'] ?? '';
$fecha_cita = $_POST['fecha_cita'] ?? '';
$hora_cita = $_POST['hora_cita'] ?? '';
$motivo = $_POST['motivo'] ?? '';

if (empty($paciente_id) || empty($medico_id) || empty($fecha_cita) || empty($hora_cita)) {
    echo json_encode(["success" => false, "message" => "Complete todos los campos"]);
    exit();
}

// RN-002: reservar con mínimo 2 horas de anticipación
$momentoCita = strtotime($fecha_cita . ' ' . $hora_cita);
if ($momentoCita === false || ($momentoCita - time()) < 2 * 3600) {
    echo json_encode(["success" => false, "message" => "La cita debe reservarse con al menos 2 horas de anticipación"]);
    exit();
}

// RN-001: no dos citas el mismo día con el mismo médico
$check = $conexion->prepare("SELECT id FROM citas WHERE paciente_id = ? AND medico_id = ? AND fecha_cita = ? AND estado NOT IN ('cancelada','inasistencia')");
$check->bind_param("iis", $paciente_id, $medico_id, $fecha_cita);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Ya tiene una cita ese día con este médico"]);
    exit();
}

// RN-004: máximo de citas por día según el médico (por defecto 20)
$sqlMax = $conexion->prepare("SELECT max_citas_dia FROM medicos WHERE id = ?");
$sqlMax->bind_param("i", $medico_id);
$sqlMax->execute();
$filaMax = $sqlMax->get_result()->fetch_assoc();
$maxCitas = $filaMax ? intval($filaMax['max_citas_dia']) : 20;

$sqlCount = $conexion->prepare("SELECT COUNT(*) AS total FROM citas WHERE medico_id = ? AND fecha_cita = ? AND estado NOT IN ('cancelada','inasistencia')");
$sqlCount->bind_param("is", $medico_id, $fecha_cita);
$sqlCount->execute();
$totalDia = $sqlCount->get_result()->fetch_assoc()['total'];

if ($totalDia >= $maxCitas) {
    echo json_encode(["success" => false, "message" => "El médico alcanzó el máximo de citas para ese día"]);
    exit();
}

// Horario no ocupado
$check2 = $conexion->prepare("SELECT id FROM citas WHERE medico_id = ? AND fecha_cita = ? AND hora_cita = ? AND estado NOT IN ('cancelada','inasistencia')");
$check2->bind_param("iss", $medico_id, $fecha_cita, $hora_cita);
$check2->execute();
$check2->store_result();

if ($check2->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Ese horario ya está ocupado"]);
    exit();
}

// Registrar la cita
$sql = $conexion->prepare("INSERT INTO citas (paciente_id, medico_id, fecha_cita, hora_cita, motivo, estado) VALUES (?, ?, ?, ?, ?, 'pendiente')");
$sql->bind_param("iisss", $paciente_id, $medico_id, $fecha_cita, $hora_cita, $motivo);

if ($sql->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Cita reservada correctamente",
        "cita_id" => $conexion->insert_id
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Error al reservar la cita"]);
}

$conexion->close();
?>