<?php
include 'conexion.php';

$paciente_id = $_POST['paciente_id'] ?? '';

if (empty($paciente_id)) {
    echo json_encode(["success" => false, "message" => "Falta el paciente"]);
    exit();
}

$sql = $conexion->prepare("SELECT c.id, c.medico_id, c.fecha_cita, c.hora_cita, c.estado, c.motivo,
        u.nombre, u.apellido, e.nombre as especialidad
        FROM citas c
        INNER JOIN medicos m ON c.medico_id = m.id
        INNER JOIN usuarios u ON m.usuario_id = u.id
        INNER JOIN especialidades e ON m.especialidad_id = e.id
        WHERE c.paciente_id = ?
        ORDER BY c.fecha_cita DESC, c.hora_cita DESC");
$sql->bind_param("i", $paciente_id);
$sql->execute();
$resultado = $sql->get_result();

$citas = [];
while ($fila = $resultado->fetch_assoc()) {
    $citas[] = $fila;
}

echo json_encode(["success" => true, "citas" => $citas]);
$conexion->close();
?>