package com.example.saludtotal;

public class ApiConfig {

    private static final String BASE_URL = "http://172.20.10.2/salud_api/";

    public static final String LOGIN = BASE_URL + "login.php";
    public static final String REGISTER = BASE_URL + "register.php";
    public static final String GET_ESPECIALIDADES = BASE_URL + "get_especialidades.php";
    public static final String GET_MEDICOS = BASE_URL + "get_medicos.php";
    public static final String GET_HORARIOS = BASE_URL + "get_horarios.php";
    public static final String RESERVAR_CITA = BASE_URL + "reservar_cita.php";
    public static final String GET_MIS_CITAS = BASE_URL + "get_mis_citas.php";
    public static final String CANCELAR_CITA = BASE_URL + "cancelar_cita.php";
    public static final String GET_CITAS_MEDICO = BASE_URL + "get_citas_medico.php";
    public static final String GET_ESTADISTICAS = BASE_URL + "get_estadisticas.php";
    public static final String GET_ESTADISTICAS_MEDICO = BASE_URL + "get_estadisticas_medico.php";
    public static final String CONFIRMAR_CITA = BASE_URL + "confirmar_cita.php";
    public static final String GET_PERFIL = BASE_URL + "get_perfil.php";
    public static final String ACTUALIZAR_PERFIL = BASE_URL + "actualizar_perfil.php";
    public static final String GET_TODAS_CITAS = BASE_URL + "get_todas_citas.php";
    public static final String GET_MEDICOS_ADMIN = BASE_URL + "get_medicos_admin.php";
    public static final String TOGGLE_MEDICO = BASE_URL + "toggle_medico.php";
    public static final String GET_HISTORIAL_PACIENTE = BASE_URL + "get_historial_paciente.php";
    public static final String ELIMINAR_CITA = BASE_URL + "eliminar_cita.php";
    public static final String GET_PACIENTES = BASE_URL + "get_pacientes.php";
    public static final String GET_REPORTE = BASE_URL + "get_reporte.php";
    public static final String CAMBIAR_PASSWORD = BASE_URL + "cambiar_password.php";
    public static final String REPROGRAMAR_CITA = BASE_URL + "reprogramar_cita.php";
    public static final String AGREGAR_MEDICO = BASE_URL + "agregar_medico.php";
}