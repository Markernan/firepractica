package com.pucp.firepractica.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.pucp.firepractica.R;
import com.pucp.firepractica.databinding.ActivityLoginBinding;
import com.pucp.firepractica.databinding.DialogRecuperarBinding;
import com.pucp.firepractica.ui.MainActivity;

import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * Inicio de sesión: correo y contraseña, recuperación de contraseña y proveedores
 * externos (Google, GitHub y Facebook). Si ya hay sesión, entra directo.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private CallbackManager callbackFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            irAPrincipal();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnIniciarSesion.setOnClickListener(v -> iniciarConCorreo());
        binding.tvRegistrate.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        binding.tvOlvide.setOnClickListener(v -> mostrarRecuperarContrasena());
        binding.btnGoogle.setOnClickListener(v -> iniciarConGoogle());
        binding.btnGithub.setOnClickListener(v -> iniciarConProveedor("github.com"));
        binding.btnFacebook.setOnClickListener(v -> iniciarConFacebook());
        prepararFacebook();
    }

    // ------------------------------------------------------------ correo y contraseña

    private void iniciarConCorreo() {
        binding.tilCorreo.setError(null);
        binding.tilContrasena.setError(null);

        String correo = texto(binding.etCorreo.getText());
        String contrasena = binding.etContrasena.getText() == null ? "" : binding.etContrasena.getText().toString();

        if (correo.isEmpty()) {
            binding.tilCorreo.setError(getString(R.string.error_correo_vacio));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError(getString(R.string.error_correo_invalido));
            return;
        }
        if (contrasena.isEmpty()) {
            binding.tilContrasena.setError(getString(R.string.error_contrasena_vacia));
            return;
        }

        cargando(true);
        auth.signInWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener(r -> irAPrincipal())
                .addOnFailureListener(this::mostrarErrorAuth);
    }

    private void mostrarRecuperarContrasena() {
        DialogRecuperarBinding dialogo = DialogRecuperarBinding.inflate(getLayoutInflater());
        dialogo.etCorreoRecuperar.setText(texto(binding.etCorreo.getText()));

        androidx.appcompat.app.AlertDialog alerta = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.recuperar_titulo)
                .setView(dialogo.getRoot())
                .setPositiveButton(R.string.btn_enviar, null)
                .setNegativeButton(R.string.btn_cancelar, null)
                .create();
        alerta.show();

        // Se sobreescribe el click para que el diálogo no se cierre si el correo es inválido.
        alerta.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String correo = texto(dialogo.etCorreoRecuperar.getText());
            if (correo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                dialogo.tilCorreoRecuperar.setError(getString(R.string.error_correo_invalido));
                return;
            }
            auth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener(t -> {
                        // Mismo mensaje exista o no el correo, para no revelar qué cuentas existen.
                        Toast.makeText(this, R.string.recuperar_enviado, Toast.LENGTH_LONG).show();
                        alerta.dismiss();
                    });
        });
    }

    // ------------------------------------------------------------ proveedores externos

    /** Google, con Credential Manager y el cliente web que genera google-services.json. */
    private void iniciarConGoogle() {
        cargando(true);
        GetGoogleIdOption opcion = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();
        GetCredentialRequest solicitud = new GetCredentialRequest.Builder()
                .addCredentialOption(opcion)
                .build();

        CredentialManager.create(this).getCredentialAsync(this, solicitud, null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse respuesta) {
                        if (respuesta.getCredential() instanceof CustomCredential
                                && GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                .equals(respuesta.getCredential().getType())) {
                            GoogleIdTokenCredential google = GoogleIdTokenCredential
                                    .createFrom(respuesta.getCredential().getData());
                            AuthCredential credencial = GoogleAuthProvider.getCredential(google.getIdToken(), null);
                            runOnUiThread(() -> auth.signInWithCredential(credencial)
                                    .addOnSuccessListener(r -> irAPrincipal())
                                    .addOnFailureListener(LoginActivity.this::mostrarErrorAuth));
                        } else {
                            runOnUiThread(() -> errorSocial(getString(R.string.error_autenticacion)));
                        }
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        runOnUiThread(() -> errorSocial(e instanceof GetCredentialCancellationException
                                ? getString(R.string.error_social_cancelado)
                                : getString(R.string.error_autenticacion)));
                    }
                });
    }

    /**
     * Facebook: Firebase no permite su flujo OAuth genérico (términos de Facebook), así que se usa el
     * SDK oficial para obtener el token y luego se entrega a Firebase como credencial.
     */
    private void prepararFacebook() {
        callbackFacebook = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackFacebook, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult resultado) {
                AccessToken token = resultado.getAccessToken();
                AuthCredential credencial = FacebookAuthProvider.getCredential(token.getToken());
                auth.signInWithCredential(credencial)
                        .addOnSuccessListener(r -> irAPrincipal())
                        .addOnFailureListener(LoginActivity.this::mostrarErrorAuth);
            }

            @Override
            public void onCancel() {
                errorSocial(getString(R.string.error_social_cancelado));
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                errorSocial(getString(R.string.error_autenticacion));
            }
        });
    }

    private void iniciarConFacebook() {
        cargando(true);
        LoginManager.getInstance().logInWithReadPermissions(this, callbackFacebook,
                Arrays.asList("public_profile"));
    }

    /** GitHub: flujo OAuth web administrado por Firebase Auth. */
    private void iniciarConProveedor(String idProveedor) {
        cargando(true);
        OAuthProvider.Builder proveedor = OAuthProvider.newBuilder(idProveedor);
        if ("github.com".equals(idProveedor)) {
            proveedor.addCustomParameter("allow_signup", "true");
        }
        Task<AuthResult> pendiente = auth.getPendingAuthResult();
        if (pendiente != null) {
            pendiente.addOnSuccessListener(r -> irAPrincipal()).addOnFailureListener(this::mostrarErrorAuth);
            return;
        }
        auth.startActivityForSignInWithProvider(this, proveedor.build())
                .addOnSuccessListener(r -> irAPrincipal())
                .addOnFailureListener(this::mostrarErrorAuth);
    }

    // ------------------------------------------------------------ utilidades

    private void errorSocial(String mensaje) {
        cargando(false);
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void mostrarErrorAuth(Exception e) {
        cargando(false);
        Toast.makeText(this, AuthErrores.mensaje(this, e), Toast.LENGTH_LONG).show();
    }

    private void cargando(boolean activo) {
        binding.progreso.setVisibility(activo ? View.VISIBLE : View.GONE);
        binding.btnIniciarSesion.setEnabled(!activo);
        binding.btnGoogle.setEnabled(!activo);
        binding.btnGithub.setEnabled(!activo);
        binding.btnFacebook.setEnabled(!activo);
    }

    private void irAPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String texto(CharSequence valor) {
        return valor == null ? "" : valor.toString().trim();
    }
}
