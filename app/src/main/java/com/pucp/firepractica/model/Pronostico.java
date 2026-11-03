package com.pucp.firepractica.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

/** Un pronóstico de partido. Se guarda en pronosticos/{uid}/{id}. */
@IgnoreExtraProperties
public class Pronostico implements Serializable {

    public static final String PENDIENTE = "Pendiente";
    public static final String ACERTADO = "Acertado";
    public static final String FALLADO = "Fallado";

    private String id;
    private String seleccionA;
    private String seleccionB;
    /** Fecha del partido en milisegundos (medianoche local). */
    private long fechaPartido;
    private int golesA;
    private int golesB;
    private String estado = PENDIENTE;
    private long creadoEn;

    /** Requerido por Firebase. */
    public Pronostico() {
    }

    public Pronostico(String seleccionA, String seleccionB, long fechaPartido, int golesA, int golesB) {
        this.seleccionA = seleccionA;
        this.seleccionB = seleccionB;
        this.fechaPartido = fechaPartido;
        this.golesA = golesA;
        this.golesB = golesB;
        this.estado = PENDIENTE;
        this.creadoEn = System.currentTimeMillis();
    }

    @Exclude
    public String getId() { return id; }
    @Exclude
    public void setId(String id) { this.id = id; }

    public String getSeleccionA() { return seleccionA; }
    public void setSeleccionA(String seleccionA) { this.seleccionA = seleccionA; }

    public String getSeleccionB() { return seleccionB; }
    public void setSeleccionB(String seleccionB) { this.seleccionB = seleccionB; }

    public long getFechaPartido() { return fechaPartido; }
    public void setFechaPartido(long fechaPartido) { this.fechaPartido = fechaPartido; }

    public int getGolesA() { return golesA; }
    public void setGolesA(int golesA) { this.golesA = golesA; }

    public int getGolesB() { return golesB; }
    public void setGolesB(int golesB) { this.golesB = golesB; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getCreadoEn() { return creadoEn; }
    public void setCreadoEn(long creadoEn) { this.creadoEn = creadoEn; }

    /** Solo un pronóstico pendiente se puede editar o eliminar; Acertado/Fallado queda bloqueado. */
    @Exclude
    public boolean esPendiente() {
        return PENDIENTE.equals(estado);
    }
}
