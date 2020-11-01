package com.trolp.lookupcar.tracker;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Coordinate {
	private double longitude;
	private double latitude;
	private double altitude;

	public Coordinate() {
	}

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public Coordinate(double latitude, double longitude, double altitude) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.altitude = altitude;
	}

	public Coordinate(Location loc) {
		this.longitude = loc.getLongitude();
		this.latitude = loc.getLatitude();
		this.altitude = loc.getAltitude();
	}

	public Coordinate(String coordVal) {
		if(coordVal != null) {
			String[] coords = coordVal.split(",");
			int i = 0;
			for(String coord : coords) {
				switch (i++) {
				case 0:
                    latitude = Double.valueOf(coord);
					break;
				case 1:
                    longitude = Double.valueOf(coord);
					break;
				case 2:
					altitude = Double.valueOf(coord);
					break;
				}
			}
		}
	}

	public LatLng toLatLng() {
		return new LatLng(latitude, longitude);
	}

	@Override
	public String toString() {
		return latitude + "," + longitude + "," + altitude;
	}
}
