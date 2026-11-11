package com.example.saludtotal;

import android.content.Intent;
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
import java.util.List;

public class PacientesActivity extends AppCompatActivity {

    private RecyclerView recyclerPacientes;
    private TextView tvSinPacientes;
    private RequestQueue requestQueue;
    private List<PacienteItem> lista = new ArrayList<>();
    private PacienteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacientes);

        recyclerPacientes = findViewById(R.id.recyclerPacientes);
        tvSinPacientes = findViewById(R.id.tvSinPacientes);
        ImageView btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());

        recyclerPacientes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PacienteAdapter(lista, paciente -> {
            Intent intent = new Intent(PacientesActivity.this, HistorialPacienteActivity.class);
            intent.putExtra("paciente_id", paciente.id);
            intent.putExtra("nombre_paciente", paciente.nombre + " " + paciente.apellido);
            startActivity(intent);
        });
        recyclerPacientes.setAdapter(adapter);

        cargarPacientes();
    }

    private void cargarPacientes() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_PACIENTES,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("pacientes");
                            lista.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject p = arr.getJSONObject(i);
                                lista.add(new PacienteItem(
                                        p.getString("id"),
                                        p.getString("nombre"),
                                        p.getString("apellido"),
                                        p.getString("email"),
                                        p.optString("dni", ""),
                                        p.optString("telefono", ""),
                                        p.getInt("total_citas")
                                ));
                            }
                            adapter.notifyDataSetChanged();

                            if (lista.isEmpty()) {
                                tvSinPacientes.setVisibility(View.VISIBLE);
                                recyclerPacientes.setVisibility(View.GONE);
                            } else {
                                tvSinPacientes.setVisibility(View.GONE);
                                recyclerPacientes.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar pacientes",
                        Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}