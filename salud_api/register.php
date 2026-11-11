<?php
include 'conexion.php';

$nombre = $_POST['nombre'] ?? '';
$apellido = $_POST['apellido'] ?? '';
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$dni = $_POST['dni'] ?? '';
$telefono = $_POST['telefono'] ?? '';

if (empty($nombre) || empty($apellido) || empty($email) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Complete todos los campos"]);
    exit();
}

// Verificar si el email ya existe
$check = $conexion->prepare("SELECT id FROM usuarios WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
$check->store_result();

if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "El correo ya está registrado"]);
    exit();
}

// Cifrar la contraseña (seguridad)
$password_segura = password_hash($password, PASSWORD_DEFAULT);

// Insertar el nuevo usuario como paciente
$sql = $conexion->prepare("INSERT INTO usuarios (nombre, apellido, email, password, dni, telefono, rol) VALUES (?, ?, ?, ?, ?, ?, 'paciente')");
$sql->bind_param("ssssss", $nombre, $apellido, $email, $password_segura, $dni, $telefono);

if ($sql->execute()) {
    echo json_encode(["success" => true, "message" => "Registro exitoso"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al registrar"]);
}

$conexion->close();
?>