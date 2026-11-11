package com.example.saludtotal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class AdminActivity extends AppCompatActivity {

    private TextView tvNombreAdmin, tvTotalCitas, tvPendientes,
            tvConfirmadas, tvCanceladas, tvPacientes, tvMedicos,
            tvCambiarPassword, tvMiPerfil;
    private LinearLayout cardTodasCitas, cardGestionarMedicos, cardUbicacion,
            cardPacientes, cardReporte;
    private Button btnCerrarSesion;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        tvNombreAdmin = findViewById(R.id.tvNombreAdmin);
        tvTotalCitas = findViewById(R.id.tvTotalCitas);
        tvPendientes = findViewById(R.id.tvPendientes);
        tvConfirmadas = findViewById(R.id.tvConfirmadas);
        tvCanceladas = findViewById(R.id.tvCanceladas);
        tvPacientes = findViewById(R.id.tvPacientes);
        tvMedicos = findViewById(R.id.tvMedicos);
        tvCambiarPassword = findViewById(R.id.tvCambiarPassword);
        tvMiPerfil = findViewById(R.id.tvMiPerfil);
        cardTodasCitas = findViewById(R.id.cardTodasCitas);
        cardGestionarMedicos = findViewById(R.id.cardGestionarMedicos);
        cardUbicacion = findViewById(R.id.cardUbicacion);
        cardPacientes = findViewById(R.id.cardPacientes);
        cardReporte = findViewById(R.id.cardReporte);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);

        cardTodasCitas.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, TodasCitasActivity.class))
        );

        cardGestionarMedicos.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, GestionarMedicosActivity.class))
        );

        cardPacientes.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, PacientesActivity.class))
        );

        cardReporte.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, ReporteActivity.class))
        );

        tvCambiarPassword.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, CambiarPasswordActivity.class))
        );

        tvMiPerfil.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, PerfilActivity.class))
        );

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
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Recarga nombre y estadísticas al volver (por ejemplo desde Mi perfil)
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", "");
        String apellido = prefs.getString("apellido", "");
        tvNombreAdmin.setText(nombre + " " + apellido);
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_ESTADISTICAS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            tvTotalCitas.setText(json.getString("total"));
                            tvPendientes.setText(json.getString("pendientes"));
                            tvConfirmadas.setText(json.getString("confirmadas"));
                            tvCanceladas.setText(json.getString("canceladas"));
                            tvPacientes.setText(json.getString("pacientes"));
                            tvMedicos.setText(json.getString("medicos"));
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar estadísticas",
                        Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}