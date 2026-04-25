package com.zpl.handcricket.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Stylized foam-glove hand that renders 0..6 extended fingers.
 *   0 = closed fist (rock) — used during timer
 *   5 = all fingers extended (palm)
 *   6 = "hang loose" (thumb + pinky)
 * Mirroring & color are configurable.
 */
public class HandView extends View {

    public static final int COLOR_BLUE = Color.parseColor("#1D4ED8");
    public static final int COLOR_RED  = Color.parseColor("#E63946");

    private int fingers = 0;
    private int handColor = COLOR_BLUE;
    private boolean mirrored = false;
    private String label = "";

    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint skinPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public HandView(Context c) { super(c); init(); }
    public HandView(Context c, AttributeSet a) { super(c, a); init(); }
    public HandView(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(6f);
        strokePaint.setColor(Color.WHITE);
        skinPaint.setStyle(Paint.Style.FILL);
        skinPaint.setColor(Color.parseColor("#F4B183"));
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
    }

    public void setHandColor(int color) { this.handColor = color; fillPaint.setColor(color); invalidate(); }
    public void setMirrored(boolean m) { this.mirrored = m; invalidate(); }
    public void setFingers(int f) { this.fingers = Math.max(0, Math.min(6, f)); invalidate(); }
    public void setLabel(String s) { this.label = s == null ? "" : s; invalidate(); }

    @Override
    protected void onDraw(Canvas c) {
        fillPaint.setColor(handColor);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        c.save();
        if (mirrored) { c.scale(-1f, 1f, w / 2f, h / 2f); }

        // Wrist / forearm
        RectF wrist = new RectF(w*0.08f, h*0.78f, w*0.52f, h*0.98f);
        c.drawRoundRect(wrist, 40, 40, skinPaint);

        // Palm (rounded rect) - foam glove look
        RectF palm = new RectF(w*0.18f, h*0.40f, w*0.78f, h*0.86f);
        c.drawRoundRect(palm, 60, 60, fillPaint);
        c.drawRoundRect(palm, 60, 60, strokePaint);

        // Fingers: position five slots + thumb
        // Index through pinky at top
        float slotW = (palm.width() - 40) / 4f;
        float fingerTop = palm.top - h*0.30f;
        float fingerBot = palm.top + 20;
        boolean[] show = new boolean[5]; // thumb, index, middle, ring, pinky
        switch (fingers) {
            case 0: break;
            case 1: show[1] = true; break;
            case 2: show[1] = show[2] = true; break;
            case 3: show[1] = show[2] = show[3] = true; break;
            case 4: show[1] = show[2] = show[3] = show[4] = true; break;
            case 5: show[0] = show[1] = show[2] = show[3] = show[4] = true; break;
            case 6: show[0] = show[4] = true; break; // hang-loose
        }
        // four non-thumb fingers
        for (int i = 0; i < 4; i++) {
            float x = palm.left + 20 + i*slotW;
            RectF f = new RectF(x, fingerTop, x + slotW - 10, fingerBot);
            if (show[i+1]) {
                c.drawRoundRect(f, 30, 30, fillPaint);
                c.drawRoundRect(f, 30, 30, strokePaint);
            } else {
                // knuckle bump
                RectF knuck = new RectF(f.left, palm.top - 15, f.right, palm.top + 25);
                c.drawRoundRect(knuck, 20, 20, fillPaint);
            }
        }
        // thumb (left side)
        if (show[0]) {
            RectF t = new RectF(palm.left - w*0.18f, palm.top + h*0.05f,
                    palm.left + 20, palm.top + h*0.25f);
            c.drawRoundRect(t, 40, 40, fillPaint);
            c.drawRoundRect(t, 40, 40, strokePaint);
        }

        c.restore();

        // Label (number) centered on palm
        if (!label.isEmpty()) {
            labelPaint.setTextSize(h * 0.22f);
            c.drawText(label, w/2f, h*0.70f, labelPaint);
        }
    }
}
