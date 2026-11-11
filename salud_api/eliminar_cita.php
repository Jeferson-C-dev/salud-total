<?php
include 'conexion.php';

$cita_id = $_POST['cita_id'] ?? '';

if (empty($cita_id)) {
    echo json_encode(["success" => false, "message" => "Falta la cita"]);
    exit();
}

// Solo se pueden eliminar citas canceladas o con inasistencia
$sql = $conexion->prepare("DELETE FROM citas WHERE id = ? AND estado IN ('cancelada','inasistencia')");
$sql->bind_param("i", $cita_id);

if ($sql->execute() && $sql->affected_rows > 0) {
    echo json_encode(["success" => true, "message" => "Cita eliminada"]);
} else {
    echo json_encode(["success" => false, "message" => "Solo se pueden eliminar citas canceladas"]);
}

$conexion->close();
?>