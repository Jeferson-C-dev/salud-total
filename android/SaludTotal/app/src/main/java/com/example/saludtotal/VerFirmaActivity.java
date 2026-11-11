package com.example.saludtotal;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class VerFirmaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_firma);

        ImageView btnRegresar = findViewById(R.id.btnRegresar);
        ImageView imgFirma = findViewById(R.id.imgFirma);
        TextView tvFecha = findViewById(R.id.tvFechaAceptacion);
        TextView tvSinFirma = findViewById(R.id.tvSinFirma);

        btnRegresar.setOnClickListener(v -> finish());

        SharedPreferences sesion = getSharedPreferences("sesion", MODE_PRIVATE);
        String email = sesion.getString("email", "");
        String clave = email.toLowerCase().replaceAll("[^a-z0-9]", "_");

        File archivo = new File(getFilesDir(), "firma_" + clave + ".png");
        SharedPreferences aceptacion = getSharedPreferences("aceptacion", MODE_PRIVATE);
        String fecha = aceptacion.getString("fecha_" + clave, "");

        if (!email.isEmpty() && archivo.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(archivo.getAbsolutePath());
            imgFirma.setImageBitmap(bitmap);
            tvFecha.setText("Aceptado el: " + fecha);
        } else {
            tvSinFirma.setVisibility(View.VISIBLE);
            tvFecha.setText("Sin firma registrada");
        }
    }
}