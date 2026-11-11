<?php
include 'conexion.php';

$sql = "SELECT c.id, c.fecha_cita, c.hora_cita, c.estado, c.motivo,
        u.nombre as paciente_nombre, u.apellido as paciente_apellido,
        um.nombre as medico_nombre, um.apellido as medico_apellido,
        e.nombre as especialidad
        FROM citas c
        INNER JOIN usuarios u ON c.paciente_id = u.id
        INNER JOIN medicos m ON c.medico_id = m.id
        INNER JOIN usuarios um ON m.usuario_id = um.id
        INNER JOIN especialidades e ON m.especialidad_id = e.id
        ORDER BY c.fecha_cita DESC, c.hora_cita ASC";

$resultado = $conexion->query($sql);
$citas = [];
while ($fila = $resultado->fetch_assoc()) {
    $citas[] = $fila;
}

echo json_encode(["success" => true, "citas" => $citas]);
$conexion->close();
?>