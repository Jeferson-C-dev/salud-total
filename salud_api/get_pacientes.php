<?php
include 'conexion.php';

$sql = "SELECT u.id, u.nombre, u.apellido, u.email, u.dni, u.telefono,
        (SELECT COUNT(*) FROM citas c WHERE c.paciente_id = u.id) AS total_citas
        FROM usuarios u
        WHERE u.rol = 'paciente'
        ORDER BY u.apellido, u.nombre";

$resultado = $conexion->query($sql);
$pacientes = [];
while ($fila = $resultado->fetch_assoc()) {
    $pacientes[] = $fila;
}

echo json_encode(["success" => true, "pacientes" => $pacientes]);
$conexion->close();
?>