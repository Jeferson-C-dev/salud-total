-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3307
-- Tiempo de generación: 22-10-2026 a las 17:57:59
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `salud_total`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `citas`
--

CREATE TABLE `citas` (
  `id` int(11) NOT NULL,
  `paciente_id` int(11) NOT NULL,
  `medico_id` int(11) NOT NULL,
  `fecha_cita` date NOT NULL,
  `hora_cita` time NOT NULL,
  `estado` enum('pendiente','confirmada','cancelada','inasistencia','completada') DEFAULT 'pendiente',
  `motivo` text DEFAULT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `citas`
--

INSERT INTO `citas` (`id`, `paciente_id`, `medico_id`, `fecha_cita`, `hora_cita`, `estado`, `motivo`, `fecha_registro`) VALUES
(1, 22, 1, '2026-10-23', '15:00:00', 'completada', '', '2026-10-22 03:53:52');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `especialidades`
--

CREATE TABLE `especialidades` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `especialidades`
--

INSERT INTO `especialidades` (`id`, `nombre`, `descripcion`, `activo`) VALUES
(1, 'Medicina General', 'Atención médica general y preventiva', 1),
(2, 'Pediatría', 'Atención médica para niños', 1),
(3, 'Ginecología', 'Salud de la mujer', 1),
(4, 'Nutrición', 'Asesoría nutricional y dietética', 1),
(5, 'Cardiología', 'Atención del corazón y sistema cardiovascular', 1),
(6, 'Dermatología', 'Cuidado de la piel, cabello y uñas', 1),
(7, 'Neurología', 'Atención del sistema nervioso', 1),
(8, 'Oftalmología', 'Salud visual y enfermedades de los ojos', 1),
(9, 'Traumatología', 'Lesiones del sistema óseo y muscular', 1),
(10, 'Psicología', 'Salud mental y bienestar emocional', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `historial_atenciones`
--

CREATE TABLE `historial_atenciones` (
  `id` int(11) NOT NULL,
  `cita_id` int(11) NOT NULL,
  `paciente_id` int(11) NOT NULL,
  `medico_id` int(11) NOT NULL,
  `notas_medicas` text DEFAULT NULL,
  `fecha_atencion` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `historial_atenciones`
--

INSERT INTO `historial_atenciones` (`id`, `cita_id`, `paciente_id`, `medico_id`, `notas_medicas`, `fecha_atencion`) VALUES
(1, 1, 22, 1, 'mejoro', '2026-10-22 03:55:59');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `horarios`
--

CREATE TABLE `horarios` (
  `id` int(11) NOT NULL,
  `medico_id` int(11) NOT NULL,
  `dia_semana` enum('Lunes','Martes','Miercoles','Jueves','Viernes','Sabado') NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fin` time NOT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `medicos`
--

CREATE TABLE `medicos` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `especialidad_id` int(11) NOT NULL,
  `colegiatura` varchar(20) DEFAULT NULL,
  `max_citas_dia` int(11) DEFAULT 20,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `medicos`
--

INSERT INTO `medicos` (`id`, `usuario_id`, `especialidad_id`, `colegiatura`, `max_citas_dia`, `activo`) VALUES
(1, 3, 1, 'CMP12345', 20, 1),
(2, 4, 2, 'CMP12346', 20, 1),
(3, 5, 3, 'CMP12347', 20, 1),
(4, 6, 4, 'CMP12348', 20, 1),
(5, 7, 5, 'CMP12349', 20, 1),
(6, 8, 6, 'CMP12350', 20, 1),
(7, 9, 7, 'CMP12351', 20, 1),
(8, 10, 8, 'CMP12352', 20, 1),
(9, 11, 9, 'CMP12353', 20, 1),
(10, 12, 10, 'CMP12354', 20, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `dni` varchar(8) DEFAULT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `rol` enum('paciente','medico','admin') NOT NULL DEFAULT 'paciente',
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `apellido`, `email`, `password`, `dni`, `telefono`, `rol`, `fecha_registro`) VALUES
(1, 'Admin', 'Sistema', 'admin@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '00000000', '999999999', 'admin', '2026-10-05 00:44:19'),
(2, 'Juan', 'Perez', 'juan@test.com', '$2y$10$9CHZrtjqh95BlMQh.inM2u.Mv57YB0hXNh5hN3zbHOj0.V79Ln1ka', '12345678', '987654321', 'paciente', '2026-10-05 00:51:35'),
(3, 'Carmen', 'Rojas', 'carmen.rojas@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000001', '987000001', 'medico', '2026-10-05 00:56:19'),
(4, 'Luis', 'Mendoza', 'luis.mendoza@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000002', '987000002', 'medico', '2026-10-05 00:56:19'),
(5, 'Ana', 'Torres', 'ana.torres@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000003', '987000003', 'medico', '2026-10-05 00:56:19'),
(6, 'Rosa', 'Vargas Paredes', 'rosa.vargas@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000004', '987000004', 'medico', '2026-10-20 00:59:54'),
(7, 'Carlos', 'Huerta Quispe', 'carlos.huerta@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000005', '987000005', 'medico', '2026-10-20 00:59:54'),
(8, 'Maria', 'Flores Ramos', 'maria.flores@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000006', '987000006', 'medico', '2026-10-20 00:59:54'),
(9, 'Jorge', 'Castillo Vega', 'jorge.castillo@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000007', '987000007', 'medico', '2026-10-20 00:59:54'),
(10, 'Sofia', 'Ramirez Cruz', 'sofia.ramirez@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000008', '987000008', 'medico', '2026-10-20 00:59:54'),
(11, 'Diego', 'Morales Leon', 'diego.morales@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000009', '987000009', 'medico', '2026-10-20 00:59:54'),
(12, 'Lucia', 'Torres Meza', 'lucia.torres@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '20000010', '987000010', 'medico', '2026-10-20 00:59:54'),
(14, 'Patricia', 'Asenjo Silva', 'patricia@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '12345679', '987654322', 'paciente', '2026-10-20 01:00:59'),
(15, 'Michael', 'Julca Bonilla', 'michael@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '12345680', '987654323', 'paciente', '2026-10-20 01:00:59'),
(16, 'Juan', 'Quispe Echeverre', 'juan@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '12345681', '987654324', 'paciente', '2026-10-20 01:00:59'),
(17, 'Robert', 'Ulloa Salazar', 'robert@saludtotal.com', '$2y$10$E3or1T7.ZYZOd.EIJObe.OJJFwXhgC5W2hadkOUT.PX94jSTZnjNm', '12345682', '987654325', 'paciente', '2026-10-20 01:00:59'),
(18, 'Denzel', 'Camillo Fonseca', 'Denzel@saludtotal.com', '$2y$10$RgWOwaYZrf1sZkpJ/t2TIu9AWyU5ddrCAOdbCSxnFWABQcokdOtgu', '72841213', '918172953', 'paciente', '2026-10-20 02:11:34'),
(22, 'Jeferson', 'Coacalla Coaquira', 'jeferson@saludtotal.com', '$2y$10$C2B58rn8jh9K6mZXNaeImOacmvD2cBvDPWIUshSDUi0PtetzJKgLO', '75852508', '993513364', 'paciente', '2026-10-22 03:32:22');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `citas`
--
ALTER TABLE `citas`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_fecha` (`fecha_cita`),
  ADD KEY `idx_medico` (`medico_id`),
  ADD KEY `idx_paciente` (`paciente_id`);

--
-- Indices de la tabla `especialidades`
--
ALTER TABLE `especialidades`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `historial_atenciones`
--
ALTER TABLE `historial_atenciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `cita_id` (`cita_id`);

--
-- Indices de la tabla `horarios`
--
ALTER TABLE `horarios`
  ADD PRIMARY KEY (`id`),
  ADD KEY `medico_id` (`medico_id`);

--
-- Indices de la tabla `medicos`
--
ALTER TABLE `medicos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`),
  ADD KEY `especialidad_id` (`especialidad_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `citas`
--
ALTER TABLE `citas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `especialidades`
--
ALTER TABLE `especialidades`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `historial_atenciones`
--
ALTER TABLE `historial_atenciones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `horarios`
--
ALTER TABLE `horarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `medicos`
--
ALTER TABLE `medicos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `citas`
--
ALTER TABLE `citas`
  ADD CONSTRAINT `citas_ibfk_1` FOREIGN KEY (`paciente_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `citas_ibfk_2` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`);

--
-- Filtros para la tabla `historial_atenciones`
--
ALTER TABLE `historial_atenciones`
  ADD CONSTRAINT `historial_atenciones_ibfk_1` FOREIGN KEY (`cita_id`) REFERENCES `citas` (`id`);

--
-- Filtros para la tabla `horarios`
--
ALTER TABLE `horarios`
  ADD CONSTRAINT `horarios_ibfk_1` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `medicos`
--
ALTER TABLE `medicos`
  ADD CONSTRAINT `medicos_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `medicos_ibfk_2` FOREIGN KEY (`especialidad_id`) REFERENCES `especialidades` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
