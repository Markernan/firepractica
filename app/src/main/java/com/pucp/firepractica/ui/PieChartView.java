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

import java.util.Locale;

/** Gráfico circular (anillo) con el porcentaje de cada categoría y una leyenda. */
public class PieChartView extends View {

    private String[] etiquetas = new String[0];
    private int[] valores = new int[0];
    private int[] colores = new int[0];

    private final Paint pincelArco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelVacio = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ovalo = new RectF();

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        pincelArco.setStyle(Paint.Style.STROKE);
        pincelArco.setStrokeCap(Paint.Cap.BUTT);
        pincelVacio.setStyle(Paint.Style.STROKE);
        pincelVacio.setColor(ContextCompat.getColor(context, R.color.borde));
        pincelTexto.setColor(ContextCompat.getColor(context, R.color.texto_principal));
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
        float d = getResources().getDisplayMetrics().density;
        float ancho = getWidth();
        float alto = getHeight();

        int total = 0;
        for (int v : valores) {
            total += v;
        }

        float diametro = Math.min(alto - 16 * d, ancho * 0.5f);
        float grosor = diametro * 0.22f;
        float cx = diametro / 2f + 8 * d;
        float cy = alto / 2f;
        ovalo.set(cx - diametro / 2f + grosor / 2f, cy - diametro / 2f + grosor / 2f,
                cx + diametro / 2f - grosor / 2f, cy + diametro / 2f - grosor / 2f);
        pincelArco.setStrokeWidth(grosor);
        pincelVacio.setStrokeWidth(grosor);

        if (total == 0) {
            canvas.drawArc(ovalo, 0, 360, false, pincelVacio);
        } else {
            float inicio = -90f;
            for (int i = 0; i < valores.length; i++) {
                if (valores[i] == 0) {
                    continue;
                }
                float barrido = 360f * valores[i] / total;
                pincelArco.setColor(colores[i]);
                canvas.drawArc(ovalo, inicio, barrido, false, pincelArco);
                inicio += barrido;
            }
        }

        // Total al centro
        pincelTexto.setTextAlign(Paint.Align.CENTER);
        pincelTexto.setFakeBoldText(true);
        pincelTexto.setTextSize(26 * d);
        canvas.drawText(String.valueOf(total), cx, cy + 4 * d, pincelTexto);
        pincelTexto.setFakeBoldText(false);
        pincelTexto.setTextSize(11 * d);
        canvas.drawText("total", cx, cy + 20 * d, pincelTexto);

        // Leyenda con porcentajes
        pincelTexto.setTextAlign(Paint.Align.LEFT);
        pincelTexto.setTextSize(14 * d);
        float x = diametro + 32 * d;
        float paso = 30 * d;
        float y = cy - paso * (valores.length - 1) / 2f;
        Paint punto = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < valores.length; i++) {
            punto.setColor(colores[i]);
            canvas.drawCircle(x, y - 4 * d, 6 * d, punto);
            int porcentaje = total == 0 ? 0 : Math.round(100f * valores[i] / total);
            canvas.drawText(String.format(Locale.US, "%s  %d%%", etiquetas[i], porcentaje),
                    x + 16 * d, y, pincelTexto);
            y += paso;
        }
    }
}
