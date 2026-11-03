package com.pucp.firepractica.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.pucp.firepractica.R;

/** Gráfico de barras simple: una barra por categoría, con su valor encima. */
public class BarChartView extends View {

    private String[] etiquetas = new String[0];
    private int[] valores = new int[0];
    private int[] colores = new int[0];

    private final Paint pincelBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelEje = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;
        pincelTexto.setTextAlign(Paint.Align.CENTER);
        pincelTexto.setColor(ContextCompat.getColor(context, R.color.texto_principal));
        pincelEje.setColor(ContextCompat.getColor(context, R.color.borde));
        pincelEje.setStrokeWidth(2 * d);
    }

    public void setDatos(String[] etiquetas, int[] valores, int[] colores) {
        this.etiquetas = etiquetas;
        this.valores = valores;
        this.colores = colores;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = valores.length;
        if (n == 0) {
            return;
        }
        float d = getResources().getDisplayMetrics().density;
        float ancho = getWidth();
        float alto = getHeight();
        float margenInferior = 28 * d;
        float margenSuperior = 24 * d;
        float baseY = alto - margenInferior;

        int maximo = 1;
        for (int v : valores) {
            maximo = Math.max(maximo, v);
        }

        float ranura = ancho / n;
        float anchoBarra = ranura * 0.55f;
        float areaUtil = baseY - margenSuperior;

        pincelTexto.setTextSize(13 * d);
        canvas.drawLine(0, baseY, ancho, baseY, pincelEje);

        for (int i = 0; i < n; i++) {
            float centro = ranura * i + ranura / 2f;
            float altoBarra = areaUtil * valores[i] / maximo;
            pincelBarra.setColor(colores[i]);
            rect.set(centro - anchoBarra / 2f, baseY - altoBarra, centro + anchoBarra / 2f, baseY);
            canvas.drawRoundRect(rect, 6 * d, 6 * d, pincelBarra);

            pincelTexto.setFakeBoldText(true);
            canvas.drawText(String.valueOf(valores[i]), centro, baseY - altoBarra - 6 * d, pincelTexto);
            pincelTexto.setFakeBoldText(false);
            canvas.drawText(etiquetas[i], centro, alto - 8 * d, pincelTexto);
        }
    }
}
