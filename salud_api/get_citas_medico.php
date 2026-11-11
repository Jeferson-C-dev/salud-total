<?php
include 'conexion.php';

$usuario_id = $_POST['medico_id'] ?? '';

if (empty($usuario_id)) {
    echo json_encode(["success" => false, "message" => "Falta el médico"]);
    exit();
}

// Buscar el medico_id real usando el usuario_id
$sqlMedico = $conexion->prepare("SELECT id FROM medicos WHERE usuario_id = ?");
$sqlMedico->bind_param("i", $usuario_id);
$sqlMedico->execute();
$resMedico = $sqlMedico->get_result();

if ($resMedico->num_rows === 0) {
    // Si no es médico, devolver todas las citas (para el admin)
    $sql = "SELECT c.id, c.fecha_cita, c.hora_cita, c.estado, c.motivo,
            c.paciente_id,
            u.nombre, u.apellido, u.dni
            FROM citas c
            INNER JOIN usuarios u ON c.paciente_id = u.id
            ORDER BY c.fecha_cita DESC, c.hora_cita ASC";
    $resultado = $conexion->query($sql);
} else {
    $medico = $resMedico->fetch_assoc();
    $medico_id = $medico['id'];

    $sql = $conexion->prepare("SELECT c.id, c.fecha_cita, c.hora_cita, c.estado, c.motivo,
            c.paciente_id,
            u.nombre, u.apellido, u.dni
            FROM citas c
            INNER JOIN usuarios u ON c.paciente_id = u.id
            WHERE c.medico_id = ?
            ORDER BY c.fecha_cita DESC, c.hora_cita ASC");
    $sql->bind_param("i", $medico_id);
    $sql->execute();
    $resultado = $sql->get_result();
}

$citas = [];
while ($fila = $resultado->fetch_assoc()) {
    $citas[] = $fila;
}

echo json_encode(["success" => true, "citas" => $citas]);
$conexion->close();
?>