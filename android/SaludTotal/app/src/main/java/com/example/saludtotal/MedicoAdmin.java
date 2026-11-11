package com.example.saludtotal;

public class MedicoAdmin {
    public String medicoId;
    public String nombre;
    public String apellido;
    public String especialidad;
    public String colegiatura;
    public boolean activo;

    public MedicoAdmin(String medicoId, String nombre, String apellido,
                       String especialidad, String colegiatura, boolean activo) {
        this.medicoId = medicoId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.especialidad = especialidad;
        this.colegiatura = colegiatura;
        this.activo = activo;
    }
}