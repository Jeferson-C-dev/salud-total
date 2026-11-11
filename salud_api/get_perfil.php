<?php
include 'conexion.php';

$usuario_id = $_POST['usuario_id'] ?? '';

if (empty($usuario_id)) {
    echo json_encode(["success" => false, "message" => "Falta el usuario"]);
    exit();
}

$sql = $conexion->prepare("SELECT id, nombre, apellido, email, dni, telefono FROM usuarios WHERE id = ?");
$sql->bind_param("i", $usuario_id);
$sql->execute();
$resultado = $sql->get_result();

if ($resultado->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Usuario no encontrado"]);
    exit();
}

$usuario = $resultado->fetch_assoc();
echo json_encode(["success" => true, "usuario" => $usuario]);
$conexion->close();
?>