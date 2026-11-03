package com.pucp.firepractica.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pucp.firepractica.R;
import com.pucp.firepractica.databinding.ItemPronosticoBinding;
import com.pucp.firepractica.model.Pronostico;
import com.pucp.firepractica.util.Fechas;

import java.util.ArrayList;
import java.util.List;

public class PronosticoAdapter extends RecyclerView.Adapter<PronosticoAdapter.Holder> {

    public interface Acciones {
        void editar(Pronostico pronostico);
        void eliminar(Pronostico pronostico);
    }

    private final List<Pronostico> datos = new ArrayList<>();
    private final Acciones acciones;

    public PronosticoAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void reemplazar(List<Pronostico> nuevos) {
        datos.clear();
        datos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPronosticoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.enlazar(datos.get(position), acciones);
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        private final ItemPronosticoBinding b;

        Holder(ItemPronosticoBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        void enlazar(Pronostico p, Acciones acciones) {
            Context ctx = b.getRoot().getContext();
            b.tvPartido.setText(ctx.getString(R.string.item_vs, p.getSeleccionA(), p.getSeleccionB()));
            b.tvFecha.setText(ctx.getString(R.string.item_fecha, Fechas.formatear(p.getFechaPartido())));
            b.tvResultado.setText(ctx.getString(R.string.item_resultado, p.getGolesA(), p.getGolesB()));

            b.tvEstado.setText(p.getEstado());
            GradientDrawable chip = new GradientDrawable();
            chip.setCornerRadius(40f);
            chip.setColor(colorEstado(ctx, p.getEstado()));
            b.tvEstado.setBackground(chip);

            // Acertado/Fallado: bloqueado, sin editar ni eliminar.
            boolean editable = p.esPendiente();
            b.filaAcciones.setVisibility(editable ? View.VISIBLE : View.GONE);
            b.filaBloqueado.setVisibility(editable ? View.GONE : View.VISIBLE);
            b.btnEditar.setOnClickListener(v -> acciones.editar(p));
            b.btnEliminar.setOnClickListener(v -> acciones.eliminar(p));
        }

        private static int colorEstado(Context ctx, String estado) {
            if (Pronostico.ACERTADO.equals(estado)) {
                return ContextCompat.getColor(ctx, R.color.estado_acertado);
            }
            if (Pronostico.FALLADO.equals(estado)) {
                return ContextCompat.getColor(ctx, R.color.estado_fallado);
            }
            return ContextCompat.getColor(ctx, R.color.estado_pendiente);
        }
    }
}
