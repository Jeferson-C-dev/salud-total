package com.example.saludtotal;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {

    public interface OnCancelarListener {
        void onCancelar(String citaId);
    }

    public interface OnConfirmarListener {
        void onConfirmar(String citaId);
    }

    public interface OnCompletarListener {
        void onCompletar(String citaId);
    }

    public interface OnNoAsistioListener {
        void onNoAsistio(String citaId);
    }

    public interface OnEliminadaListener {
        void onEliminada();
    }

    public interface OnReprogramarListener {
        void onReprogramar(Cita cita);
    }

    private List<Cita> citas;
    private OnCancelarListener cancelarListener;
    private OnConfirmarListener confirmarListener;
    private OnCompletarListener completarListener;
    private OnNoAsistioListener noAsistioListener;
    private OnEliminadaListener eliminadaListener;
    private OnReprogramarListener reprogramarListener;
    private boolean esMedico;
    private boolean soloLectura = false;

    // Paciente / admin
    public CitaAdapter(List<Cita> citas, OnCancelarListener cancelarListener) {
        this.citas = citas;
        this.cancelarListener = cancelarListener;
        this.esMedico = false;
    }

    // Médico
    public CitaAdapter(List<Cita> citas, OnCancelarListener cancelarListener,
                       OnConfirmarListener confirmarListener,
                       OnCompletarListener completarListener,
                       OnNoAsistioListener noAsistioListener) {
        this.citas = citas;
        this.cancelarListener = cancelarListener;
        this.confirmarListener = confirmarListener;
        this.completarListener = completarListener;
        this.noAsistioListener = noAsistioListener;
        this.esMedico = true;
    }

    public void setOnEliminadaListener(OnEliminadaListener listener) {
        this.eliminadaListener = listener;
    }

    public void setOnReprogramarListener(OnReprogramarListener listener) {
        this.reprogramarListener = listener;
    }

    public void setSoloLectura(boolean soloLectura) {
        this.soloLectura = soloLectura;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citas.get(position);
        holder.tvMedico.setText(cita.medico);
        holder.tvEspecialidad.setText(cita.especialidad);
        holder.tvFechaHora.setText(FormatoHelper.fechaHora(cita.fecha, cita.hora));

        if (cita.notas != null && !cita.notas.isEmpty() && !cita.notas.equals("null")) {
            holder.tvNotas.setText("📝 Notas: " + cita.notas);
            holder.tvNotas.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotas.setVisibility(View.GONE);
        }

        String estadoTexto = cita.estado.substring(0, 1).toUpperCase() + cita.estado.substring(1);
        holder.tvEstado.setText(estadoTexto);

        switch (cita.estado) {
            case "confirmada":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_green);
                holder.tvEstado.setTextColor(0xFF0F6E56);
                break;
            case "cancelada":
            case "inasistencia":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_red);
                holder.tvEstado.setTextColor(0xFFA32D2D);
                break;
            case "completada":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_blue);
                holder.tvEstado.setTextColor(0xFF185FA5);
                break;
            default:
                holder.tvEstado.setBackgroundResource(R.drawable.badge_bg);
                holder.tvEstado.setTextColor(0xFF854F0B);
                break;
        }

        if (soloLectura) {
            holder.btnCancelar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnConfirmar.setVisibility(View.GONE);
            holder.btnCompletar.setVisibility(View.GONE);
            holder.btnNoAsistio.setVisibility(View.GONE);
            holder.btnReprogramar.setVisibility(View.GONE);
            return;
        }

        boolean activa = cita.estado.equals("pendiente") || cita.estado.equals("confirmada");
        boolean eliminable = cita.estado.equals("cancelada") || cita.estado.equals("inasistencia");

        if (activa) {
            holder.btnCancelar.setVisibility(View.VISIBLE);
            holder.btnCancelar.setOnClickListener(v -> cancelarListener.onCancelar(cita.id));
        } else {
            holder.btnCancelar.setVisibility(View.GONE);
        }

        if (activa && reprogramarListener != null) {
            holder.btnReprogramar.setVisibility(View.VISIBLE);
            holder.btnReprogramar.setOnClickListener(v -> reprogramarListener.onReprogramar(cita));
        } else {
            holder.btnReprogramar.setVisibility(View.GONE);
        }

        if (eliminable) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    confirmarEliminar(v.getContext(), citas.get(pos));
                }
            });
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
        }

        if (esMedico && cita.estado.equals("pendiente") && confirmarListener != null) {
            holder.btnConfirmar.setVisibility(View.VISIBLE);
            holder.btnConfirmar.setOnClickListener(v -> confirmarListener.onConfirmar(cita.id));
        } else {
            holder.btnConfirmar.setVisibility(View.GONE);
        }

        if (esMedico && cita.estado.equals("confirmada") && completarListener != null) {
            holder.btnCompletar.setVisibility(View.VISIBLE);
            holder.btnCompletar.setOnClickListener(v -> completarListener.onCompletar(cita.id));
        } else {
            holder.btnCompletar.setVisibility(View.GONE);
        }

        if (esMedico && cita.estado.equals("confirmada") && noAsistioListener != null) {
            holder.btnNoAsistio.setVisibility(View.VISIBLE);
            holder.btnNoAsistio.setOnClickListener(v -> noAsistioListener.onNoAsistio(cita.id));
        } else {
            holder.btnNoAsistio.setVisibility(View.GONE);
        }
    }

    private void confirmarEliminar(Context context, Cita cita) {
        new AlertDialog.Builder(context)
                .setTitle("🗑️ Eliminar cita")
                .setMessage("Esta acción es permanente. ¿Eliminar esta cita?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    StringRequest request = new StringRequest(Request.Method.POST,
                            ApiConfig.ELIMINAR_CITA,
                            response -> {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    Toast.makeText(context, json.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                    if (json.getBoolean("success")) {
                                        int idx = citas.indexOf(cita);
                                        if (idx >= 0) {
                                            citas.remove(idx);
                                            notifyItemRemoved(idx);
                                        }
                                        if (eliminadaListener != null) {
                                            eliminadaListener.onEliminada();
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(context, "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(context, "Error de conexión",
                                    Toast.LENGTH_SHORT).show()) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("cita_id", cita.id);
                            return params;
                        }
                    };
                    request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
                    Volley.newRequestQueue(context).add(request);
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedico, tvEspecialidad, tvFechaHora, tvEstado, tvNotas;
        Button btnCancelar, btnConfirmar, btnCompletar, btnNoAsistio, btnReprogramar, btnEliminar;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedico = itemView.findViewById(R.id.tvMedico);
            tvEspecialidad = itemView.findViewById(R.id.tvEspecialidad);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvNotas = itemView.findViewById(R.id.tvNotas);
            btnCancelar = itemView.findViewById(R.id.btnCancelar);
            btnConfirmar = itemView.findViewById(R.id.btnConfirmar);
            btnCompletar = itemView.findViewById(R.id.btnCompletar);
            btnNoAsistio = itemView.findViewById(R.id.btnNoAsistio);
            btnReprogramar = itemView.findViewById(R.id.btnReprogramar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}