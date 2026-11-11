<?php
include 'conexion.php';
date_default_timezone_set('America/Lima');

$cita_id = $_POST['cita_id'] ?? '';

if (empty($cita_id)) {
    echo json_encode(["success" => false, "message" => "Falta la cita"]);
    exit();
}

// Buscar la cita
$sqlCita = $conexion->prepare("SELECT fecha_cita, hora_cita FROM citas WHERE id = ?");
$sqlCita->bind_param("i", $cita_id);
$sqlCita->execute();
$cita = $sqlCita->get_result()->fetch_assoc();

if (!$cita) {
    echo json_encode(["success" => false, "message" => "Cita no encontrada"]);
    exit();
}

// RN-005: cancelar con al menos 1 hora de anticipación; si no, es inasistencia
$momentoCita = strtotime($cita['fecha_cita'] . ' ' . $cita['hora_cita']);
$conAnticipacion = ($momentoCita - time()) >= 3600;

$nuevoEstado = $conAnticipacion ? 'cancelada' : 'inasistencia';

$sql = $conexion->prepare("UPDATE citas SET estado = ? WHERE id = ?");
$sql->bind_param("si", $nuevoEstado, $cita_id);

if ($sql->execute()) {
    $mensaje = $conAnticipacion
        ? "Cita cancelada"
        : "Cancelación tardía: la cita se registró como inasistencia";
    echo json_encode(["success" => true, "message" => $mensaje]);
} else {
    echo json_encode(["success" => false, "message" => "Error al cancelar"]);
}

$conexion->close();
?>