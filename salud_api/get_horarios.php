<?php
include 'conexion.php';

$medico_id = $_POST['medico_id'] ?? '';
$fecha = $_POST['fecha'] ?? '';

if (empty($medico_id) || empty($fecha)) {
    echo json_encode(["success" => false, "message" => "Faltan datos"]);
    exit();
}

// Horarios base del consultorio (8am a 6pm cada hora)
$horarios_base = ['08:00','09:00','10:00','11:00','12:00','14:00','15:00','16:00','17:00'];

// Buscar horarios ya ocupados
$sql = $conexion->prepare("SELECT hora_cita FROM citas WHERE medico_id = ? AND fecha_cita = ? AND estado != 'cancelada'");
$sql->bind_param("is", $medico_id, $fecha);
$sql->execute();
$resultado = $sql->get_result();

$ocupados = [];
while ($fila = $resultado->fetch_assoc()) {
    $ocupados[] = substr($fila['hora_cita'], 0, 5);
}

// Filtrar los disponibles
$disponibles = array_values(array_diff($horarios_base, $ocupados));

echo json_encode(["success" => true, "horarios" => $disponibles]);
$conexion->close();
?>