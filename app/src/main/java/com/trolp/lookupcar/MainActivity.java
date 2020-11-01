package com.trolp.lookupcar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trolp.lookupcar.ParkStateMachine.ParkStates;
import com.trolp.lookupcar.ParkStateMachine.UserStateData;
import com.trolp.lookupcar.radar.RadarFragment;
import com.trolp.lookupcar.recorder.Recorder;
import com.trolp.lookupcar.recorder.Recorder.ParkData;
import com.trolp.lookupcar.tracker.Coordinate;
import com.trolp.lookupcar.tracker.CoordinateListener;
import com.trolp.lookupcar.tracker.Tracker;
import com.trolp.lookupcar.utils.CompleteListener;
import com.trolp.lookupcar.view.FragmentPagerAdapter;
import com.trolp.lookupcar.view.VerticalViewPager;
import com.trolp.lookupcar.view.VerticalViewPager.OnPageChangeListener;
import com.trolp.lookupcar.maps.utils.MapUtils;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements CoordinateListener, ParkStateChangeListener {
	private Tracker tracker;
	private Recorder recorder;
	private Coordinate parkedPos;
	private Button parkButton;
	private Button findButton;
	private ImageButton finderTypeButton;
	private TextView msgTitle;
	private TextView msgDetail;
	private ImageView imageView;
	private VerticalViewPager viewPager;
	private ParkStateMachine machine;

    @TargetApi(21)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		machine = new ParkStateMachine();
		machine.addStateChangeListener(this);

        TextView appName = ((TextView)this.findViewById(R.id.textView));
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/Nordica.ttf");
        appName.setTypeface(typeFace);

		parkButton = ((Button)this.findViewById(R.id.imageButton1));
		findButton = ((Button)this.findViewById(R.id.imageButton2));
		finderTypeButton = ((ImageButton)this.findViewById(R.id.finderTypeButton));
		viewPager = (VerticalViewPager)this.findViewById(R.id.pager);
		viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int arg0) {
                return arg0 == 0 ? new SupportMapFragment() : new RadarFragment();
			}

			@Override
			public int getCount() {
				return 2;
			}
		});
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				finderTypeButton.setImageDrawable(getResources().getDrawable(position == 1 ? R.drawable.map_screen : R.drawable.radar_screen));
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
									   int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		msgTitle = (TextView) this.findViewById(R.id.parkItemTxt);
		msgDetail = (TextView) this.findViewById(R.id.parkTimeItemTxt);
		imageView = (ImageView) this.findViewById(R.id.parkItemImg);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(this.getResources().getColor(R.color.sea_blue));
		}
		init();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Tracker.getTracker().off();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Tracker.getTracker().on();
	}

	private void addButtonHandler(View button, View.OnClickListener handler) {
		button.setOnClickListener(handler);
	}

	private void init() {
		addButtonHandler(parkButton, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isParked()) {
					if(parkedPos != null) {
						machine.park(new UserStateData(parkedPos));
					}
				} else {
					machine.unpark(null);
				}
			}
		});
		addButtonHandler(findButton, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				machine.find(null);
			}
		});
		addButtonHandler(finderTypeButton, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1 - viewPager.getCurrentItem());
				finderTypeButton.setImageDrawable(getResources().getDrawable(viewPager.getCurrentItem() == 1 ? R.drawable.map_screen : R.drawable.radar_screen));
			}
		});
		//		addButtonHandler(arrowButton, new View.OnClickListener() {
		//			@Override
		//			public void onClick(View v) {
		//				int res = (viewPager.getCurrentItem()+1)%2;
		//				if(res == 0) {
		//					arrowButton.setBackground(getResources().getDrawable(R.drawable.up));
		//					viewPager.setCurrentItem(res);
		//				}
		//				else {
		//					arrowButton.setBackground(getResources().getDrawable(R.drawable.down));
		//					viewPager.setCurrentItem(res);
		//				}
		//			}
		//		});
		tracker = Tracker.init(this.getApplicationContext());
		recorder = Recorder.init(this.getApplicationContext());
		tracker.addCoordinateListener(this);

		if(recorder.play() != null)
			machine.park(new UserStateData(((ParkData)recorder.play()).getCoordinate()));
		else
			machine.begin();
	}

	private SupportMapFragment getMapFragment() {
		if(getSupportFragmentManager().getFragments() != null) {
			for(Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment instanceof SupportMapFragment)
					return (SupportMapFragment)fragment;
			}
		}
		return null;
	}

	private void moveCurrentPos(Coordinate location) {
		SupportMapFragment mapFragment = getMapFragment();
		if (mapFragment != null) {
			GoogleMap map = mapFragment.getMap();
			LatLng coords = location.toLatLng();
			if(coords != null) {
				map.clear();
				MapUtils.addMarker(coords, "Park Location", "You car is parked now", R.drawable.blue_pin, map);
				MapUtils.shoot(coords, 15, 16, 2000, null, map);
			}
		}
	}

	private void findRoute(Coordinate src, Coordinate des) {
		SupportMapFragment mapFragment = getMapFragment();
		if (mapFragment != null) {
			final GoogleMap map = mapFragment.getMap();
			if (map != null && src != null && des != null) {
				LatLng srcLat = src.toLatLng();
				LatLng desLat = des.toLatLng();

				map.clear();
				MapUtils.addMarker(srcLat, "Parked Location", "You car is parked here", R.drawable.blue_pin, map);
				MapUtils.addMarker(desLat, "Your Position", "You are here", R.drawable.green_pin, map);
				MapUtils.shoot(desLat, 15, 16, 2000, null, map);

				MapUtils.route(srcLat, desLat, new CompleteListener<PolylineOptions, Void>() {
                    @Override
                    public void onSuccess(PolylineOptions opts) {
                        map.addPolyline(opts);
                        machine.found(null);
                    }

                    @Override
                    public void onFailure(Void e) {
                    }
                });

				parkedPos = des;
			}
		}
	}

	@Override
	public void onCoordinateChanged(Coordinate coords) {
		if(isStart()) {
			machine.ready(new UserStateData(coords));
		} else {
			if(isFind()) {
				findRoute(recorder.play().getCoordinate(), coords);
			}
		}
	}

	private boolean isParked() {
		return machine.getCurrentState() == ParkStates.PARKED;
	}

	private boolean isFind() {
		return machine.getCurrentState() == ParkStates.FINDING && machine.getPreviousState() == ParkStates.PARKED;
	}

	private boolean isStart() {
		return machine.getCurrentState() == ParkStates.START;
	}

	// Park State handlers
	@Override
	public void onBeforeStateChange(ParkStates from, ParkStates to, UserStateData data) {
	}

	@Override
	public void onStateChange(ParkStates from, ParkStates to, UserStateData data) {
		switch (to) {
		case START:
			parkButton.setEnabled(false);
			findButton.setEnabled(false);
			parkedPos = null;
			break;
		case PARKABLE:
			if(from == ParkStates.PARKED) {
				recorder.update();
				parkedPos = tracker.getCoordinate();
				moveCurrentPos(parkedPos);
			}
			if(data != null && data.getData() != null) {
				parkedPos = data.getData();
				moveCurrentPos(parkedPos);
			}
			parkButton.setEnabled(true);
			findButton.setEnabled(false);
//            parkButton.setTextColor(getResources().getColor(R.color.white));
			//			parkButton.setText(R.string.park);
			//parkButton.setImageDrawable(getResources().getDrawable(R.drawable.park));
			tracker.off();
			break;
		case PARKED:
			switch(from) {
			case FINDING:
				tracker.off();
				findButton.setEnabled(true);
				break;
			case PARKABLE:
				if(data != null && data.getData() != null) {
					recorder.record(((Coordinate)data.getData()));
					ParkData dat = recorder.play();
					if(dat != null)
                        printMsg(dat.getDescription(), dat.getParkedTime().toString());
				}
			case START:
				findButton.setEnabled(true);
				parkedPos = null;
				break;
			}
			parkButton.setEnabled(false);
//            parkButton.setTextColor(getResources().getColor(R.color.steel_grey));
			//			parkButton.setText(R.string.unpark);
			//parkButton.setImageDrawable(getResources().getDrawable(R.drawable.park_dis));
			break;
		case FINDING:
			tracker.on();
			findButton.setEnabled(false);
			parkButton.setEnabled(false);
			break;
		}
	}

    private void printMsg(String title, String desc) {
        if(title != null && !title.isEmpty())
            msgTitle.setText(title);
        if(desc != null && !desc.isEmpty())
            msgDetail.setText(desc);
    }

	@Override
	@SuppressLint("NewApi")
	public void onAfterStateChange(ParkStates from, ParkStates to, UserStateData data) {
	}
}
