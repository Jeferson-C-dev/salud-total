package com.example.saludtotal;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservarCitaActivity extends AppCompatActivity {

    private Spinner spinnerEspecialidad, spinnerMedico, spinnerHora;
    private TextView tvFecha;
    private EditText etMotivo, etBuscarMedico;
    private Button btnConfirmar;
    private ImageView btnRegresar;
    private RequestQueue requestQueue;
    private CacheDbHelper cacheDb;

    private List<String> especialidadIds = new ArrayList<>();
    private List<String> especialidadNombres = new ArrayList<>();
    private List<String> medicoIds = new ArrayList<>();
    private List<String> medicoNombres = new ArrayList<>();
    private List<String> horasOriginales = new ArrayList<>();

    // Catálogo completo para el buscador: [medicoId, nombre, especialidadId, especialidadNombre]
    private List<String[]> catalogo = new ArrayList<>();

    // Para seleccionar el médico buscado cuando cargue su lista
    private String medicoPendiente = null;

    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservar_cita);

        spinnerEspecialidad = findViewById(R.id.spinnerEspecialidad);
        spinnerMedico = findViewById(R.id.spinnerMedico);
        spinnerHora = findViewById(R.id.spinnerHora);
        tvFecha = findViewById(R.id.tvFecha);
        etMotivo = findViewById(R.id.etMotivo);
        etBuscarMedico = findViewById(R.id.etBuscarMedico);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnRegresar = findViewById(R.id.btnRegresar);
        requestQueue = Volley.newRequestQueue(this);
        cacheDb = new CacheDbHelper(this);

        btnRegresar.setOnClickListener(v -> finish());
        cargarEspecialidades();

        spinnerEspecialidad.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               android.view.View view, int position, long id) {
                        if (position >= 0 && position < especialidadIds.size()) {
                            cargarMedicos(especialidadIds.get(position));
                        }
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });

        // Buscador: al escribir, muestra coincidencias para elegir
        etBuscarMedico.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().trim().toLowerCase();
                if (texto.length() >= 2) {
                    mostrarCoincidencias(texto);
                }
            }
        });

        tvFecha.setOnClickListener(v -> mostrarCalendario());
        btnConfirmar.setOnClickListener(v -> reservar());
    }

    // ---------- BUSCADOR ----------
    private void mostrarCoincidencias(String texto) {
        List<String[]> coincidencias = new ArrayList<>();
        for (String[] m : catalogo) {
            if (m[1].toLowerCase().contains(texto) || m[3].toLowerCase().contains(texto)) {
                coincidencias.add(m);
            }
        }
        if (coincidencias.isEmpty()) {
            return;
        }
        String[] etiquetas = new String[coincidencias.size()];
        for (int i = 0; i < coincidencias.size(); i++) {
            etiquetas[i] = coincidencias.get(i)[1] + " · " + coincidencias.get(i)[3];
        }
        new AlertDialog.Builder(this)
                .setTitle("Médicos encontrados")
                .setItems(etiquetas, (dialog, which) -> {
                    String[] elegido = coincidencias.get(which);
                    seleccionarMedico(elegido[0], elegido[2]);
                    etBuscarMedico.setText("");
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void seleccionarMedico(String medicoId, String especialidadId) {
        int posEsp = especialidadIds.indexOf(especialidadId);
        if (posEsp < 0) return;
        medicoPendiente = medicoId;
        if (spinnerEspecialidad.getSelectedItemPosition() == posEsp) {
            // Misma especialidad: no se dispara el listener, se selecciona directo
            aplicarMedicoPendiente();
        } else {
            spinnerEspecialidad.setSelection(posEsp);
        }
    }

    private void aplicarMedicoPendiente() {
        if (medicoPendiente == null) return;
        int pos = medicoIds.indexOf(medicoPendiente);
        if (pos >= 0) {
            spinnerMedico.setSelection(pos);
        }
        medicoPendiente = null;
    }

    // Trae el catálogo completo de médicos para el buscador
    private void cargarCatalogo() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_MEDICOS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("medicos");
                            catalogo.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject m = arr.getJSONObject(i);
                                String espNombre = m.getString("especialidad");
                                int pos = especialidadNombres.indexOf(espNombre);
                                String espId = pos >= 0 ? especialidadIds.get(pos) : "";
                                catalogo.add(new String[]{
                                        m.getString("medico_id"),
                                        "Dr(a). " + m.getString("nombre") + " " + m.getString("apellido"),
                                        espId,
                                        espNombre
                                });
                            }
                        }
                    } catch (Exception ignored) {
                    }
                },
                error -> { });
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    // ---------- ESPECIALIDADES (servidor + caché SQLite) ----------
    private void mostrarEspecialidades() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, especialidadNombres);
        spinnerEspecialidad.setAdapter(adapter);
    }

    private void cargarEspecialidades() {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_ESPECIALIDADES,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("especialidades");
                            especialidadIds.clear();
                            especialidadNombres.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject e = arr.getJSONObject(i);
                                especialidadIds.add(e.getString("id"));
                                especialidadNombres.add(e.getString("nombre"));
                            }
                            cacheDb.guardarEspecialidades(especialidadIds, especialidadNombres);
                            mostrarEspecialidades();
                            cargarCatalogo();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    cacheDb.leerEspecialidades(especialidadIds, especialidadNombres);
                    if (especialidadIds.isEmpty()) {
                        Toast.makeText(this, "Sin conexión y sin datos guardados",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Sin conexión: mostrando datos guardados",
                                Toast.LENGTH_SHORT).show();
                        mostrarEspecialidades();
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    // ---------- MÉDICOS (servidor + caché SQLite) ----------
    private void mostrarMedicos() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, medicoNombres);
        spinnerMedico.setAdapter(adapter);
        aplicarMedicoPendiente();
    }

    private void cargarMedicos(String especialidadId) {
        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_MEDICOS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("medicos");
                            medicoIds.clear();
                            medicoNombres.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject m = arr.getJSONObject(i);
                                medicoIds.add(m.getString("medico_id"));
                                medicoNombres.add("Dr(a). " + m.getString("nombre")
                                        + " " + m.getString("apellido"));
                            }
                            cacheDb.guardarMedicos(especialidadId, medicoIds, medicoNombres);
                            mostrarMedicos();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    medicoIds.clear();
                    medicoNombres.clear();
                    cacheDb.leerMedicos(especialidadId, medicoIds, medicoNombres);
                    if (medicoIds.isEmpty()) {
                        Toast.makeText(this,
                                "Sin conexión: no hay médicos guardados para esta especialidad",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Sin conexión: médicos guardados",
                                Toast.LENGTH_SHORT).show();
                    }
                    mostrarMedicos();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("especialidad_id", especialidadId);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void mostrarCalendario() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            month = month + 1;
            fechaSeleccionada = year + "-" + String.format("%02d", month)
                    + "-" + String.format("%02d", day);
            tvFecha.setText(FormatoHelper.fechaEspanol(fechaSeleccionada));
            cargarHorarios();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void cargarHorarios() {
        if (spinnerMedico.getSelectedItemPosition() < 0 || medicoIds.isEmpty()) {
            Toast.makeText(this, "Seleccione un médico primero", Toast.LENGTH_SHORT).show();
            return;
        }
        String medicoId = medicoIds.get(spinnerMedico.getSelectedItemPosition());

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.GET_HORARIOS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            JSONArray arr = json.getJSONArray("horarios");
                            List<String> horasMostrar = new ArrayList<>();
                            horasOriginales.clear();
                            for (int i = 0; i < arr.length(); i++) {
                                String horaOriginal = arr.getString(i);
                                horasOriginales.add(horaOriginal);
                                horasMostrar.add(FormatoHelper.hora12(horaOriginal));
                            }
                            if (horasMostrar.isEmpty()) {
                                horasMostrar.add("No hay horarios disponibles");
                                horasOriginales.add("No hay horarios disponibles");
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_dropdown_item, horasMostrar);
                            spinnerHora.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error al cargar horarios",
                        Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("medico_id", medicoId);
                params.put("fecha", fechaSeleccionada);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }

    private void reservar() {
        if (medicoIds.isEmpty() || spinnerMedico.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Seleccione un médico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerHora.getSelectedItem() == null || horasOriginales.isEmpty()) {
            Toast.makeText(this, "Seleccione un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        String hora = horasOriginales.get(spinnerHora.getSelectedItemPosition());
        String horaMostrar = spinnerHora.getSelectedItem().toString();

        if (hora.equals("No hay horarios disponibles")) {
            Toast.makeText(this, "No hay horarios disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("sesion", MODE_PRIVATE);
        String pacienteId = String.valueOf(prefs.getInt("id", 0));
        String medicoId = medicoIds.get(spinnerMedico.getSelectedItemPosition());
        String medicoNombre = medicoNombres.get(spinnerMedico.getSelectedItemPosition());
        String motivo = etMotivo.getText().toString().trim();

        StringRequest request = new StringRequest(Request.Method.POST,
                ApiConfig.RESERVAR_CITA,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {

                            RecordatorioHelper.programar(this, json.optString("cita_id", "0"),
                                    fechaSeleccionada, hora, medicoNombre);

                            NotificacionHelper.enviarNotificacion(this,
                                    "✅ Cita reservada",
                                    "Tu cita con " + medicoNombre + " para el "
                                            + FormatoHelper.fechaEspanol(fechaSeleccionada)
                                            + " a las " + horaMostrar
                                            + " ha sido registrada exitosamente.");

                            new AlertDialog.Builder(this)
                                    .setTitle("✅ ¡Cita reservada!")
                                    .setMessage("Tu cita ha sido registrada exitosamente." +
                                            "\n\nRecuerda asistir puntualmente.")
                                    .setPositiveButton("Ver mis citas", (dialog, which) -> {
                                        startActivity(new Intent(ReservarCitaActivity.this,
                                                MisCitasActivity.class));
                                        finish();
                                    })
                                    .setNegativeButton("Volver al inicio", (dialog, which) -> finish())
                                    .setCancelable(false)
                                    .show();
                        } else {
                            Toast.makeText(this, json.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Error de conexión: la reserva requiere internet",
                        Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("paciente_id", pacienteId);
                params.put("medico_id", medicoId);
                params.put("fecha_cita", fechaSeleccionada);
                params.put("hora_cita", hora);
                params.put("motivo", motivo);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, 1f));
        requestQueue.add(request);
    }
}