package com.example.saludtotal;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicoActivity extends AppCompatActivity {

    private TextView tvNombreMedico, tvSinCitas, tvCambiarPassword, tvMiPerfil;
    private TextView tvTotalCitas, tvPendientes, tvCompletadas;
    private RecyclerView recyclerCitasMedico;
    private Button btnCerrarSesion;
    private RequestQueue requestQueue;
    private List<Cita> listaCitas = new ArrayList<>();
    private CitaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico);

        tvNombreMedico = findViewById(R.id.tvNombreMedico);
        tvSinCitas = findViewById(R.id.tvSinCitas);
        tvCambiarPassword = findViewById(R.id.tvCambiarPassword);
        tvMiPerfil = findViewById(R.id.tvMiPerfil);
        tvTotalCitas = findViewById(R.id.tvTotalCitas);
        tvPendientes = findViewById(R.id.tvPendientes);
        tvCompletadas = findViewById(R.id.tvCompletadas);
        recyclerCitasMedico = findViewById(R.id.recyclerCitasMedico);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);

        tvCambiarPassword.setOnClickListener(v ->
                startActivity(new Intent(MedicoActivity.this, CambiarPasswordActivity.class)));
        tvMiPerfil.setOnClickListener(v ->
                startActivity(new Intent(MedicoActivity.this, PerfilActivity.class)));

        recyclerCitasMedico.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitaAdapter(listaCitas,
                citaId -> confirmarOCancelar(citaId, "cancelada"),
                citaId -> confirmarOCancelar(citaId, "confirmada"),
                citaId -> pedirNotas(citaId),
                citaId -> confirmarOCancelar(citaId, "inasistencia")
        );
        recyclerCitasMedico.setAdapter(adapter);

        adapter.setOnEliminadaListener(() -> cargarCitas());

        recyclerCitasMedico.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerCitasMedico,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if (position >= 0 && position < listaCitas.size()) {
                                    Cita cita = listaCitas.get(position);
                                    Intent intent = new Intent(MedicoActivity.this,
                                            HistorialPacienteActivity.class);
                                    intent.putExtra("paciente_id", cita.pacienteId);
                                    intent.putExtra("nombre_paciente", cita.medico);
                                    startActivity(intent);
                                }
                            }
                            @Override
                            public void onLongItemClick(View view, int position) {}
                        })
        );

        btnCerrarSesion.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(MedicoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Recarga nombre y citas al volver (por ejemplo desde Mi perfil)
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "");
        String apellido = prefs.getString("apellido", "");
        tvNombreMedico.setText("Dr(a). " + nombre + " " + apellido);
        cargarCitas();
    }

    private void cargarCitas() {
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String medicoId = String.valueOf(prefs.getInt("id", 0));

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_CITAS_MEDICO,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("citas");
                            listaCitas.clear();
                            int pendientes = 0;
                            int completadas = 0;

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject c = arr.getJSONObject(i);
                                Cita cita = new Cita(
                                        c.getString("id"),
                                        c.getString("nombre") + " " + c.getString("apellido"),
                                        "DNI: " + c.getString("dni"),
                                        c.getString("fecha_cita"),
                                        c.getString("hora_cita").substring(0, 5),
                                        c.getString("estado"),
                                        c.optString("paciente_id", "0")
                                );
                                listaCitas.add(cita);
                                if (c.getString("estado").equals("pendiente")) pendientes++;
                                if (c.getString("estado").equals("completada")) completadas++;
                            }

                            tvTotalCitas.setText(String.valueOf(arr.length()));
                            tvPendientes.setText(String.valueOf(pendientes));
                            tvCompletadas.setText(String.valueOf(completadas));

                            adapter.notifyDataSetChanged();

                            if (listaCitas.isEmpty()) {
                                tvSinCitas.setVisibility(View.VISIBLE);
                                recyclerCitasMedico.setVisibility(View.GONE);
                            } else {
                                tvSinCitas.setVisibility(View.GONE);
                                recyclerCitasMedico.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar citas",
                        Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("medico_id", medicoId);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void pedirNotas(String citaId) {
        EditText etNotas = new EditText(this);
        etNotas.setHint("Diagnóstico, indicaciones, observaciones...");
        etNotas.setMinLines(3);
        etNotas.setGravity(Gravity.TOP);

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout contenedor = new FrameLayout(this);
        contenedor.setPadding(pad, pad / 2, pad, 0);
        contenedor.addView(etNotas);

        new AlertDialog.Builder(this)
                .setTitle("✔️ Completar cita")
                .setMessage("Escribe las notas médicas (opcional):")
                .setView(contenedor)
                .setPositiveButton("Completar", (dialog, which) ->
                        enviarEstado(citaId, "completada", etNotas.getText().toString().trim()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarOCancelar(String citaId, String nuevoEstado) {
        String titulo;
        String mensaje;
        if (nuevoEstado.equals("confirmada")) {
            titulo = "✅ Confirmar cita";
            mensaje = "¿Confirmar esta cita?";
        } else if (nuevoEstado.equals("inasistencia")) {
            titulo = "🚫 Paciente no asistió";
            mensaje = "¿Registrar que el paciente no asistió a esta cita?";
        } else {
            titulo = "❌ Cancelar cita";
            mensaje = "¿Cancelar esta cita?";
        }

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> enviarEstado(citaId, nuevoEstado, ""))
                .setNegativeButton("No", null)
                .show();
    }

    private void enviarEstado(String citaId, String nuevoEstado, String notas) {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.CONFIRMAR_CITA,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"),
                                Toast.LENGTH_SHORT).show();
                        if (json.getBoolean("success")) {
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
                params.put("estado", nuevoEstado);
                params.put("notas", notas);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}