package com.example.saludtotal;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class RecordatorioWorker extends Worker {

    public RecordatorioWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data datos = getInputData();
        String medico = datos.getString("medico");
        String hora = datos.getString("hora");

        NotificacionHelper.enviarNotificacion(getApplicationContext(),
                "⏰ Recordatorio de cita",
                "Mañana tienes cita con " + medico + " a las "
                        + FormatoHelper.hora12(hora != null ? hora : "") + ".");
        return Result.success();
    }
}