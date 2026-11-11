<?php
include 'conexion.php';

$medico_id = $_POST['medico_id'] ?? '';
$activo = $_POST['activo'] ?? '1';

if (empty($medico_id)) {
    echo json_encode(["success" => false, "message" => "Falta el médico"]);
    exit();
}

$sql = $conexion->prepare("UPDATE medicos SET activo = ? WHERE id = ?");
$sql->bind_param("ii", $activo, $medico_id);

if ($sql->execute()) {
    $msg = $activo == '1' ? "Médico activado" : "Médico desactivado";
    echo json_encode(["success" => true, "message" => $msg]);
} else {
    echo json_encode(["success" => false, "message" => "Error al actualizar"]);
}

$conexion->close();
?>