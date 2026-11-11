<?php
include 'conexion.php';

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Complete todos los campos"]);
    exit();
}

// Buscar el usuario por email
$sql = $conexion->prepare("SELECT id, nombre, apellido, email, password, rol FROM usuarios WHERE email = ?");
$sql->bind_param("s", $email);
$sql->execute();
$resultado = $sql->get_result();

if ($resultado->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Usuario no encontrado"]);
    exit();
}

$usuario = $resultado->fetch_assoc();

// Verificar la contraseña cifrada
if (password_verify($password, $usuario['password'])) {
    echo json_encode([
        "success" => true,
        "message" => "Bienvenido",
        "usuario" => [
            "id" => $usuario['id'],
            "nombre" => $usuario['nombre'],
            "apellido" => $usuario['apellido'],
            "email" => $usuario['email'],
            "rol" => $usuario['rol']
        ]
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Contraseña incorrecta"]);
}

$conexion->close();
?>