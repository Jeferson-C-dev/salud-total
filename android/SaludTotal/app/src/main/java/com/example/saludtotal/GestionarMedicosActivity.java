package com.example.saludtotal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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

public class GestionarMedicosActivity extends AppCompatActivity {

    private RecyclerView recyclerMedicos;
    private RequestQueue requestQueue;
    private List<MedicoAdmin> listaMedicos = new ArrayList<>();
    private MedicoAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_medicos);

        recyclerMedicos = findViewById(R.id.recyclerMedicos);
        ImageView btnRegresar = findViewById(R.id.btnRegresar);
        Button btnAgregarMedico = findViewById(R.id.btnAgregarMedico);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());
        btnAgregarMedico.setOnClickListener(v ->
                startActivity(new Intent(GestionarMedicosActivity.this,
                        AgregarMedicoActivity.class)));

        recyclerMedicos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicoAdminAdapter(listaMedicos, this::toggleMedico);
        recyclerMedicos.setAdapter(adapter);
    }

    // Recarga la lista al volver de "Agregar médico"
    @Override
    protected void onResume() {
        super.onResume();
        cargarMedicos();
    }

    private void cargarMedicos() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_MEDICOS_ADMIN,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("medicos");
                            listaMedicos.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject m = arr.getJSONObject(i);
                                listaMedicos.add(new MedicoAdmin(
                                        m.getString("medico_id"),
                                        m.getString("nombre"),
                                        m.getString("apellido"),
                                        m.getString("especialidad"),
                                        m.getString("colegiatura"),
                                        m.getInt("activo") == 1
                                ));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar médicos",
                        Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void toggleMedico(String medicoId, boolean activo) {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.TOGGLE_MEDICO,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"),
                                Toast.LENGTH_SHORT).show();
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
                params.put("medico_id", medicoId);
                params.put("activo", activo ? "1" : "0");
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}