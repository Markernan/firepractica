package com.pucp.firepractica.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class Fechas {

    private static final String FORMATO = "dd/MM/yyyy";

    private Fechas() {
    }

    public static String formatear(long millis) {
        return new SimpleDateFormat(FORMATO, new Locale("es", "PE")).format(millis);
    }

    /** Medianoche local del día indicado (mes de 0 a 11, como en DatePicker). */
    public static long medianoche(int anio, int mes, int dia) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(anio, mes, dia, 0, 0, 0);
        return c.getTimeInMillis();
    }
}
