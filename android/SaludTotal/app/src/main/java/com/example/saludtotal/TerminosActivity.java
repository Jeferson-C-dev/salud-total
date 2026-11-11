package com.example.saludtotal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TerminosActivity extends AppCompatActivity {

    private CheckBox checkAceptar;
    private Button btnAceptar;
    private ImageView btnRegresar;
    private TextView tvLimpiarFirma;
    private FirmaView firmaView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos);

        checkAceptar = findViewById(R.id.checkAceptar);
        btnAceptar = findViewById(R.id.btnAceptar);
        btnRegresar = findViewById(R.id.btnRegresar);
        tvLimpiarFirma = findViewById(R.id.tvLimpiarFirma);
        firmaView = findViewById(R.id.firmaView);

        btnRegresar.setOnClickListener(v -> finish());

        checkAceptar.setOnCheckedChangeListener((buttonView, isChecked) -> actualizarBoton());
        firmaView.setOnFirmaListener(this::actualizarBoton);
        tvLimpiarFirma.setOnClickListener(v -> firmaView.limpiar());

        btnAceptar.setOnClickListener(v -> {
            if (guardarFirmaTemporal()) {
                startActivity(new Intent(TerminosActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }

    private void actualizarBoton() {
        boolean listo = checkAceptar.isChecked() && firmaView.estaFirmado();
        btnAceptar.setEnabled(listo);
        btnAceptar.setAlpha(listo ? 1.0f : 0.5f);
    }

    // La firma queda temporal hasta que el registro termine; ahí se asigna al usuario
    private boolean guardarFirmaTemporal() {
        try {
            Bitmap bitmap = firmaView.obtenerBitmap();
            File archivo = new File(getFilesDir(), "firma_temp.png");
            FileOutputStream salida = new FileOutputStream(archivo);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, salida);
            salida.close();

            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()).format(new Date());
            SharedPreferences prefs = getSharedPreferences("aceptacion", MODE_PRIVATE);
            prefs.edit().putString("fecha_temp", fecha).apply();
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo guardar la firma: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }
}