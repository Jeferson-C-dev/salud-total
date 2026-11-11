package com.example.saludtotal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private TextView tvIniciales, tvNombreCompleto, tvEmailPerfil, tvCambiarPassword, tvRol;
    private EditText etNombre, etApellido, etDni, etTelefono;
    private Button btnGuardar;
    private ImageView btnRegresar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tvIniciales = findViewById(R.id.tvIniciales);
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvEmailPerfil = findViewById(R.id.tvEmailPerfil);
        tvCambiarPassword = findViewById(R.id.tvCambiarPassword);
        tvRol = findViewById(R.id.tvRol);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etDni = findViewById(R.id.etDni);
        etTelefono = findViewById(R.id.etTelefono);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        // Etiqueta del rol según la sesión
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String rol = prefs.getString("rol", "paciente");
        if (rol.equals("medico")) {
            tvRol.setText("Médico");
        } else if (rol.equals("admin")) {
            tvRol.setText("Administrador");
        } else {
            tvRol.setText("Paciente");
        }

        btnRegresar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarPerfil());
        tvCambiarPassword.setOnClickListener(v ->
                startActivity(new Intent(PerfilActivity.this, CambiarPasswordActivity.class)));

        cargarPerfil();
    }

    private void cargarPerfil() {
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuarioId = String.valueOf(prefs.getInt("id", 0));

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_PERFIL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject usuario = json.getJSONObject("usuario");

                            String nombre = usuario.getString("nombre");
                            String apellido = usuario.getString("apellido");
                            String email = usuario.getString("email");
                            String dni = usuario.optString("dni", "");
                            String telefono = usuario.optString("telefono", "");
                            if (dni.equals("null")) dni = "";
                            if (telefono.equals("null")) telefono = "";

                            String iniciales = nombre.substring(0, 1).toUpperCase() +
                                    apellido.substring(0, 1).toUpperCase();
                            tvIniciales.setText(iniciales);
                            tvNombreCompleto.setText(nombre + " " + apellido);
                            tvEmailPerfil.setText(email);

                            etNombre.setText(nombre);
                            etApellido.setText(apellido);
                            etDni.setText(dni);
                            etTelefono.setText(telefono);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar perfil",
                        Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("usuario_id", usuarioId);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void guardarPerfil() {
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuarioId = String.valueOf(prefs.getInt("id", 0));

        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(this, "Nombre y apellido son obligatorios",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dni.isEmpty() && dni.length() != 8) {
            etDni.setError("El DNI debe tener 8 dígitos");
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.ACTUALIZAR_PERFIL,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"),
                                Toast.LENGTH_SHORT).show();
                        if (json.getBoolean("success")) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("nombre", nombre);
                            editor.putString("apellido", apellido);
                            editor.apply();

                            String iniciales = nombre.substring(0, 1).toUpperCase() +
                                    apellido.substring(0, 1).toUpperCase();
                            tvIniciales.setText(iniciales);
                            tvNombreCompleto.setText(nombre + " " + apellido);
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
                params.put("usuario_id", usuarioId);
                params.put("nombre", nombre);
                params.put("apellido", apellido);
                params.put("dni", dni);
                params.put("telefono", telefono);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}