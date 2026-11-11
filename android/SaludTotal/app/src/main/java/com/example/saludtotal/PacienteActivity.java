package com.example.saludtotal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PacienteActivity extends AppCompatActivity {

    private TextView tvNombrePaciente, tvBadgeCitas, tvOds12, tvIniciales, tvActivas, tvHojas;
    private LinearLayout cardReservar, cardMisCitas, cardPerfil, cardUbicacion;
    private Button btnCerrarSesion;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paciente);

        tvNombrePaciente = findViewById(R.id.tvNombrePaciente);
        tvBadgeCitas = findViewById(R.id.tvBadgeCitas);
        tvOds12 = findViewById(R.id.tvOds12);
        tvIniciales = findViewById(R.id.tvIniciales);
        tvActivas = findViewById(R.id.tvActivas);
        tvHojas = findViewById(R.id.tvHojas);
        cardReservar = findViewById(R.id.cardReservar);
        cardMisCitas = findViewById(R.id.cardMisCitas);
        cardPerfil = findViewById(R.id.cardPerfil);
        cardUbicacion = findViewById(R.id.cardUbicacion);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "");
        String apellido = prefs.getString("apellido", "");
        tvNombrePaciente.setText(nombre + " " + apellido);

        if (!nombre.isEmpty() && !apellido.isEmpty()) {
            tvIniciales.setText(nombre.substring(0, 1).toUpperCase()
                    + apellido.substring(0, 1).toUpperCase());
        }

        cardReservar.setOnClickListener(v ->
                startActivity(new Intent(PacienteActivity.this, ReservarCitaActivity.class))
        );

        cardMisCitas.setOnClickListener(v ->
                startActivity(new Intent(PacienteActivity.this, MisCitasActivity.class))
        );

        cardPerfil.setOnClickListener(v ->
                startActivity(new Intent(PacienteActivity.this, PerfilActivity.class))
        );

        // Mantener presionado "Mi perfil" abre la firma de aceptación
        cardPerfil.setOnLongClickListener(v -> {
            startActivity(new Intent(PacienteActivity.this, VerFirmaActivity.class));
            return true;
        });

        cardUbicacion.setOnClickListener(v -> {
            String direccion = "Consultorio Médico Salud Total, Lima, Perú";
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(direccion));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Uri webUri = Uri.parse("https://maps.google.com/?q=" + Uri.encode(direccion));
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(PacienteActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCitas(getSharedPreferences("sesion", MODE_PRIVATE));
    }

    private void cargarCitas(SharedPreferences prefs) {
        String pacienteId = String.valueOf(prefs.getInt("id", 0));

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_MIS_CITAS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("citas");
                            int activas = 0;
                            for (int i = 0; i < arr.length(); i++) {
                                String estado = arr.getJSONObject(i).getString("estado");
                                if (estado.equals("pendiente") || estado.equals("confirmada")) {
                                    activas++;
                                }
                            }

                            tvActivas.setText(String.valueOf(activas));

                            if (activas > 0) {
                                tvBadgeCitas.setText(String.valueOf(activas));
                                tvBadgeCitas.setVisibility(View.VISIBLE);
                            } else {
                                tvBadgeCitas.setVisibility(View.GONE);
                            }

                            // ODS 12: cada cita digital evita una hoja en papel
                            int hojas = arr.length();
                            tvHojas.setText(String.valueOf(hojas));
                            tvOds12.setText("Con tus citas digitales evitaste "
                                    + hojas + (hojas == 1 ? " hoja" : " hojas"));
                        }
                    } catch (Exception e) {
                        tvBadgeCitas.setVisibility(View.GONE);
                    }
                },
                error -> tvBadgeCitas.setVisibility(View.GONE)) {
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