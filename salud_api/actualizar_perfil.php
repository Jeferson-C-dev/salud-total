<?php
include 'conexion.php';

$usuario_id = $_POST['usuario_id'] ?? '';
$nombre = $_POST['nombre'] ?? '';
$apellido = $_POST['apellido'] ?? '';
$dni = $_POST['dni'] ?? '';
$telefono = $_POST['telefono'] ?? '';

if (empty($usuario_id) || empty($nombre) || empty($apellido)) {
    echo json_encode(["success" => false, "message" => "Complete los campos obligatorios"]);
    exit();
}

$sql = $conexion->prepare("UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?, telefono = ? WHERE id = ?");
$sql->bind_param("ssssi", $nombre, $apellido, $dni, $telefono, $usuario_id);

if ($sql->execute()) {
    echo json_encode(["success" => true, "message" => "Perfil actualizado correctamente"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al actualizar"]);
}

$conexion->close();
?>