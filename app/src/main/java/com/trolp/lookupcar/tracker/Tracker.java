package com.trolp.lookupcar.tracker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Tracker implements LocationListener, SensorEventListener {
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static Tracker instance = null;
	private static Object lock = new Object();
	private volatile Coordinate latestCoord;
    private volatile Location lastLocation;
	private LocationManager locationManager;
	private SensorManager sensorManager;
	private List<CoordinateListener> listeners;
	private Object syncLock = new Object();
	private LocationListener locationListener;
	private Sensor magSensor;
    private Sensor accSensor;
	private float[] lastMagVals;
    private float[] lastAccVals;
    private float[] lastOrientVals = new float[3];

	private Tracker(Context ctx) {
		locationManager = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
		sensorManager = (SensorManager)ctx.getSystemService(Context.SENSOR_SERVICE);
		accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magSensor= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME);
		locationListener = this;
		on();
	}

	public static Tracker init(Context ctx) {
		if(instance == null) {
			synchronized(lock) {
				if(instance == null) {
					instance = new Tracker(ctx);
				}
			}
		}
		return instance;
	}

	public static Tracker getTracker() {
		return instance;
	}

	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
//		if (isSignificantlyNewer) {
//			return true;
//		} else {
		    if (isSignificantlyOlder) {
			    return false;
            }
//        }

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 20;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
            return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

    private void findLoc() {
		if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			if (latestCoord == null || lastLocation == null) {
				latestCoord = new Coordinate(lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
			}
		}
    }

	public Coordinate getCoordinate() {
		findLoc();
		return latestCoord;
	}

	public void addCoordinateListener(CoordinateListener listener) {
		synchronized (syncLock) {
			if(listeners == null)
				listeners = new ArrayList<CoordinateListener>();
			listeners.add(listener);
		}
	}

	public void removeCoordinateListener(CoordinateListener listener) {
		synchronized(syncLock) {
			if(listeners != null)
				listeners.remove(listener);
		}
	}

	public void on() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	public void off() {
        locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onLocationChanged(Location location) {
//        if(isBetterLocation(location, lastLocation)) {
            lastLocation = location;
            latestCoord = new Coordinate(location.getLatitude(), location.getLongitude(), location.getAltitude());
            synchronized (Tracker.this.syncLock) {
                if (listeners != null) {
                    for (CoordinateListener listener : listeners) {
                        listener.onCoordinateChanged(latestCoord);
                    }
                }
            }
//        }
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType ()){
            case Sensor.TYPE_ACCELEROMETER:
                lastAccVals = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastMagVals = event.values.clone();
                break;
        }

        float[] R = new float[16];

        if( lastMagVals == null || lastAccVals == null )
            return;

        if( !SensorManager.getRotationMatrix(R, null, lastAccVals, lastMagVals) )
            return;

        float[] outR = new float[16];
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
        SensorManager.getOrientation(outR, lastOrientVals);
    }

    public Location getLocation() {
        findLoc();
        return lastLocation;
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	public float[] getOrientation() {
        return lastOrientVals;
	}
}
