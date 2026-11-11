package com.example.saludtotal;

import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvMostrar;
    private RequestQueue requestQueue;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvMostrar = findViewById(R.id.tvMostrar);
        requestQueue = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> login());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, TerminosActivity.class)));

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

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.LOGIN,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONObject usuario = json.getJSONObject("usuario");

                            SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("id", usuario.getInt("id"));
                            editor.putString("nombre", usuario.getString("nombre"));
                            editor.putString("apellido", usuario.getString("apellido"));
                            editor.putString("rol", usuario.getString("rol"));
                            editor.putString("email", usuario.getString("email"));
                            editor.apply();

                            Toast.makeText(this, "Bienvenido " + usuario.getString("nombre"),
                                    Toast.LENGTH_SHORT).show();

                            String rol = usuario.getString("rol");
                            Intent intent;

                            if (rol.equals("paciente")) {
                                intent = new Intent(LoginActivity.this, PacienteActivity.class);
                            } else if (rol.equals("medico")) {
                                intent = new Intent(LoginActivity.this, MedicoActivity.class);
                            } else {
                                intent = new Intent(LoginActivity.this, AdminActivity.class);
                            }

                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al leer respuesta: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String msg = "Error de conexión";
                    if (error.getMessage() != null) {
                        msg = "Error: " + error.getMessage();
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}