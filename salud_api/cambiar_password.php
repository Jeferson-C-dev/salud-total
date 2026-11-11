<?php
include 'conexion.php';

$usuario_id = $_POST['usuario_id'] ?? '';
$actual = $_POST['actual'] ?? '';
$nueva = $_POST['nueva'] ?? '';

if (empty($usuario_id) || empty($actual) || empty($nueva)) {
    echo json_encode(["success" => false, "message" => "Complete todos los campos"]);
    exit();
}

if (strlen($nueva) < 6) {
    echo json_encode(["success" => false, "message" => "La nueva contraseña debe tener al menos 6 caracteres"]);
    exit();
}

$q = $conexion->prepare("SELECT password FROM usuarios WHERE id = ?");
$q->bind_param("i", $usuario_id);
$q->execute();
$fila = $q->get_result()->fetch_assoc();

if (!$fila || !password_verify($actual, $fila['password'])) {
    echo json_encode(["success" => false, "message" => "La contraseña actual es incorrecta"]);
    exit();
}

$hash = password_hash($nueva, PASSWORD_DEFAULT);
$u = $conexion->prepare("UPDATE usuarios SET password = ? WHERE id = ?");
$u->bind_param("si", $hash, $usuario_id);

if ($u->execute()) {
    echo json_encode(["success" => true, "message" => "Contraseña actualizada"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al actualizar"]);
}

$conexion->close();
?>