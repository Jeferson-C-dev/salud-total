<?php
include 'conexion.php';

$nombre = trim($_POST['nombre'] ?? '');
$apellido = trim($_POST['apellido'] ?? '');
$email = trim($_POST['email'] ?? '');
$dni = trim($_POST['dni'] ?? '');
$telefono = trim($_POST['telefono'] ?? '');
$colegiatura = trim($_POST['colegiatura'] ?? '');
$especialidad_id = $_POST['especialidad_id'] ?? '';
$password = $_POST['password'] ?? '';

if ($nombre === '' || $apellido === '' || $email === '' || $colegiatura === '' ||
    $especialidad_id === '' || $password === '') {
    echo json_encode(["success" => false, "message" => "Complete los campos obligatorios"]);
    exit();
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["success" => false, "message" => "El correo no es válido"]);
    exit();
}

if (strlen($password) < 6) {
    echo json_encode(["success" => false, "message" => "La contraseña debe tener al menos 6 caracteres"]);
    exit();
}

// Correo no repetido
$check = $conexion->prepare("SELECT id FROM usuarios WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
$check->store_result();
if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "El correo ya está registrado"]);
    exit();
}

// Colegiatura no repetida
$checkCol = $conexion->prepare("SELECT id FROM medicos WHERE colegiatura = ?");
$checkCol->bind_param("s", $colegiatura);
$checkCol->execute();
$checkCol->store_result();
if ($checkCol->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Esa colegiatura ya existe"]);
    exit();
}

$conexion->begin_transaction();

try {
    $hash = password_hash($password, PASSWORD_DEFAULT);
    $u = $conexion->prepare("INSERT INTO usuarios (nombre, apellido, email, password, dni, telefono, rol) VALUES (?, ?, ?, ?, ?, ?, 'medico')");
    $u->bind_param("ssssss", $nombre, $apellido, $email, $hash, $dni, $telefono);
    $u->execute();
    $usuario_id = $conexion->insert_id;

    $m = $conexion->prepare("INSERT INTO medicos (usuario_id, especialidad_id, colegiatura, max_citas_dia) VALUES (?, ?, ?, 20)");
    $m->bind_param("iis", $usuario_id, $especialidad_id, $colegiatura);
    $m->execute();

    $conexion->commit();
    echo json_encode(["success" => true, "message" => "Médico registrado correctamente"]);
} catch (Exception $e) {
    $conexion->rollback();
    echo json_encode(["success" => false, "message" => "Error al registrar el médico"]);
}

$conexion->close();
?>