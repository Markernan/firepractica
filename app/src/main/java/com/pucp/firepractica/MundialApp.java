package com.pucp.firepractica;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MundialApp extends Application {

    private static final String HOST_EMULADOR = "10.0.2.2";

    private static FirebaseDatabase baseDeDatos;

    @Override
    public void onCreate() {
        super.onCreate();
        baseDeDatos = FirebaseDatabase.getInstance();
        // Solo con -PuseEmulator=true (pruebas sin credenciales). En el flujo normal esto no se ejecuta.
        if (BuildConfig.USE_EMULATOR) {
            FirebaseAuth.getInstance().useEmulator(HOST_EMULADOR, 9099);
            baseDeDatos.useEmulator(HOST_EMULADOR, 9000);
        }
    }

    /**
     * Única instancia de Realtime Database de toda la app. Se guarda aquí porque, con useEmulator,
     * volver a llamar FirebaseDatabase.getInstance() puede devolver una instancia distinta sin configurar.
     */
    public static FirebaseDatabase baseDeDatos() {
        return baseDeDatos;
    }
}
