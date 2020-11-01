package com.trolp.lookupcar;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ParkItem extends ArrayAdapter<String> {
	public ParkItem(Activity context) {
		super(context, R.layout.park_item, new String[]{"hi"});
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
		View rowView= inflater.inflate(R.layout.park_item, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.parkItemTxt);
		TextView txtTime = (TextView) rowView.findViewById(R.id.parkTimeItemTxt);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.parkItemImg);
		txtTitle.setText("Your car is parked at Forum Mall");
		txtTime.setText("14 October 1983, 10:34 PM");
//		imageView.setImageResource(imageId[position]);
		return rowView;
	}
}
