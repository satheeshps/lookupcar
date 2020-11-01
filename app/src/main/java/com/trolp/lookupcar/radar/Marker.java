package com.trolp.lookupcar.radar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by leonidas on 6/20/15.
 */
class Marker {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static float CIRCLE_DISTANCE_MIN = 10;
    private int markerRadius;
    private float lastBoundaryRadius = -1;
    private float lastMaxDistance = CIRCLE_DISTANCE_MIN;

    Marker(int rad) {
        this.markerRadius = rad;
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
    }

    public void pin(int x, int y, float radians, float radius, float distance, Canvas canvas) {
        if(CIRCLE_DISTANCE_MIN < distance || lastBoundaryRadius == -1) {
            if(CIRCLE_DISTANCE_MIN < distance)
                lastMaxDistance = distance;
            lastBoundaryRadius = radius;
        }
        double currentDistInRadar = lastBoundaryRadius * distance / lastMaxDistance;
        float yPos = y - (float) (Math.sin(radians) * currentDistInRadar);
        float xPos = (float) (Math.cos(radians) * currentDistInRadar) + x;

        canvas.drawCircle(xPos, yPos, markerRadius, paint);
    }
}
