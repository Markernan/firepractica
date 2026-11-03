package com.pucp.firepractica.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.pucp.firepractica.R;
import com.pucp.firepractica.data.PronosticoRepository;
import com.pucp.firepractica.databinding.ActivityPronosticoFormBinding;
import com.pucp.firepractica.model.Pronostico;
import com.pucp.firepractica.util.Fechas;
import com.pucp.firepractica.util.Notificaciones;

import java.util.Calendar;

/** Formulario para registrar un pronóstico nuevo o editar uno pendiente. */
public class PronosticoFormActivity extends AppCompatActivity {

    public static final String EXTRA_PRONOSTICO = "pronostico";
    public static final String EXTRA_ID = "id";

    private ActivityPronosticoFormBinding binding;
    private final PronosticoRepository repositorio = new PronosticoRepository();
    private Pronostico existente;
    private long fechaElegida = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPronosticoFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        existente = (Pronostico) getIntent().getSerializableExtra(EXTRA_PRONOSTICO);
        if (existente != null) {
            existente.setId(getIntent().getStringExtra(EXTRA_ID));
        }

        configurarSelecciones();
        binding.etFecha.setOnClickListener(v -> elegirFecha());
        binding.btnGuardar.setOnClickListener(v -> guardar());
        binding.btnCancelar.setOnClickListener(v -> finish());

        if (existente != null) {
            prepararEdicion();
        }
    }

    private void configurarSelecciones() {
        // Se puede elegir de la lista o escribir cualquier selección: el campo es un String libre.
        String[] selecciones = getResources().getStringArray(R.array.selecciones);
        binding.acSeleccionA.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, selecciones));
        binding.acSeleccionB.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, selecciones));
    }

    private void prepararEdicion() {
        binding.tvTituloForm.setText(R.string.form_titulo_editar);
        binding.btnGuardar.setText(R.string.btn_actualizar);

        binding.acSeleccionA.setText(existente.getSeleccionA(), false);
        binding.acSeleccionB.setText(existente.getSeleccionB(), false);
        fechaElegida = existente.getFechaPartido();
        binding.etFecha.setText(Fechas.formatear(fechaElegida));
        binding.etGolesA.setText(String.valueOf(existente.getGolesA()));
        binding.etGolesB.setText(String.valueOf(existente.getGolesB()));

        // El estado solo se puede cambiar al editar: Pendiente, Acertado o Fallado.
        binding.bloqueEstado.setVisibility(android.view.View.VISIBLE);
        String[] estados = {Pronostico.PENDIENTE, Pronostico.ACERTADO, Pronostico.FALLADO};
        binding.acEstado.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estados));
        binding.acEstado.setText(existente.getEstado(), false);
    }

    private void elegirFecha() {
        Calendar c = Calendar.getInstance();
        if (fechaElegida > 0) {
            c.setTimeInMillis(fechaElegida);
        }
        new DatePickerDialog(this, (picker, anio, mes, dia) -> {
            fechaElegida = Fechas.medianoche(anio, mes, dia);
            binding.etFecha.setText(Fechas.formatear(fechaElegida));
            binding.tilFecha.setError(null);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardar() {
        limpiarErrores();

        String a = texto(binding.acSeleccionA.getText());
        String b = texto(binding.acSeleccionB.getText());
        boolean valido = true;

        if (a.isEmpty()) {
            error(binding.tilSeleccionA, R.string.error_campo_obligatorio);
            valido = false;
        }
        if (b.isEmpty()) {
            error(binding.tilSeleccionB, R.string.error_campo_obligatorio);
            valido = false;
        }
        // Las selecciones no pueden ser iguales (sin distinguir mayúsculas ni espacios sobrantes)
        if (!a.isEmpty() && !b.isEmpty() && a.equalsIgnoreCase(b)) {
            error(binding.tilSeleccionB, R.string.error_selecciones_iguales);
            valido = false;
        }
        if (fechaElegida < 0) {
            error(binding.tilFecha, R.string.error_fecha_obligatoria);
            valido = false;
        }
        Integer golesA = entero(texto(binding.etGolesA.getText()));
        Integer golesB = entero(texto(binding.etGolesB.getText()));
        if (golesA == null) {
            error(binding.tilGolesA, R.string.error_goles_invalidos);
            valido = false;
        }
        if (golesB == null) {
            error(binding.tilGolesB, R.string.error_goles_invalidos);
            valido = false;
        }
        if (!valido) {
            return;
        }

        binding.btnGuardar.setEnabled(false);
        if (existente == null) {
            registrar(a, b, golesA, golesB);
        } else {
            actualizar(a, b, golesA, golesB);
        }
    }

    private void registrar(String a, String b, int golesA, int golesB) {
        // El estado nace siempre como Pendiente (lo fija el constructor y el repositorio)
        Pronostico nuevo = new Pronostico(a, b, fechaElegida, golesA, golesB);
        repositorio.crear(nuevo, exito -> {
            if (exito) {
                new Notificaciones(this).pronosticoRegistrado(a, b);
                Toast.makeText(this, getString(R.string.notif_registrado_texto, a, b), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                falloAlGuardar();
            }
        });
    }

    private void actualizar(String a, String b, int golesA, int golesB) {
        if (!existente.esPendiente()) {
            Toast.makeText(this, R.string.error_solo_pendiente, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        existente.setSeleccionA(a);
        existente.setSeleccionB(b);
        existente.setFechaPartido(fechaElegida);
        existente.setGolesA(golesA);
        existente.setGolesB(golesB);
        existente.setEstado(texto(binding.acEstado.getText()));

        repositorio.actualizar(existente, exito -> {
            if (exito) {
                new Notificaciones(this).pronosticoActualizado(a, b);
                Toast.makeText(this, getString(R.string.notif_actualizado_texto, a, b), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                falloAlGuardar();
            }
        });
    }

    private void falloAlGuardar() {
        binding.btnGuardar.setEnabled(true);
        Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_LONG).show();
    }

    private void limpiarErrores() {
        binding.tilSeleccionA.setError(null);
        binding.tilSeleccionB.setError(null);
        binding.tilFecha.setError(null);
        binding.tilGolesA.setError(null);
        binding.tilGolesB.setError(null);
    }

    private void error(TextInputLayout campo, int mensaje) {
        campo.setError(getString(mensaje));
    }

    private static Integer entero(String valor) {
        if (valor.isEmpty()) {
            return null;
        }
        try {
            int n = Integer.parseInt(valor);
            return n >= 0 ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String texto(CharSequence valor) {
        return valor == null ? "" : valor.toString().trim();
    }
}
