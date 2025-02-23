package com.example.dynamopush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.BlurMaskFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    private Paint backgroundPaint, progressPaint, glowingProgressPaint;
    private RectF rectF;
    private float progress = 0;

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Background paint (transparent)
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0x00000000); // Transparent background
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20);
        backgroundPaint.setAntiAlias(true);

        // Progress paint (solid color #C45673)
        progressPaint = new Paint();
        progressPaint.setColor(0xFFC45673); // Progress color (C45673)
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20);
        progressPaint.setAntiAlias(true);

        // Glowing progress paint (color #EA64D5 for glow effect)
        glowingProgressPaint = new Paint();
        glowingProgressPaint.setColor(0xFFEA64D5); // Glowing color (EA64D5)
        glowingProgressPaint.setStyle(Paint.Style.STROKE);
        glowingProgressPaint.setStrokeWidth(20);
        glowingProgressPaint.setAntiAlias(true);
        glowingProgressPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL)); // Add glow effect
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int size = Math.min(w, h);
        int padding = 20;
        rectF = new RectF(padding, padding, size - padding, size - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background circle
        canvas.drawArc(rectF, -90, 360, false, backgroundPaint);

        // Draw glowing progress arc
        float angle = (progress / 100) * 360;
        if (progress > 0) {
            canvas.drawArc(rectF, -90, -angle, false, glowingProgressPaint);
        }

        // Draw solid progress arc
        canvas.drawArc(rectF, -90, -angle, false, progressPaint);
    }

    // Public method to update progress
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // Redraw the view
    }
}

