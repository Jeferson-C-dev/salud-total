package com.example.saludtotal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.List;

public class ReporteActivity extends AppCompatActivity {

    private static class Fila {
        String medico, especialidad;
        int total, canceladas, inasistencias, completadas;
    }

    private final List<Fila> filas = new ArrayList<>();
    private RecyclerView.Adapter<FilaHolder> adapter;
    private TextView tvResumen;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        ImageView btnRegresar = findViewById(R.id.btnRegresar);
        tvResumen = findViewById(R.id.tvResumen);
        RecyclerView recycler = findViewById(R.id.recyclerReporte);
        requestQueue = Volley.newRequestQueue(this);

        btnRegresar.setOnClickListener(v -> finish());

        adapter = new RecyclerView.Adapter<FilaHolder>() {
            @NonNull
            @Override
            public FilaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_reporte, parent, false);
                return new FilaHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull FilaHolder h, int position) {
                Fila f = filas.get(position);
                h.tvMedico.setText("Dr(a). " + f.medico);
                h.tvEspecialidad.setText(f.especialidad);
                h.tvDetalle.setText("Total " + f.total + " · Completadas " + f.completadas
                        + " · Canceladas " + f.canceladas
                        + " · Inasistencias " + f.inasistencias);

                int sinAtencion = f.canceladas + f.inasistencias;
                int pct = f.total == 0 ? 0 : Math.round(sinAtencion * 100f / f.total);
                h.barra.setProgress(pct);
                h.tvPorcentaje.setText(pct + "% sin atención");
            }

            @Override
            public int getItemCount() {
                return filas.size();
            }
        };

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        cargarReporte();
    }

    private void cargarReporte() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_REPORTE,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("reporte");
                            filas.clear();
                            int totalGeneral = 0;
                            int sinAtencionGeneral = 0;
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject o = arr.getJSONObject(i);
                                Fila f = new Fila();
                                f.medico = o.getString("nombre") + " " + o.getString("apellido");
                                f.especialidad = o.getString("especialidad");
                                f.total = o.getInt("total");
                                f.canceladas = o.getInt("canceladas");
                                f.inasistencias = o.getInt("inasistencias");
                                f.completadas = o.getInt("completadas");
                                filas.add(f);
                                totalGeneral += f.total;
                                sinAtencionGeneral += f.canceladas + f.inasistencias;
                            }
                            int pctGeneral = totalGeneral == 0 ? 0
                                    : Math.round(sinAtencionGeneral * 100f / totalGeneral);
                            tvResumen.setText("Consultorio: " + pctGeneral + "% de citas sin atención");
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar el reporte",
                        Toast.LENGTH_SHORT).show());
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    static class FilaHolder extends RecyclerView.ViewHolder {
        TextView tvMedico, tvEspecialidad, tvDetalle, tvPorcentaje;
        ProgressBar barra;

        FilaHolder(@NonNull View v) {
            super(v);
            tvMedico = v.findViewById(R.id.tvMedico);
            tvEspecialidad = v.findViewById(R.id.tvEspecialidad);
            tvDetalle = v.findViewById(R.id.tvDetalle);
            tvPorcentaje = v.findViewById(R.id.tvPorcentaje);
            barra = v.findViewById(R.id.barra);
        }
    }
}