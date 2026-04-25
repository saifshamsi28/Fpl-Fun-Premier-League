package com.zpl.handcricket.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Stylized stadium backdrop used behind the game pitch.
 */
public class StadiumView extends View {

    private final Paint sky   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stand = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint roof  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public StadiumView(Context c) { super(c); init(); }
    public StadiumView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        stand.setColor(Color.parseColor("#1D4ED8"));
        roof.setColor(Color.parseColor("#0F3BB0"));
        line.setColor(Color.WHITE);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(3f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        sky.setShader(new LinearGradient(0, 0, 0, h,
                Color.parseColor("#7DB4F8"), Color.parseColor("#1D4ED8"),
                Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        c.drawRect(0, 0, w, h, sky);
        // Roof band
        c.drawRect(new RectF(0, h*0.3f, w, h*0.45f), roof);
        // Stand rows
        c.drawRect(new RectF(0, h*0.45f, w, h), stand);
        for (int i = 1; i < 8; i++) {
            float y = h*0.45f + i * (h*0.55f/8f);
            c.drawLine(0, y, w, y, line);
        }
    }
}
