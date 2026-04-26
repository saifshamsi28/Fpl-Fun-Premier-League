package com.zpl.handcricket.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Premium animated hand view with smooth finger extension transitions.
 *
 * Finger index mapping: 0=index, 1=middle, 2=ring, 3=pinky, 4=thumb
 * Pose matrix (currentExt[5]):
 *   n=0  -> {0,0,0,0,0}  fist/rock
 *   n=1  -> {1,0,0,0,0}
 *   n=2  -> {1,1,0,0,0}
 *   n=3  -> {1,1,1,0,0}
 *   n=4  -> {1,1,1,1,0}
 *   n=5  -> {1,1,1,1,1}  open palm
 *   n=6  -> {0,0,0,1,1}  shaka
 *
 * All Paint / RectF objects are pre-allocated - zero GC in onDraw.
 */
public class HandView extends View {

    public static final int COLOR_BLUE = Color.parseColor("#1D4ED8");
    public static final int COLOR_RED  = Color.parseColor("#E63946");

    private static final float[][] POSES = {
            {0, 0, 0, 0, 0},   // 0 - fist
            {1, 0, 0, 0, 0},   // 1
            {1, 1, 0, 0, 0},   // 2
            {1, 1, 1, 0, 0},   // 3
            {1, 1, 1, 1, 0},   // 4
            {1, 1, 1, 1, 1},   // 5
            {0, 0, 0, 1, 1},   // 6 - shaka
    };

    private int handColor = COLOR_BLUE;
    private boolean mirrored = false;
    private String label = "";

    // Per-finger animated extension values (0=retracted, 1=fully extended, clamped in draw)
    private final float[] currentExt   = new float[5];
    private final float[] startExt     = new float[5];
    private final float[] targetExt    = new float[5];

    private ValueAnimator fingerAnimator;

    // Pre-allocated drawing objects (no new() in onDraw)
    private final Paint palmPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fingerPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF palmRect    = new RectF();
    private final RectF fingerRect  = new RectF();
    private final RectF wristRect   = new RectF();
    private final RectF thumbRect   = new RectF();
    private final RectF hlRect      = new RectF();

    private LinearGradient palmShader;
    private LinearGradient fingerShader;
    private int lastW, lastH;

    public HandView(Context c) { super(c); init(); }
    public HandView(Context c, AttributeSet a) { super(c, a); init(); }
    public HandView(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        palmPaint.setStyle(Paint.Style.FILL);
        fingerPaint.setStyle(Paint.Style.FILL);

        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3.5f);
        strokePaint.setColor(Color.argb(130, 255, 255, 255));

        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(Color.argb(55, 255, 255, 255));

        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(50, 0, 0, 0));
    }

    // ---------------------------------------------
    // Public API
    // ---------------------------------------------

    public void setHandColor(int color) {
        this.handColor = color;
        rebuildShaders(getWidth(), getHeight());
        invalidate();
    }

    public void setMirrored(boolean m) { this.mirrored = m; invalidate(); }

    public void setFingers(int n) {
        n = Math.max(0, Math.min(6, n));
        System.arraycopy(currentExt, 0, startExt, 0, 5);
        System.arraycopy(POSES[n], 0, targetExt, 0, 5);

        if (fingerAnimator != null) fingerAnimator.cancel();

        fingerAnimator = ValueAnimator.ofFloat(0f, 1f);
        fingerAnimator.setDuration(300);
        fingerAnimator.setInterpolator(new OvershootInterpolator(1.6f));
        fingerAnimator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            for (int i = 0; i < 5; i++) {
                currentExt[i] = startExt[i] + (targetExt[i] - startExt[i]) * t;
            }
            invalidate();
        });
        fingerAnimator.start();
    }

    public void setLabel(String s) { this.label = s == null ? "" : s; invalidate(); }

    // ---------------------------------------------
    // Shaders (created once per size/color change)
    // ---------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        rebuildShaders(w, h);
    }

    private void rebuildShaders(int w, int h) {
        if (w == 0 || h == 0) return;
        lastW = w; lastH = h;

        float cx = w / 2f;
        float palmTop = h * 0.34f;
        float palmBot = h * 0.82f;

        int lightColor = lighten(handColor, 0.30f);
        int darkColor  = darken(handColor, 0.20f);

        palmShader = new LinearGradient(
                cx, palmTop, cx, palmBot,
                lightColor, darkColor, Shader.TileMode.CLAMP);

        // Fingers slightly lighter at tip, same at base
        float fingerTop = palmTop - h * 0.36f;
        fingerShader = new LinearGradient(
                cx, fingerTop, cx, palmTop,
                lightColor, handColor, Shader.TileMode.CLAMP);

        palmPaint.setShader(palmShader);
        fingerPaint.setShader(fingerShader);
    }

    // ---------------------------------------------
    // Drawing
    // ---------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // Rebuild if size changed but onSizeChanged missed (rare)
        if (w != lastW || h != lastH) rebuildShaders(w, h);

        canvas.save();
        if (mirrored) canvas.scale(-1f, 1f, w / 2f, h / 2f);

        float palmL = w * 0.10f;
        float palmR = w * 0.90f;
        float palmT = h * 0.34f;
        float palmB = h * 0.82f;
        float palmW = palmR - palmL;
        float palmH = palmB - palmT;
        float cr     = palmW * 0.22f;  // palm corner radius

        // -- Wrist --
        wristRect.set(palmL + palmW * 0.12f, palmB - h * 0.06f,
                      palmR - palmW * 0.12f, h * 0.97f);
        canvas.drawRoundRect(wristRect, h * 0.05f, h * 0.05f, shadowPaint);
        palmPaint.setShader(palmShader);
        canvas.drawRoundRect(wristRect, h * 0.05f, h * 0.05f, palmPaint);

        // -- Four main fingers (index, middle, ring, pinky) --
        float totalFingerW = palmW * 0.88f;
        float fingerW      = totalFingerW / 4.4f;
        float fingerGap    = (totalFingerW - fingerW * 4) / 3f;
        float maxExt       = palmH * 0.55f;

        for (int i = 0; i < 4; i++) {
            float ext = clamp(currentExt[i]);
            float fx  = palmL + palmW * 0.06f + i * (fingerW + fingerGap);
            float topY = palmT - ext * maxExt;
            float botY = palmT + palmH * 0.10f;

            fingerRect.set(fx, topY, fx + fingerW, botY);
            float fr = fingerW * 0.44f;

            // Finger fill
            fingerPaint.setShader(fingerShader);
            canvas.drawRoundRect(fingerRect, fr, fr, fingerPaint);
            // Finger stroke
            canvas.drawRoundRect(fingerRect, fr, fr, strokePaint);
        }

        // -- Thumb --
        float thumbExt = clamp(currentExt[4]);
        if (thumbExt > 0.01f) {
            float thumbLen = (palmW * 0.35f + palmH * 0.22f) * thumbExt;
            float thumbFat = palmH * 0.15f;
            float thumbY   = palmT + palmH * 0.10f;
            thumbRect.set(palmL - thumbLen, thumbY,
                          palmL + palmW * 0.05f, thumbY + thumbFat);
            float tr = thumbFat * 0.44f;
            fingerPaint.setShader(fingerShader);
            canvas.drawRoundRect(thumbRect, tr, tr, fingerPaint);
            canvas.drawRoundRect(thumbRect, tr, tr, strokePaint);
        }

        // -- Palm body (drawn on top of finger bases) --
        palmRect.set(palmL, palmT, palmR, palmB);
        palmPaint.setShader(palmShader);
        canvas.drawRoundRect(palmRect, cr, cr, palmPaint);
        canvas.drawRoundRect(palmRect, cr, cr, strokePaint);

        // -- Specular highlight (top-left of palm) --
        hlRect.set(palmL + palmW * 0.12f, palmT + palmH * 0.07f,
                   palmL + palmW * 0.48f, palmT + palmH * 0.30f);
        canvas.drawOval(hlRect, highlightPaint);

        canvas.restore();

        // -- Label --
        if (!label.isEmpty()) {
            labelPaint.setTextSize(h * 0.17f);
            float baseline = palmB - palmH * 0.28f;
            canvas.drawText(label, w / 2f, baseline, labelPaint);
        }
    }

    // ---------------------------------------------
    // Helpers
    // ---------------------------------------------

    private static float clamp(float v) {
        return v < 0f ? 0f : Math.min(v, 1f);
    }

    private static int lighten(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(1f, hsv[2] * (1f + factor));
        return Color.HSVToColor(hsv);
    }

    private static int darken(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.max(0f, hsv[2] * (1f - factor));
        return Color.HSVToColor(hsv);
    }
}
