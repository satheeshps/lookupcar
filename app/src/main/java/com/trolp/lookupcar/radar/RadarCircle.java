package com.trolp.lookupcar.radar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by leonidas on 6/20/15.
 */
class RadarCircle {
    private static final int INIT_ALPHA = 100;
    private static final double INIT_RADIUS = 40.0;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private double maxRadius;
    private int delay;
    private double radius;
    private int alpha;
    private int iters = 0;
    float x,y;

    RadarCircle(float x,float y, double maxRadius) {
        this(x, y, maxRadius, 0, Color.GREEN);
    }

    RadarCircle(float x,float y, double maxRadius, int delay) {
        this(x, y, maxRadius, delay, Color.GREEN);
    }

    RadarCircle(float x,float y, double maxRadius, int delay, int color) {
        this.x = x;
        this.y = y;
        this.delay = delay;
        this.maxRadius = maxRadius;
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        init();
    }

    void grow(Canvas canvas) {
        if(delay < iters) {
            double margin = radius - (x - 20);
            radius += 5;
            alpha -= Math.abs(margin)/(x - 20);
            if(alpha < 0)
                alpha = 0;

            paint.setAlpha(alpha);
            canvas.drawCircle(x, y, (float)radius, paint);

            if(maxRadius <= radius) {
                init();
            }
        }
        iters++;
    }

    void init() {
        radius = INIT_RADIUS;
        alpha = INIT_ALPHA;
        iters = 0;
    }

    public double getMaxRadius() {
        return maxRadius;
    }
}
