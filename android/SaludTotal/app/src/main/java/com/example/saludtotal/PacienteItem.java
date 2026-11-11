package com.example.saludtotal;

public class PacienteItem {
    public String id;
    public String nombre;
    public String apellido;
    public String email;
    public String dni;
    public String telefono;
    public int totalCitas;

    public PacienteItem(String id, String nombre, String apellido, String email,
                        String dni, String telefono, int totalCitas) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.dni = dni;
        this.telefono = telefono;
        this.totalCitas = totalCitas;
    }
}