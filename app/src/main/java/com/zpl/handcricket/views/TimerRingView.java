package com.zpl.handcricket.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Black circle with white number countdown (matches the timer in the game screen).
 */
public class TimerRingView extends View {

    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int displayNumber = 3;
    private float progress = 1f; // 1 -> full, 0 -> empty

    private ValueAnimator animator;

    public TimerRingView(Context c) { super(c); init(); }
    public TimerRingView(Context c, AttributeSet a) { super(c, a); init(); }
    public TimerRingView(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    private void init() {
        circlePaint.setColor(Color.parseColor("#0E1A3B"));
        circlePaint.setStyle(Paint.Style.FILL);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(6f);
        progressPaint.setColor(Color.parseColor("#F59E0B"));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(8f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void startCountdown(int seconds, int secondsDisplayed) {
        if (animator != null) animator.cancel();
        displayNumber = secondsDisplayed;
        progress = 1f;
        animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(seconds * 1000L);
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            float elapsedFrac = 1f - progress;
            int newDisplay = Math.max(0, secondsDisplayed - (int)(elapsedFrac * secondsDisplayed));
            if (newDisplay != displayNumber) {
                displayNumber = newDisplay;
            }
            invalidate();
        });
        animator.start();
    }

    public void startCountdownSequence(long totalDurationMs, int... sequence) {
        if (sequence == null || sequence.length == 0) return;
        if (animator != null) animator.cancel();
        displayNumber = sequence[0];
        progress = 1f;
        animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(totalDurationMs);
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            float elapsed = 1f - progress;
            int index = Math.min(sequence.length - 1,
                    (int) Math.floor(elapsed * sequence.length));
            displayNumber = sequence[index];
            invalidate();
        });
        animator.start();
    }

    public void setNumber(int n) {
        if (animator != null) animator.cancel();
        displayNumber = n;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        float r = Math.min(w, h) / 2f - 8;
        float cx = w / 2f, cy = h / 2f;
        c.drawCircle(cx, cy, r, circlePaint);
        c.drawCircle(cx, cy, r, strokePaint);

        RectF arc = new RectF(cx - r, cy - r, cx + r, cy + r);
        c.drawArc(arc, -90f, 360f * progress, false, progressPaint);

        textPaint.setTextSize(r * 1.1f);
        float baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
        c.drawText(String.valueOf(displayNumber), cx, baseline, textPaint);
    }
}
