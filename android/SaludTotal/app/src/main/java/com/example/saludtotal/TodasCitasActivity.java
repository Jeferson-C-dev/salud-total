package com.example.saludtotal;

import android.app.AlertDialog;
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

public class TodasCitasActivity extends AppCompatActivity {

    private RecyclerView recyclerTodasCitas;
    private TextView tvSinCitas;
    private ImageView btnRegresar;
    private RequestQueue requestQueue;
    private List<Cita> listaCitas = new ArrayList<>();
    private CitaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todas_citas);

        recyclerTodasCitas = findViewById(R.id.recyclerTodasCitas);
        tvSinCitas = findViewById(R.id.tvSinCitas);
        btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());

        recyclerTodasCitas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitaAdapter(listaCitas, this::cancelarCita);
        // Al eliminar una cita, recargar la lista
        adapter.setOnEliminadaListener(this::cargarTodasCitas);
        recyclerTodasCitas.setAdapter(adapter);

        cargarTodasCitas();
    }

    private void cargarTodasCitas() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_TODAS_CITAS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("citas");
                            listaCitas.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject c = arr.getJSONObject(i);
                                listaCitas.add(new Cita(
                                        c.getString("id"),
                                        c.getString("paciente_nombre") + " "
                                                + c.getString("paciente_apellido"),
                                        "Dr(a). " + c.getString("medico_nombre")
                                                + " · " + c.getString("especialidad"),
                                        c.getString("fecha_cita"),
                                        c.getString("hora_cita").substring(0, 5),
                                        c.getString("estado")
                                ));
                            }
                            adapter.notifyDataSetChanged();

                            if (listaCitas.isEmpty()) {
                                tvSinCitas.setVisibility(View.VISIBLE);
                                recyclerTodasCitas.setVisibility(View.GONE);
                            } else {
                                tvSinCitas.setVisibility(View.GONE);
                                recyclerTodasCitas.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar citas",
                        Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void cancelarCita(String citaId) {
        new AlertDialog.Builder(this)
                .setTitle("❌ Cancelar cita")
                .setMessage("¿Seguro que deseas cancelar esta cita?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    StringRequest request = new StringRequest(Request.Method.POST,
                            ApiConfig.CANCELAR_CITA,
                            response -> {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    Toast.makeText(this, json.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                    if (json.getBoolean("success")) {
                                        cargarTodasCitas();
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
}