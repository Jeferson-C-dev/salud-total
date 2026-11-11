<?php
include 'conexion.php';

$usuario_id = $_POST['usuario_id'] ?? '';

if (empty($usuario_id)) {
    echo json_encode(["success" => false, "message" => "Falta el usuario"]);
    exit();
}

$sqlMedico = $conexion->prepare("SELECT id FROM medicos WHERE usuario_id = ?");
$sqlMedico->bind_param("i", $usuario_id);
$sqlMedico->execute();
$resMedico = $sqlMedico->get_result();

if ($resMedico->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "No es médico"]);
    exit();
}

$medico = $resMedico->fetch_assoc();
$medico_id = $medico['id'];

$hoy = date('Y-m-d');
$hoy_count = $conexion->query("SELECT COUNT(*) as t FROM citas WHERE medico_id = $medico_id AND fecha_cita = '$hoy'")->fetch_assoc()['t'];
$pendientes = $conexion->query("SELECT COUNT(*) as t FROM citas WHERE medico_id = $medico_id AND estado = 'pendiente'")->fetch_assoc()['t'];
$completadas = $conexion->query("SELECT COUNT(*) as t FROM citas WHERE medico_id = $medico_id AND estado = 'completada'")->fetch_assoc()['t'];
$canceladas = $conexion->query("SELECT COUNT(*) as t FROM citas WHERE medico_id = $medico_id AND estado = 'cancelada'")->fetch_assoc()['t'];
$total = $conexion->query("SELECT COUNT(*) as t FROM citas WHERE medico_id = $medico_id")->fetch_assoc()['t'];

echo json_encode([
    "success" => true,
    "hoy" => $hoy_count,
    "pendientes" => $pendientes,
    "completadas" => $completadas,
    "canceladas" => $canceladas,
    "total" => $total
]);

$conexion->close();
?>