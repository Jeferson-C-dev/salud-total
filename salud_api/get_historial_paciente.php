<?php
include 'conexion.php';

$paciente_id = $_POST['paciente_id'] ?? '';

if (empty($paciente_id)) {
    echo json_encode(["success" => false, "message" => "Falta el paciente"]);
    exit();
}

$sql = $conexion->prepare("SELECT c.id, c.fecha_cita, c.hora_cita, c.estado, c.motivo,
        u.nombre as medico_nombre, u.apellido as medico_apellido,
        e.nombre as especialidad,
        h.notas_medicas
        FROM citas c
        INNER JOIN medicos m ON c.medico_id = m.id
        INNER JOIN usuarios u ON m.usuario_id = u.id
        INNER JOIN especialidades e ON m.especialidad_id = e.id
        LEFT JOIN historial_atenciones h ON h.cita_id = c.id
        WHERE c.paciente_id = ?
        ORDER BY c.fecha_cita DESC");
$sql->bind_param("i", $paciente_id);
$sql->execute();
$resultado = $sql->get_result();

$historial = [];
while ($fila = $resultado->fetch_assoc()) {
    $historial[] = $fila;
}

$sqlPaciente = $conexion->prepare("SELECT nombre, apellido, dni, telefono FROM usuarios WHERE id = ?");
$sqlPaciente->bind_param("i", $paciente_id);
$sqlPaciente->execute();
$paciente = $sqlPaciente->get_result()->fetch_assoc();

echo json_encode([
    "success" => true,
    "paciente" => $paciente,
    "historial" => $historial,
    "total" => count($historial)
]);

$conexion->close();
?>