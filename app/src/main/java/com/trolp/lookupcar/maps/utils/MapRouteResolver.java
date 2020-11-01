package com.trolp.lookupcar.maps.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trolp.lookupcar.utils.CompleteListener;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

public class MapRouteResolver extends AsyncTask<Object, Void, MapRouteResolver.RouteMapDataHolder> {
	public static class RouteMapDataHolder {
		private List<List<Map<String, String>>> result;
		private CompleteListener<PolylineOptions,Void> listener;

		public RouteMapDataHolder(List<List<Map<String, String>>> result, CompleteListener<PolylineOptions,Void> listener) {
			this.result = result;
			this.listener = listener;
		}

		public List<List<Map<String, String>>> getResult() {
			return result;
		}

		public CompleteListener<PolylineOptions,Void> getListener() {
			return listener;
		}
	}

	@Override
	protected RouteMapDataHolder doInBackground(Object... data) {
		List<List<Map<String, String>>> result = null;
		CompleteListener<PolylineOptions,Void> listener = null;
		try{
			RouteParser parser = new RouteParser();
			String url = downloadUrl((String)data[0]);
			listener = (CompleteListener<PolylineOptions,Void>)data[1];
			result = parser.parse(new JSONObject(url));
		}catch(Exception e) {
			Log.d("Background Task",e.toString());
		}
		return new RouteMapDataHolder(result, listener);
	}

	@Override
	protected void onPostExecute(RouteMapDataHolder result) {
		super.onPostExecute(result);
		if(result != null) {
			CompleteListener<PolylineOptions,Void> listener = result.getListener();
			if(result.getResult() != null) {
				PolylineOptions lineOptions = drawRoute(result.getResult());
				if(listener != null)
					listener.onSuccess(lineOptions);
			} else {
				if(listener != null)
					listener.onFailure(null);
			}
		}
	}

	private PolylineOptions drawRoute(List<List<Map<String, String>>> result) {
		List<LatLng> points = null;
		PolylineOptions lineOptions = null;

		if(result != null) {
			for(int i=0;i<result.size();i++){
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();
				List<Map<String, String>> path = result.get(i);
				for(int j=0;j<path.size();j++){
					Map<String,String> point = path.get(j);
					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}
				lineOptions.addAll(points);
				lineOptions.width(10);
				lineOptions.color(Color.RED);
			}
		}
		return lineOptions;
	}

	private String downloadUrl(String strUrl) throws IOException{
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL(strUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			iStream = urlConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
			StringBuffer sb  = new StringBuffer();
			String line = "";
			while( ( line = br.readLine())  != null)
				sb.append(line);
			data = sb.toString();
			br.close();
		}catch(Exception e){
			Log.d("Exception while downloading url", e.toString());
		}finally{
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}
}
