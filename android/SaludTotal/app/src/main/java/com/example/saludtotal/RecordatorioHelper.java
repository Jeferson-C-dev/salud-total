package com.example.saludtotal;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecordatorioHelper {

    // true = el recordatorio llega en 1 minuto (solo para probar)
    // false = llega 24 horas antes de la cita (uso real)
    public static final boolean MODO_PRUEBA = false;

    private static String etiqueta(String citaId) {
        return "cita_" + citaId;
    }

    // Programa el recordatorio 24 horas antes de la cita
    public static void programar(Context context, String citaId, String fecha,
                                 String hora, String medico) {
        try {
            String horaCorta = hora.length() >= 5 ? hora.substring(0, 5) : hora;
            Date momento = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .parse(fecha + " " + horaCorta);
            if (momento == null) return;

            long retraso = MODO_PRUEBA
                    ? TimeUnit.MINUTES.toMillis(1)
                    : momento.getTime() - TimeUnit.HOURS.toMillis(24) - System.currentTimeMillis();

            // Si la cita es dentro de menos de 24 horas, no hay recordatorio
            if (retraso <= 0) return;

            Data datos = new Data.Builder()
                    .putString("medico", medico)
                    .putString("hora", horaCorta)
                    .build();

            OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(RecordatorioWorker.class)
                    .setInitialDelay(retraso, TimeUnit.MILLISECONDS)
                    .setInputData(datos)
                    .addTag(etiqueta(citaId))
                    .build();

            WorkManager.getInstance(context).enqueue(trabajo);
        } catch (Exception ignored) {
        }
    }

    // Cancela el recordatorio (al cancelar o reprogramar la cita)
    public static void cancelar(Context context, String citaId) {
        WorkManager.getInstance(context).cancelAllWorkByTag(etiqueta(citaId));
    }
}