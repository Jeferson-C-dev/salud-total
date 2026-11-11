package com.example.saludtotal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class CambiarPasswordActivity extends AppCompatActivity {

    private EditText etActual, etNueva, etConfirmar;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_password);

        etActual = findViewById(R.id.etActual);
        etNueva = findViewById(R.id.etNueva);
        etConfirmar = findViewById(R.id.etConfirmar);
        Button btnCambiar = findViewById(R.id.btnCambiar);
        ImageView btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());
        btnCambiar.setOnClickListener(v -> cambiar());
    }

    private void cambiar() {
        String actual = etActual.getText().toString();
        String nueva = etNueva.getText().toString();
        String confirmar = etConfirmar.getText().toString();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nueva.length() < 6) {
            etNueva.setError("Mínimo 6 caracteres");
            return;
        }
        if (!nueva.equals(confirmar)) {
            etConfirmar.setError("Las contraseñas no coinciden");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String usuarioId = String.valueOf(prefs.getInt("id", 0));

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.CAMBIAR_PASSWORD,
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
                params.put("usuario_id", usuarioId);
                params.put("actual", actual);
                params.put("nueva", nueva);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}