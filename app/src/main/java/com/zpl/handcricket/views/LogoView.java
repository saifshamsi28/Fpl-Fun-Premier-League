package com.zpl.handcricket.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Simple stand-in for the "ZPL HAND CRICKET" top logo badge.
 * (Swap with a real PNG in drawable-nodpi/zpl_logo.png for production.)
 */
public class LogoView extends View {
    private final Paint badge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accent= new Paint(Paint.ANTI_ALIAS_FLAG);

    public LogoView(Context c) { super(c); init(); }
    public LogoView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        badge.setStyle(Paint.Style.FILL);
        badge.setColor(Color.parseColor("#0F3BB0"));
        accent.setColor(Color.parseColor("#E63946"));
        accent.setStyle(Paint.Style.FILL);
        text.setColor(Color.WHITE);
        text.setFakeBoldText(true);
        text.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        RectF r = new RectF(w*0.1f, h*0.1f, w*0.9f, h*0.85f);
        c.drawRoundRect(r, 28, 28, badge);

        text.setTextSize(h * 0.36f);
        c.drawText("FPL", w/2f, h*0.58f, text);
        text.setTextSize(h * 0.14f);
        c.drawText("HAND CRICKET", w/2f, h*0.78f, text);

        // Little red ball dot
        c.drawCircle(w*0.78f, h*0.28f, h*0.09f, accent);
    }
}
