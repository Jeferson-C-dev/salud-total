<?php
include 'conexion.php';

$sql = "SELECT u.nombre, u.apellido, e.nombre AS especialidad,
        COUNT(c.id) AS total,
        SUM(CASE WHEN c.estado = 'cancelada' THEN 1 ELSE 0 END) AS canceladas,
        SUM(CASE WHEN c.estado = 'inasistencia' THEN 1 ELSE 0 END) AS inasistencias,
        SUM(CASE WHEN c.estado = 'completada' THEN 1 ELSE 0 END) AS completadas
        FROM medicos m
        INNER JOIN usuarios u ON m.usuario_id = u.id
        INNER JOIN especialidades e ON m.especialidad_id = e.id
        LEFT JOIN citas c ON c.medico_id = m.id
        GROUP BY m.id, u.nombre, u.apellido, e.nombre
        ORDER BY total DESC, u.apellido";

$resultado = $conexion->query($sql);
$reporte = [];
while ($fila = $resultado->fetch_assoc()) {
    $reporte[] = $fila;
}

echo json_encode(["success" => true, "reporte" => $reporte]);
$conexion->close();
?>