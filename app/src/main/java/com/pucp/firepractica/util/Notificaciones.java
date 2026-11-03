package com.pucp.firepractica.util;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pucp.firepractica.R;
import com.pucp.firepractica.ui.MainActivity;

/** Notificaciones del sistema al registrar o actualizar un pronóstico. */
public class Notificaciones {

    private static final String CANAL = "pronosticos";
    private static final int ID_REGISTRADO = 1;
    private static final int ID_ACTUALIZADO = 2;

    private final Context context;
    private final NotificationManagerCompat manager;

    public Notificaciones(Context context) {
        this.context = context.getApplicationContext();
        this.manager = NotificationManagerCompat.from(this.context);
        NotificationChannel canal = new NotificationChannel(CANAL,
                this.context.getString(R.string.canal_pronosticos), NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription(this.context.getString(R.string.canal_pronosticos_desc));
        this.context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    public void pronosticoRegistrado(String a, String b) {
        mostrar(ID_REGISTRADO, R.string.notif_registrado_titulo,
                context.getString(R.string.notif_registrado_texto, a, b));
    }

    public void pronosticoActualizado(String a, String b) {
        mostrar(ID_ACTUALIZADO, R.string.notif_actualizado_titulo,
                context.getString(R.string.notif_actualizado_texto, a, b));
    }

    // areNotificationsEnabled() es false si el usuario negó POST_NOTIFICATIONS: así se evita un SecurityException.
    @SuppressLint("MissingPermission")
    private void mostrar(int id, int titulo, String texto) {
        if (!manager.areNotificationsEnabled()) {
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, id, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        manager.notify(id, new NotificationCompat.Builder(context, CANAL)
                .setSmallIcon(R.drawable.ic_logo_mundial)
                .setContentTitle(context.getString(titulo))
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build());
    }
}
