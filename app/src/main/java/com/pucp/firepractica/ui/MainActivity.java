package com.pucp.firepractica.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.pucp.firepractica.R;
import com.pucp.firepractica.auth.LoginActivity;
import com.pucp.firepractica.databinding.ActivityMainBinding;

/** Contenedor principal: barra de navegación con Mis Pronósticos, Estadísticas y Cerrar Sesión. */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_PRONOSTICOS = "pronosticos";
    private static final String TAG_ESTADISTICAS = "estadisticas";

    private ActivityMainBinding binding;

    // Debe registrarse antes de que la Activity llegue a STARTED (por eso es un campo)
    private final ActivityResultLauncher<String> permisoNotificaciones =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (!concedido) {
                    Toast.makeText(this, R.string.toast_permiso_notificaciones, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sin sesión no hay nada que mostrar (por ejemplo, si la sesión se cerró en otra pantalla)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irALogin();
            return;
        }

        binding.barraNavegacion.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pronosticos) {
                mostrar(new PronosticosFragment(), TAG_PRONOSTICOS);
                return true;
            }
            if (id == R.id.nav_estadisticas) {
                mostrar(new EstadisticasFragment(), TAG_ESTADISTICAS);
                return true;
            }
            if (id == R.id.nav_cerrar_sesion) {
                confirmarCerrarSesion();
                return false; // Cerrar sesión es una acción: no queda seleccionada
            }
            return false;
        });

        if (savedInstanceState == null) {
            binding.barraNavegacion.setSelectedItemId(R.id.nav_pronosticos);
            pedirPermisoNotificaciones();
        }
    }

    private void mostrar(Fragment fragmento, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contenedor, fragmento, tag)
                .commit();
    }

    private void confirmarCerrarSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cerrar_sesion_titulo)
                .setMessage(R.string.cerrar_sesion_mensaje)
                .setPositiveButton(R.string.btn_salir, (d, w) -> {
                    FirebaseAuth.getInstance().signOut();
                    com.facebook.login.LoginManager.getInstance().logOut();
                    irALogin();
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // POST_NOTIFICATIONS es obligatorio desde Android 13; minSdk es 34, así que siempre se pide
    private void pedirPermisoNotificaciones() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}
