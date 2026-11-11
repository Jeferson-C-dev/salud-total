package com.example.saludtotal;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

public class AgregarMedicoActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etDni, etTelefono, etColegiatura, etPassword;
    private Spinner spinnerEspecialidad;
    private RequestQueue requestQueue;
    private List<String> especialidadIds = new ArrayList<>();
    private List<String> especialidadNombres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_medico);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        etDni = findViewById(R.id.etDni);
        etTelefono = findViewById(R.id.etTelefono);
        etColegiatura = findViewById(R.id.etColegiatura);
        etPassword = findViewById(R.id.etPassword);
        spinnerEspecialidad = findViewById(R.id.spinnerEspecialidad);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        ImageView btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardar());

        cargarEspecialidades();
    }

    private void cargarEspecialidades() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_ESPECIALIDADES,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("especialidades");
                            especialidadIds.clear();
                            especialidadNombres.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject e = arr.getJSONObject(i);
                                especialidadIds.add(e.getString("id"));
                                especialidadNombres.add(e.getString("nombre"));
                            }
                            spinnerEspecialidad.setAdapter(new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    especialidadNombres));
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar especialidades",
                        Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String colegiatura = etColegiatura.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()
                || colegiatura.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (especialidadIds.isEmpty() || spinnerEspecialidad.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Seleccione una especialidad", Toast.LENGTH_SHORT).show();
            return;
        }
        String especialidadId = especialidadIds.get(spinnerEspecialidad.getSelectedItemPosition());

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.AGREGAR_MEDICO,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"),
                                Toast.LENGTH_LONG).show();
                        if (json.getBoolean("success")) {
                            finish();
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
                params.put("nombre", nombre);
                params.put("apellido", apellido);
                params.put("email", email);
                params.put("dni", dni);
                params.put("telefono", telefono);
                params.put("colegiatura", colegiatura);
                params.put("especialidad_id", especialidadId);
                params.put("password", password);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}