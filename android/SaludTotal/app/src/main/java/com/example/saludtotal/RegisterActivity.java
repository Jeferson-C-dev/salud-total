package com.example.saludtotal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etDni, etTelefono, etPassword;
    private Button btnRegistrar;
    private TextView tvVolver, tvMostrar;
    private RequestQueue requestQueue;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        etDni = findViewById(R.id.etDni);
        etTelefono = findViewById(R.id.etTelefono);
        etPassword = findViewById(R.id.etPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvVolver = findViewById(R.id.tvVolver);
        tvMostrar = findViewById(R.id.tvMostrar);
        requestQueue = Volley.newRequestQueue(this);

        btnRegistrar.setOnClickListener(v -> registrar());
        tvVolver.setOnClickListener(v -> finish());

        // Mostrar / ocultar contraseña
        tvMostrar.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                tvMostrar.setText("Ocultar");
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tvMostrar.setText("Mostrar");
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void validarCampo(EditText campo, String valor) {
        if (valor.isEmpty()) {
            campo.setBackgroundResource(R.drawable.input_box_error);
            campo.setError("Campo requerido");
        } else {
            campo.setBackgroundResource(R.drawable.input_box);
            campo.setError(null);
        }
    }

    // Mueve la firma temporal al archivo del usuario recién registrado
    private void asignarFirmaAlUsuario(String email) {
        String clave = email.toLowerCase().replaceAll("[^a-z0-9]", "_");
        File temp = new File(getFilesDir(), "firma_temp.png");
        File destino = new File(getFilesDir(), "firma_" + clave + ".png");

        SharedPreferences prefs = getSharedPreferences("aceptacion", MODE_PRIVATE);
        String fecha = prefs.getString("fecha_temp", "");

        if (temp.exists()) {
            if (destino.exists()) destino.delete();
            temp.renameTo(destino);
            prefs.edit().putString("fecha_" + clave, fecha).apply();
        }
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dni = etDni.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        validarCampo(etNombre, nombre);
        validarCampo(etApellido, apellido);
        validarCampo(etEmail, email);
        validarCampo(etPassword, password);

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setBackgroundResource(R.drawable.input_box_error);
            etEmail.setError("Correo no válido");
            return;
        }

        if (!dni.isEmpty() && dni.length() != 8) {
            etDni.setBackgroundResource(R.drawable.input_box_error);
            etDni.setError("El DNI debe tener 8 dígitos");
            return;
        } else {
            etDni.setBackgroundResource(R.drawable.input_box);
        }

        if (password.length() < 6) {
            etPassword.setBackgroundResource(R.drawable.input_box_error);
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.REGISTER,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        if (json.getBoolean("success")) {
                            asignarFirmaAlUsuario(email);
                            finish();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error de conexión. Verifica XAMPP.",
                        Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nombre", nombre);
                params.put("apellido", apellido);
                params.put("email", email);
                params.put("dni", dni);
                params.put("telefono", telefono);
                params.put("password", password);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}