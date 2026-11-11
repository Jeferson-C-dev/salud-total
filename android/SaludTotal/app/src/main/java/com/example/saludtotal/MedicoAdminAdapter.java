package com.example.saludtotal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicoAdminAdapter extends RecyclerView.Adapter<MedicoAdminAdapter.MedicoViewHolder> {

    public interface OnToggleListener {
        void onToggle(String medicoId, boolean activo);
    }

    private List<MedicoAdmin> medicos;
    private OnToggleListener listener;

    public MedicoAdminAdapter(List<MedicoAdmin> medicos, OnToggleListener listener) {
        this.medicos = medicos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medico, parent, false);
        return new MedicoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicoViewHolder holder, int position) {
        MedicoAdmin medico = medicos.get(position);

        String iniciales = medico.nombre.substring(0, 1).toUpperCase()
                + medico.apellido.substring(0, 1).toUpperCase();
        holder.tvIniciales.setText(iniciales);
        holder.tvNombreMedico.setText("Dr(a). " + medico.nombre + " " + medico.apellido);
        holder.tvEspecialidad.setText(medico.especialidad);
        holder.tvColegiatura.setText("CMP: " + medico.colegiatura);

        holder.switchActivo.setOnCheckedChangeListener(null);
        holder.switchActivo.setChecked(medico.activo);
        holder.switchActivo.setOnCheckedChangeListener((buttonView, isChecked) ->
                listener.onToggle(medico.medicoId, isChecked)
        );
    }

    @Override
    public int getItemCount() {
        return medicos.size();
    }

    static class MedicoViewHolder extends RecyclerView.ViewHolder {
        TextView tvIniciales, tvNombreMedico, tvEspecialidad, tvColegiatura;
        Switch switchActivo;

        public MedicoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIniciales = itemView.findViewById(R.id.tvIniciales);
            tvNombreMedico = itemView.findViewById(R.id.tvNombreMedico);
            tvEspecialidad = itemView.findViewById(R.id.tvEspecialidad);
            tvColegiatura = itemView.findViewById(R.id.tvColegiatura);
            switchActivo = itemView.findViewById(R.id.switchActivo);
        }
    }
}