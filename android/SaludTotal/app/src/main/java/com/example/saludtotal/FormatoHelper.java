package com.example.saludtotal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatoHelper {

    // "2026-10-22" → "22 de octubre de 2026"
    public static String fechaEspanol(String fecha) {
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = entrada.parse(fecha);
            SimpleDateFormat salida = new SimpleDateFormat("d 'de' MMMM 'de' yyyy",
                    new Locale("es", "PE"));
            return salida.format(date);
        } catch (ParseException e) {
            return fecha;
        }
    }

    // "08:00" → "8:00 AM"
    public static String hora12(String hora) {
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = entrada.parse(hora);
            SimpleDateFormat salida = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return salida.format(date);
        } catch (ParseException e) {
            return hora;
        }
    }

    // Combina fecha y hora bonito
    public static String fechaHora(String fecha, String hora) {
        return fechaEspanol(fecha) + "  ·  " + hora12(hora);
    }
}