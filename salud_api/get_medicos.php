<?php
include 'conexion.php';

$especialidad_id = $_POST['especialidad_id'] ?? '';

$sql = "SELECT m.id as medico_id, u.nombre, u.apellido, e.nombre as especialidad, m.colegiatura
        FROM medicos m
        INNER JOIN usuarios u ON m.usuario_id = u.id
        INNER JOIN especialidades e ON m.especialidad_id = e.id
        WHERE m.activo = 1";

if (!empty($especialidad_id)) {
    $sql .= " AND m.especialidad_id = " . intval($especialidad_id);
}

$resultado = $conexion->query($sql);

$medicos = [];
while ($fila = $resultado->fetch_assoc()) {
    $medicos[] = $fila;
}

echo json_encode(["success" => true, "medicos" => $medicos]);
$conexion->close();
?>