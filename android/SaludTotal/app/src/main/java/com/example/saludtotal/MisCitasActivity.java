package com.example.saludtotal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MisCitasActivity extends AppCompatActivity {

    private RecyclerView recyclerCitas;
    private TextView tvSinCitas;
    private ImageView btnRegresar;
    private RequestQueue requestQueue;
    private List<Cita> listaCitas = new ArrayList<>();
    private CitaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_citas);

        recyclerCitas = findViewById(R.id.recyclerCitas);
        tvSinCitas = findViewById(R.id.tvSinCitas);
        btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());

        recyclerCitas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitaAdapter(listaCitas, this::cancelarCita);
        adapter.setOnEliminadaListener(this::cargarCitas);
        adapter.setOnReprogramarListener(this::iniciarReprogramacion);
        recyclerCitas.setAdapter(adapter);

        cargarCitas();
    }

    private void cargarCitas() {
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String pacienteId = String.valueOf(prefs.getInt("id", 0));

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.GET_MIS_CITAS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("citas");
                            listaCitas.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject c = arr.getJSONObject(i);
                                Cita cita = new Cita(
                                        c.getString("id"),
                                        "Dr(a). " + c.getString("nombre") + " " + c.getString("apellido"),
                                        c.getString("especialidad"),
                                        c.getString("fecha_cita"),
                                        c.getString("hora_cita").substring(0, 5),
                                        c.getString("estado")
                                );
                                cita.medicoId = c.optString("medico_id", "");
                                listaCitas.add(cita);
                            }
                            adapter.notifyDataSetChanged();

                            if (listaCitas.isEmpty()) {
                                tvSinCitas.setVisibility(View.VISIBLE);
                                recyclerCitas.setVisibility(View.GONE);
                            } else {
                                tvSinCitas.setVisibility(View.GONE);
                                recyclerCitas.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar citas", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("paciente_id", pacienteId);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void cancelarCita(String citaId) {
        new AlertDialog.Builder(this)
                .setTitle("❌ Cancelar cita")
                .setMessage("¿Estás seguro que deseas cancelar esta cita?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.CANCELAR_CITA,
                            response -> {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    Toast.makeText(this, json.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                    if (json.getBoolean("success")) {
                                        // Al cancelar, se anula el recordatorio
                                        RecordatorioHelper.cancelar(this, citaId);
                                        cargarCitas();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(this, "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(this, "Error de conexión",
                                    Toast.LENGTH_SHORT).show()) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("cita_id", citaId);
                            return params;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
                    requestQueue.add(request);
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ---------- REPROGRAMAR ----------
    private void iniciarReprogramacion(Cita cita) {
        if (cita.medicoId == null || cita.medicoId.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar al médico", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String fecha = year + "-" + String.format("%02d", month + 1)
                    + "-" + String.format("%02d", day);
            cargarHorariosDisponibles(cita, fecha);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void cargarHorariosDisponibles(Cita cita, String fecha) {
        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.GET_HORARIOS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.getBoolean("success")) {
                            Toast.makeText(this, "No se pudieron cargar los horarios",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray arr = json.getJSONArray("horarios");
                        if (arr.length() == 0) {
                            Toast.makeText(this, "No hay horarios disponibles ese día",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        List<String> originales = new ArrayList<>();
                        String[] etiquetas = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            String h = arr.getString(i);
                            originales.add(h);
                            etiquetas[i] = FormatoHelper.hora12(h);
                        }
                        new AlertDialog.Builder(this)
                                .setTitle("Elige el nuevo horario\n" + FormatoHelper.fechaEspanol(fecha))
                                .setItems(etiquetas, (dlg, which) ->
                                        enviarReprogramacion(cita, fecha, originales.get(which)))
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("medico_id", cita.medicoId);
                params.put("fecha", fecha);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void enviarReprogramacion(Cita cita, String fecha, String hora) {
        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.REPROGRAMAR_CITA,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                        if (json.getBoolean("success")) {
                            // Se anula el recordatorio viejo y se programa el nuevo
                            RecordatorioHelper.cancelar(this, cita.id);
                            RecordatorioHelper.programar(this, cita.id, fecha, hora, cita.medico);
                            cargarCitas();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cita_id", cita.id);
                params.put("fecha_cita", fecha);
                params.put("hora_cita", hora);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}