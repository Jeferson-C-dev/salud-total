package com.example.saludtotal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PacienteAdapter extends RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder> {

    public interface OnPacienteClickListener {
        void onClick(PacienteItem paciente);
    }

    private List<PacienteItem> pacientes;
    private OnPacienteClickListener listener;

    public PacienteAdapter(List<PacienteItem> pacientes, OnPacienteClickListener listener) {
        this.pacientes = pacientes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paciente, parent, false);
        return new PacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        PacienteItem p = pacientes.get(position);

        String iniciales = p.nombre.substring(0, 1).toUpperCase()
                + p.apellido.substring(0, 1).toUpperCase();
        holder.tvIniciales.setText(iniciales);
        holder.tvNombre.setText(p.nombre + " " + p.apellido);

        String dni = (p.dni == null || p.dni.isEmpty() || p.dni.equals("null")) ? "Sin DNI" : "DNI " + p.dni;
        String tel = (p.telefono == null || p.telefono.isEmpty() || p.telefono.equals("null")) ? "" : "  ·  Tel " + p.telefono;
        holder.tvDatos.setText(dni + tel);

        holder.tvCitas.setText(p.totalCitas + (p.totalCitas == 1 ? " cita" : " citas"));
        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return pacientes.size();
    }

    static class PacienteViewHolder extends RecyclerView.ViewHolder {
        TextView tvIniciales, tvNombre, tvDatos, tvCitas;

        public PacienteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIniciales = itemView.findViewById(R.id.tvIniciales);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDatos = itemView.findViewById(R.id.tvDatos);
            tvCitas = itemView.findViewById(R.id.tvCitas);
        }
    }
}