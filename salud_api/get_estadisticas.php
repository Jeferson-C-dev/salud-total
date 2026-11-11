<?php
include 'conexion.php';

// Total de citas
$total = $conexion->query("SELECT COUNT(*) as total FROM citas")->fetch_assoc()['total'];

// Pendientes
$pendientes = $conexion->query("SELECT COUNT(*) as total FROM citas WHERE estado = 'pendiente'")->fetch_assoc()['total'];

// Confirmadas
$confirmadas = $conexion->query("SELECT COUNT(*) as total FROM citas WHERE estado = 'confirmada'")->fetch_assoc()['total'];

// Canceladas
$canceladas = $conexion->query("SELECT COUNT(*) as total FROM citas WHERE estado = 'cancelada'")->fetch_assoc()['total'];

// Total de pacientes
$pacientes = $conexion->query("SELECT COUNT(*) as total FROM usuarios WHERE rol = 'paciente'")->fetch_assoc()['total'];

// Total de médicos
$medicos = $conexion->query("SELECT COUNT(*) as total FROM usuarios WHERE rol = 'medico'")->fetch_assoc()['total'];

echo json_encode([
    "success" => true,
    "total" => $total,
    "pendientes" => $pendientes,
    "confirmadas" => $confirmadas,
    "canceladas" => $canceladas,
    "pacientes" => $pacientes,
    "medicos" => $medicos
]);

$conexion->close();
?>