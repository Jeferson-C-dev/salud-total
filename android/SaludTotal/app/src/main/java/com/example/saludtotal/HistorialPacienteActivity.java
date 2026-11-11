package com.example.saludtotal;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialPacienteActivity extends AppCompatActivity {

    private TextView tvNombrePaciente, tvDniPaciente, tvTotalConsultas, tvSinHistorial;
    private RecyclerView recyclerHistorial;
    private ImageView btnRegresar;
    private RequestQueue requestQueue;
    private List<Cita> listaHistorial = new ArrayList<>();
    private CitaAdapter adapter;
    private String pacienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_paciente);

        tvNombrePaciente = findViewById(R.id.tvNombrePaciente);
        tvDniPaciente = findViewById(R.id.tvDniPaciente);
        tvTotalConsultas = findViewById(R.id.tvTotalConsultas);
        tvSinHistorial = findViewById(R.id.tvSinHistorial);
        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        pacienteId = getIntent().getStringExtra("paciente_id");
        String nombrePaciente = getIntent().getStringExtra("nombre_paciente");
        tvNombrePaciente.setText(nombrePaciente != null ? nombrePaciente : "Historial del paciente");

        btnRegresar.setOnClickListener(v -> finish());

        recyclerHistorial.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitaAdapter(listaHistorial, citaId -> {});
        adapter.setSoloLectura(true);
        recyclerHistorial.setAdapter(adapter);

        cargarHistorial();
    }

    private void cargarHistorial() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_HISTORIAL_PACIENTE,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject paciente = json.getJSONObject("paciente");
                            String dni = paciente.optString("dni", "Sin DNI");
                            String telefono = paciente.optString("telefono", "");
                            tvDniPaciente.setText("DNI: " + dni +
                                    (telefono.isEmpty() ? "" : "  ·  Tel: " + telefono));

                            tvTotalConsultas.setText(json.getString("total"));

                            JSONArray arr = json.getJSONArray("historial");
                            listaHistorial.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject c = arr.getJSONObject(i);
                                Cita cita = new Cita(
                                        c.getString("id"),
                                        "Dr(a). " + c.getString("medico_nombre")
                                                + " " + c.getString("medico_apellido"),
                                        c.getString("especialidad"),
                                        c.getString("fecha_cita"),
                                        c.getString("hora_cita").substring(0, 5),
                                        c.getString("estado")
                                );
                                cita.notas = c.optString("notas_medicas", "");
                                listaHistorial.add(cita);
                            }
                            adapter.notifyDataSetChanged();

                            if (listaHistorial.isEmpty()) {
                                tvSinHistorial.setVisibility(View.VISIBLE);
                                recyclerHistorial.setVisibility(View.GONE);
                            } else {
                                tvSinHistorial.setVisibility(View.GONE);
                                recyclerHistorial.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar historial",
                        Toast.LENGTH_SHORT).show()) {
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
}