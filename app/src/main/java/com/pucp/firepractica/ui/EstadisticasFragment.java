package com.pucp.firepractica.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.pucp.firepractica.R;
import com.pucp.firepractica.data.PronosticoRepository;
import com.pucp.firepractica.databinding.FragmentEstadisticasBinding;
import com.pucp.firepractica.databinding.ItemContadorBinding;
import com.pucp.firepractica.model.Pronostico;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Estadísticas en tiempo real: total, acertados, fallados y pendientes, con un gráfico de barras y
 * uno circular. Se pueden filtrar por selección; se recalculan solos cuando cambian los datos.
 */
public class EstadisticasFragment extends Fragment {

    private FragmentEstadisticasBinding binding;
    private final PronosticoRepository repositorio = new PronosticoRepository();
    private PronosticoRepository.Suscripcion suscripcion;

    private List<Pronostico> todos = new ArrayList<>();
    /** null = todas las selecciones. */
    private String filtro = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.acFiltro.setOnItemClickListener((parent, v, posicion, id) -> {
            filtro = posicion == 0 ? null : (String) parent.getItemAtPosition(posicion);
            actualizar();
        });
        actualizar();
    }

    @Override
    public void onStart() {
        super.onStart();
        suscripcion = repositorio.escuchar(new PronosticoRepository.Escucha() {
            @Override
            public void alCambiar(List<Pronostico> pronosticos) {
                if (binding == null) {
                    return;
                }
                todos = pronosticos;
                actualizarOpcionesFiltro();
                actualizar();
            }

            @Override
            public void alFallar(String mensaje) {
                // Se mantiene lo último que se mostró
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (suscripcion != null) {
            suscripcion.cancelar();
            suscripcion = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /** Lista de selecciones que aparecen en los pronósticos (sin repetir, ordenadas). */
    private void actualizarOpcionesFiltro() {
        TreeSet<String> selecciones = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Pronostico p : todos) {
            selecciones.add(p.getSeleccionA());
            selecciones.add(p.getSeleccionB());
        }
        // Si la selección filtrada ya no existe (por ejemplo, se eliminó), se vuelve a "todas"
        if (filtro != null && !selecciones.contains(filtro)) {
            filtro = null;
        }

        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.est_todas));
        opciones.addAll(selecciones);
        binding.acFiltro.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, opciones));
        binding.acFiltro.setText(filtro == null ? getString(R.string.est_todas) : filtro, false);
    }

    private void actualizar() {
        if (binding == null) {
            return;
        }
        int total = 0;
        int acertados = 0;
        int fallados = 0;
        int pendientes = 0;
        for (Pronostico p : todos) {
            if (filtro != null && !filtro.equalsIgnoreCase(p.getSeleccionA())
                    && !filtro.equalsIgnoreCase(p.getSeleccionB())) {
                continue;
            }
            total++;
            if (Pronostico.ACERTADO.equals(p.getEstado())) {
                acertados++;
            } else if (Pronostico.FALLADO.equals(p.getEstado())) {
                fallados++;
            } else {
                pendientes++;
            }
        }

        int colorAcertado = ContextCompat.getColor(requireContext(), R.color.estado_acertado);
        int colorFallado = ContextCompat.getColor(requireContext(), R.color.estado_fallado);
        int colorPendiente = ContextCompat.getColor(requireContext(), R.color.estado_pendiente);
        int colorTotal = ContextCompat.getColor(requireContext(), R.color.verde_cancha);

        contador(binding.tarjetaTotal, total, R.string.est_total, colorTotal);
        contador(binding.tarjetaAcertados, acertados, R.string.est_acertados, colorAcertado);
        contador(binding.tarjetaFallados, fallados, R.string.est_fallados, colorFallado);
        contador(binding.tarjetaPendientes, pendientes, R.string.est_pendientes, colorPendiente);

        binding.graficoBarras.setDatos(
                new String[]{getString(R.string.est_total), getString(R.string.est_acertados),
                        getString(R.string.est_fallados), getString(R.string.est_pendientes)},
                new int[]{total, acertados, fallados, pendientes},
                new int[]{colorTotal, colorAcertado, colorFallado, colorPendiente});

        binding.graficoCircular.setDatos(
                new String[]{getString(R.string.est_acertados), getString(R.string.est_fallados),
                        getString(R.string.est_pendientes)},
                new int[]{acertados, fallados, pendientes},
                new int[]{colorAcertado, colorFallado, colorPendiente});
    }

    private void contador(ItemContadorBinding tarjeta, int valor, int etiqueta, int color) {
        tarjeta.tvValor.setText(String.valueOf(valor));
        tarjeta.tvValor.setTextColor(color);
        ((TextView) tarjeta.tvEtiqueta).setText(etiqueta);
    }
}
