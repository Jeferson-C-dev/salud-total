<?php
include 'conexion.php';

$sql = "SELECT m.id as medico_id, m.colegiatura, m.activo,
        u.nombre, u.apellido, u.email, u.telefono,
        e.nombre as especialidad
        FROM medicos m
        INNER JOIN usuarios u ON m.usuario_id = u.id
        INNER JOIN especialidades e ON m.especialidad_id = e.id
        ORDER BY e.nombre, u.apellido";

$resultado = $conexion->query($sql);
$medicos = [];
while ($fila = $resultado->fetch_assoc()) {
    $medicos[] = $fila;
}

echo json_encode(["success" => true, "medicos" => $medicos]);
$conexion->close();
?>