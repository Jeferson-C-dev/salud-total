<?php
include 'conexion.php';

$sql = "SELECT * FROM especialidades WHERE activo = 1";
$resultado = $conexion->query($sql);

$especialidades = [];
while ($fila = $resultado->fetch_assoc()) {
    $especialidades[] = $fila;
}

echo json_encode(["success" => true, "especialidades" => $especialidades]);
$conexion->close();
?>