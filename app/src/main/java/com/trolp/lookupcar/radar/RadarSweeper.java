package com.trolp.lookupcar.radar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationManager;

import com.trolp.lookupcar.recorder.Recorder;
import com.trolp.lookupcar.tracker.Tracker;

import java.text.DecimalFormat;

/**
 * Created by leonidas on 6/20/15.
 */
class RadarSweeper {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int x;
    private int y;
    private Marker marker;

    private class BearingDistancePair {
        private double iBearing;
        private double fBearing;
        private double distance;

        BearingDistancePair(double iBearing, double fBearing, double distance) {
            this.iBearing = iBearing;
            this.fBearing = fBearing;
            this.distance = distance;
        }

        public double getInitialBearing() {
            return iBearing;
        }

        public double getFinalBearing() {
            return fBearing;
        }

        public double getDistance() {
            return distance;
        }
    }

    RadarSweeper(int x, int y) {
        this.x = x;
        this.y = y;
        marker = new Marker(10);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(40);
    }

    private Location getDes() {
        Recorder.ParkData data = Recorder.getRecorder().play();
        Location des = null;
        if(data != null && data.getCoordinate() != null) {
            des = new Location(LocationManager.GPS_PROVIDER);
            des.setLatitude(data.getCoordinate().getLatitude());
            des.setLongitude(data.getCoordinate().getLongitude());
        }
        return des;
    }

    private Location getSrc() {
//        Location src = null;
//        if(Tracker.getTracker().getCoordinate() != null) {
//            src = new Location(LocationManager.GPS_PROVIDER);
//            src.setLatitude(Tracker.getTracker().getCoordinate().getLatitude());
//            src.setLongitude(Tracker.getTracker().getCoordinate().getLongitude());
//        }
//        return src;
        return Tracker.getTracker().getLocation();
    }

    private double toDegs(double radians) {
        return radians * 180 / Math.PI;
    }

    private double toRads(double degrees) {
        return degrees * Math.PI / 180;
    }

    private BearingDistancePair getBearingAndDistance(Location src, Location des) {
        double x1 = src.getLatitude();
        double y1 = src.getLongitude();
        double x2 = des.getLatitude();
        double y2 = des.getLongitude();

        // WGS84 datum
        double a = 6378137;
        double b = 6356752.314245;
        double f = 1 / 298.257223563;

        double L = y2 - y1;
        double tanU1 = (1-f) * Math.tan(x1), cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1 * cosU1;
        double tanU2 = (1-f) * Math.tan(x2), cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2)), sinU2 = tanU2 * cosU2;
        double sinY, cosY, sinSqσ, sinσ, cosσ, σ, sinα, cosSqα, cos2σM, C;
        double Y = L, deltaY, iterations = 0;

        do {
            sinY = Math.sin(Y);
            cosY = Math.cos(Y);
            sinSqσ = (cosU2*sinY) * (cosU2*sinY) + (cosU1*sinU2-sinU1*cosU2*cosY) * (cosU1*sinU2-sinU1*cosU2*cosY);
            sinσ = Math.sqrt(sinSqσ);

            if (sinσ == 0) return null;  // co-incident points
            cosσ = sinU1*sinU2 + cosU1*cosU2*cosY;
            σ = Math.atan2(sinσ, cosσ);
            sinα = cosU1 * cosU2 * sinY / sinσ;
            cosSqα = 1 - sinα*sinα;
            cos2σM = cosσ - 2*sinU1*sinU2/cosSqα;
            if (Double.isNaN(cos2σM)) cos2σM = 0;  // equatorial line: cosSqα=0 (§6)
            C = f/16*cosSqα*(4+f*(4-3*cosSqα));
            deltaY = Y;
            Y = L + (1-C) * f * sinα * (σ + C*sinσ*(cos2σM+C*cosσ*(-1+2*cos2σM*cos2σM)));
        } while (Math.abs(Y-deltaY) >= 1e-12 && ++iterations<200);
        if (iterations>=200) return null;

        double uSq = cosSqα * (a*a - b*b) / (b*b);
        double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
        double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
        double Δσ = B*sinσ*(cos2σM+B/4*(cosσ*(-1+2*cos2σM*cos2σM)-
                B/6*cos2σM*(-3+4*sinσ*sinσ)*(-3+4*cos2σM*cos2σM)));

        double distance = b*A*(σ-Δσ);
        double initialBearing = Math.atan2(cosU2*sinY,  cosU1*sinU2-sinU1*cosU2*cosY);
        double finalBearing = Math.atan2(cosU1*sinY, -sinU1*cosU2+cosU1*sinU2*cosY);

        initialBearing = toDegs((initialBearing + 2*Math.PI) % (2*Math.PI)); // normalise to 0...360
        finalBearing = toDegs((finalBearing + 2*Math.PI) % (2*Math.PI)); // normalise to 0...360

        //s = Number(s); // round to 1mm precision
        return new BearingDistancePair(initialBearing, finalBearing, distance);
    }

    public String sweep(Canvas canvas, float maxRad) {
        Location des = getDes();
        Location src = getSrc();
        String distStr = "Unknown";

        if(src != null && des != null) {
            double distance = src.distanceTo(des);
            int heading = (int) src.getBearing();
            int bearing = (int) src.bearingTo(des);

            //GeomagneticField field = new GeomagneticField(Double.valueOf(src.getLatitude()).floatValue(), Double.valueOf(src.getLongitude()).floatValue(), 0, System.currentTimeMillis());
            float[] orientation = Tracker.getTracker().getOrientation();
            if(orientation != null)
                marker.pin(x, y, (float) toRads(toDegs(orientation[0]) - (bearing + heading)), maxRad, (float) distance, canvas);
//                    heading = (int) (heading + field.getDeclination());
//                marker.pin(x, y, (float) orientation[0], maxRad, (float) distance, canvas);
            if(distance >= 1000) {
                distance = distance / 1000;
                distStr = new DecimalFormat("#0.00").format(distance) + " kms approx.";
            } else {
                distStr = new DecimalFormat("#0.00").format(distance) + " ms approx.";
            }
//            canvas.drawText("Provider: " + src.getProvider(), 5, 40, paint);
        }
        return "Distance from here is " + distStr;
    }
}
