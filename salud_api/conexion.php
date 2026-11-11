<?php
$host = "127.0.0.1";
$port = "3307";
$usuario = "root";
$password = "";
$base_datos = "salud_total";

$conexion = new mysqli($host, $usuario, $password, $base_datos, $port);

if ($conexion->connect_error) {
    die(json_encode([
        "success" => false,
        "message" => "Error de conexión: " . $conexion->connect_error
    ]));
}

$conexion->set_charset("utf8mb4");

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
?>