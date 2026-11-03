package com.pucp.firepractica.auth;

import android.content.Context;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.pucp.firepractica.R;

/** Traduce las excepciones de Firebase Auth a mensajes en español. */
final class AuthErrores {

    private AuthErrores() {
    }

    static String mensaje(Context context, Exception e) {
        if (e instanceof FirebaseNetworkException) {
            return context.getString(R.string.error_sin_red);
        }
        if (e instanceof FirebaseAuthInvalidUserException
                || e instanceof FirebaseAuthInvalidCredentialsException) {
            return context.getString(R.string.error_credenciales);
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            FirebaseAuthUserCollisionException colision = (FirebaseAuthUserCollisionException) e;
            // ERROR_EMAIL_ALREADY_IN_USE: registro con un correo ya usado.
            // ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL: el correo ya existe con otro método de acceso.
            return "ERROR_EMAIL_ALREADY_IN_USE".equals(colision.getErrorCode())
                    ? context.getString(R.string.error_correo_en_uso)
                    : context.getString(R.string.error_social_cuenta_existente);
        }
        if (e instanceof FirebaseAuthException) {
            String codigo = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_INVALID_CREDENTIAL".equals(codigo) || "ERROR_WRONG_PASSWORD".equals(codigo)
                    || "ERROR_USER_NOT_FOUND".equals(codigo)) {
                return context.getString(R.string.error_credenciales);
            }
        }
        return context.getString(R.string.error_autenticacion);
    }
}
