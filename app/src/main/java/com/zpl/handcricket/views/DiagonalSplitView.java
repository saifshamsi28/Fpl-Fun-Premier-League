package com.zpl.handcricket.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Background view that paints the blue-top-left / red-bottom-right diagonal split
 * seen in the matchmaking and "player matched" screens.
 */
public class DiagonalSplitView extends View {
    private final Paint bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint redPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DiagonalSplitView(Context ctx) { super(ctx); init(); }
    public DiagonalSplitView(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public DiagonalSplitView(Context ctx, AttributeSet a, int d) { super(ctx, a, d); init(); }

    private void init() {
        bluePaint.setColor(Color.parseColor("#1D4ED8"));
        bluePaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.parseColor("#E63946"));
        redPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        // Blue triangle top-left
        Path blue = new Path();
        blue.moveTo(0, 0); blue.lineTo(w, 0); blue.lineTo(0, h); blue.close();
        c.drawPath(blue, bluePaint);
        // Red triangle bottom-right
        Path red = new Path();
        red.moveTo(w, 0); red.lineTo(w, h); red.lineTo(0, h); red.close();
        c.drawPath(red, redPaint);
    }
}
