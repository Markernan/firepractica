package com.pucp.firepractica.data;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pucp.firepractica.MundialApp;
import com.pucp.firepractica.model.Pronostico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CRUD de pronósticos en Firebase Realtime Database.
 * Cada usuario solo ve y escribe en pronosticos/{su uid} (ver database.rules.json).
 */
public class PronosticoRepository {

    public interface Resultado {
        void alTerminar(boolean exito);
    }

    public interface Escucha {
        void alCambiar(List<Pronostico> pronosticos);
        void alFallar(String mensaje);
    }

    /** Detiene la escucha en tiempo real. */
    public static class Suscripcion {
        private final Query consulta;
        private final ValueEventListener listener;

        Suscripcion(Query consulta, ValueEventListener listener) {
            this.consulta = consulta;
            this.listener = listener;
        }

        public void cancelar() {
            consulta.removeEventListener(listener);
        }
    }

    private DatabaseReference referenciaUsuario() {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            return null;
        }
        return MundialApp.baseDeDatos().getReference("pronosticos").child(usuario.getUid());
    }

    /** READ en tiempo real: se vuelve a llamar cada vez que se agrega, modifica o elimina un registro. */
    public Suscripcion escuchar(Escucha escucha) {
        DatabaseReference ref = referenciaUsuario();
        if (ref == null) {
            escucha.alFallar("Sin sesión");
            return null;
        }
        Query consulta = ref.orderByChild("fechaPartido");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pronostico> lista = new ArrayList<>();
                for (DataSnapshot hijo : snapshot.getChildren()) {
                    Pronostico p = hijo.getValue(Pronostico.class);
                    if (p != null) {
                        p.setId(hijo.getKey());
                        lista.add(p);
                    }
                }
                // orderByChild es ascendente: los partidos más próximos primero
                Collections.sort(lista, (a, b) -> Long.compare(a.getFechaPartido(), b.getFechaPartido()));
                escucha.alCambiar(lista);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                escucha.alFallar(error.getMessage());
            }
        };
        consulta.addValueEventListener(listener);
        return new Suscripcion(consulta, listener);
    }

    /** CREATE: el estado siempre nace como Pendiente. */
    public void crear(Pronostico pronostico, Resultado resultado) {
        DatabaseReference ref = referenciaUsuario();
        if (ref == null) {
            resultado.alTerminar(false);
            return;
        }
        pronostico.setEstado(Pronostico.PENDIENTE);
        ref.push().setValue(pronostico)
                .addOnSuccessListener(v -> resultado.alTerminar(true))
                .addOnFailureListener(e -> resultado.alTerminar(false));
    }

    /** UPDATE: solo si el registro en la base sigue Pendiente (evita editar uno ya bloqueado). */
    public void actualizar(Pronostico pronostico, Resultado resultado) {
        DatabaseReference ref = referenciaUsuario();
        if (ref == null || pronostico.getId() == null) {
            resultado.alTerminar(false);
            return;
        }
        DatabaseReference nodo = ref.child(pronostico.getId());
        nodo.child("estado").get().addOnSuccessListener(snapshot -> {
            if (!Pronostico.PENDIENTE.equals(snapshot.getValue(String.class))) {
                resultado.alTerminar(false);
                return;
            }
            nodo.setValue(pronostico)
                    .addOnSuccessListener(v -> resultado.alTerminar(true))
                    .addOnFailureListener(e -> resultado.alTerminar(false));
        }).addOnFailureListener(e -> resultado.alTerminar(false));
    }

    /** DELETE: elimina el nodo completo de Firebase; solo si sigue Pendiente. */
    public void eliminar(Pronostico pronostico, Resultado resultado) {
        DatabaseReference ref = referenciaUsuario();
        if (ref == null || pronostico.getId() == null) {
            resultado.alTerminar(false);
            return;
        }
        DatabaseReference nodo = ref.child(pronostico.getId());
        nodo.child("estado").get().addOnSuccessListener(snapshot -> {
            if (!Pronostico.PENDIENTE.equals(snapshot.getValue(String.class))) {
                resultado.alTerminar(false);
                return;
            }
            nodo.removeValue()
                    .addOnSuccessListener(v -> resultado.alTerminar(true))
                    .addOnFailureListener(e -> resultado.alTerminar(false));
        }).addOnFailureListener(e -> resultado.alTerminar(false));
    }
}
