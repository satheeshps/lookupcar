package com.trolp.lookupcar.maps.utils;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trolp.lookupcar.utils.CompleteListener;

public class MapUtils {
	public static Marker addMarker(LatLng coords, String title, String snippet, int iconId, GoogleMap map) {
		if(coords != null && map != null) {
			return map.addMarker(new MarkerOptions()
			.position(coords)
			.title(title)
			.snippet(snippet)
			.icon(BitmapDescriptorFactory
					.fromResource(iconId)));
		}
		return null;
	}

	public static void shoot(LatLng coords, int zoom, int zoomTo, int duration, CancelableCallback callback, GoogleMap map) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, zoom));
		map.animateCamera(CameraUpdateFactory.zoomTo(zoomTo), duration, callback);
	}

	public static Polyline addPolyLine(LatLng src, LatLng des, int width, int color, GoogleMap map) {
		return map.addPolyline(new PolylineOptions()
		.add(src, des)
		.width(5)
		.color(color));
	}

	public static void route(LatLng src,LatLng des, CompleteListener<PolylineOptions,Void> listener) {
		String url = routeURL(src, des);
		MapRouteResolver resolver = new MapRouteResolver();
		resolver.execute(url, listener);
	}
	
	private static String routeURL(LatLng src, LatLng des) {
		String str_origin = "origin="+src.latitude+","+src.longitude;
		String str_dest = "destination="+des.latitude+","+des.longitude;
		String sensor = "sensor=false";

		String waypoints = "waypoints=" + src.latitude + "," + src.longitude + "|" + des.latitude + "," + des.longitude;
		String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;
		String output = "json";
		return "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
	}

	public static LatLng toLatLng(Location loc) {
		return new LatLng(loc.getLatitude(), loc.getLongitude());
	}
}
