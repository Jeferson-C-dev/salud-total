package com.example.saludtotal;

public class Cita {
    public String id;
    public String medico;
    public String especialidad;
    public String fecha;
    public String hora;
    public String estado;
    public String pacienteId;
    public String medicoId = "";
    public String notas = "";

    public Cita(String id, String medico, String especialidad,
                String fecha, String hora, String estado) {
        this.id = id;
        this.medico = medico;
        this.especialidad = especialidad;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.pacienteId = "";
    }

    public Cita(String id, String medico, String especialidad,
                String fecha, String hora, String estado, String pacienteId) {
        this.id = id;
        this.medico = medico;
        this.especialidad = especialidad;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.pacienteId = pacienteId;
    }
}