package com.pucp.firepractica.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.pucp.firepractica.R;
import com.pucp.firepractica.databinding.ActivityRegistroBinding;
import com.pucp.firepractica.ui.MainActivity;

/** Registro de un usuario nuevo con correo y contraseña. */
public class RegistroActivity extends AppCompatActivity {

    private static final int LARGO_MINIMO = 6;

    private ActivityRegistroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegistrarme.setOnClickListener(v -> registrar());
        binding.tvVolverLogin.setOnClickListener(v -> finish());
    }

    private void registrar() {
        binding.tilCorreo.setError(null);
        binding.tilContrasena.setError(null);
        binding.tilConfirmar.setError(null);

        String correo = binding.etCorreo.getText() == null ? "" : binding.etCorreo.getText().toString().trim();
        String contrasena = binding.etContrasena.getText() == null ? "" : binding.etContrasena.getText().toString();
        String confirmar = binding.etConfirmar.getText() == null ? "" : binding.etConfirmar.getText().toString();

        if (correo.isEmpty()) {
            binding.tilCorreo.setError(getString(R.string.error_correo_vacio));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError(getString(R.string.error_correo_invalido));
            return;
        }
        if (contrasena.length() < LARGO_MINIMO) {
            binding.tilContrasena.setError(getString(R.string.error_contrasena_corta));
            return;
        }
        if (!contrasena.equals(confirmar)) {
            binding.tilConfirmar.setError(getString(R.string.error_contrasenas_distintas));
            return;
        }

        cargando(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, R.string.registro_exitoso, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    cargando(false);
                    Toast.makeText(this, AuthErrores.mensaje(this, e), Toast.LENGTH_LONG).show();
                });
    }

    private void cargando(boolean activo) {
        binding.progreso.setVisibility(activo ? View.VISIBLE : View.GONE);
        binding.btnRegistrarme.setEnabled(!activo);
    }
}
