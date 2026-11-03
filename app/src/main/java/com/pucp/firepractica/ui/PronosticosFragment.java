package com.pucp.firepractica.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pucp.firepractica.R;
import com.pucp.firepractica.data.PronosticoRepository;
import com.pucp.firepractica.databinding.FragmentPronosticosBinding;
import com.pucp.firepractica.model.Pronostico;

import java.util.List;

/** Lista en tiempo real de los pronósticos del usuario, con registrar, editar y eliminar. */
public class PronosticosFragment extends Fragment {

    private FragmentPronosticosBinding binding;
    private final PronosticoRepository repositorio = new PronosticoRepository();
    private PronosticoRepository.Suscripcion suscripcion;
    private PronosticoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPronosticosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new PronosticoAdapter(new PronosticoAdapter.Acciones() {
            @Override
            public void editar(Pronostico pronostico) {
                if (!pronostico.esPendiente()) {
                    Toast.makeText(requireContext(), R.string.error_solo_pendiente, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(requireContext(), PronosticoFormActivity.class);
                intent.putExtra(PronosticoFormActivity.EXTRA_PRONOSTICO, pronostico);
                intent.putExtra(PronosticoFormActivity.EXTRA_ID, pronostico.getId());
                startActivity(intent);
            }

            @Override
            public void eliminar(Pronostico pronostico) {
                confirmarEliminar(pronostico);
            }
        });
        binding.rvPronosticos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPronosticos.setAdapter(adapter);

        binding.fabNuevo.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PronosticoFormActivity.class)));
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
                adapter.reemplazar(pronosticos);
                binding.tvVacio.setVisibility(pronosticos.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void alFallar(String mensaje) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.error_cargar, Toast.LENGTH_LONG).show();
                }
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

    private void confirmarEliminar(Pronostico pronostico) {
        if (!pronostico.esPendiente()) {
            Toast.makeText(requireContext(), R.string.error_solo_pendiente, Toast.LENGTH_SHORT).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_titulo)
                .setMessage(getString(R.string.eliminar_mensaje,
                        pronostico.getSeleccionA(), pronostico.getSeleccionB()))
                .setPositiveButton(R.string.btn_eliminar, (d, w) ->
                        repositorio.eliminar(pronostico, exito -> {
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(),
                                    exito ? R.string.eliminado_ok : R.string.error_eliminar,
                                    Toast.LENGTH_SHORT).show();
                        }))
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }
}
