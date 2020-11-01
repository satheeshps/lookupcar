package com.trolp.lookupcar.recorder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.trolp.lookupcar.recorder.DBAider.Result;
import com.trolp.lookupcar.tracker.Coordinate;
import com.trolp.lookupcar.utils.CompleteListener;

public class Recorder {
	private static Recorder instance = null;
	private static Object lock = new Object();
	private ParkData savedParkData;
	private List<ParkData> parkDataList;
	private ParkingDetails parkingDetails;

	public static class ParkData {
		private String desc;
		private Coordinate coord;
		private Date parkedTime;
		private Date unParkedTime;
		private int notificationDuration;
		private String parkingType;

		public String getDescription() {
			return desc;
		}

		public void setDescription(String desc) {
			this.desc = desc;
		}

		public Coordinate getCoordinate() {
			return coord;
		}

		public void setCoordinate(Coordinate coord) {
			this.coord = coord;
		}

		public Date getParkedTime() {
			return parkedTime;
		}

		public void setParkedTime(Date parkedTime) {
			this.parkedTime = parkedTime;
		}

		public Date getUnParkedTime() {
			return unParkedTime;
		}

		public void setUnParkedTime(Date unParkedTime) {
			this.unParkedTime = unParkedTime;
		}

		public int getNotificationDuration() {
			return notificationDuration;
		}
		public void setNotificationDuration(int notificationDuration) {
			this.notificationDuration = notificationDuration;
		}

		public String getParkingType() {
			return parkingType;
		}

		public void setParkingType(String parkingType) {
			this.parkingType = parkingType;
		}
		
		@Override
		public String toString() {
			return getDescription() + " " + getParkedTime() + " in " + getCoordinate();
		}
	}

	private Recorder(Context ctx) {
		parkingDetails = new ParkingDetails(ctx, new CompleteListener<DBAider.Result<?>, DBAider.Result<?>>() {
			@Override
			public void onSuccess(Result<?> e) {
			}

			@Override
			public void onFailure(Result<?> e) {
			}
		});
	}

	private void load() {
		try {
			DBAider.Result<Cursor> result = (Result<Cursor>) parkingDetails.readNow(Schema.ParkingDetails.TABLE_NAME,
					new String[]{Schema.ParkingDetails.COLUMN_NAME_PARKING_DETAILS_ID, Schema.ParkingDetails.COLUMN_NAME_PARKED_COORDS,Schema.ParkingDetails.COLUMN_NAME_PARKED_TIME,Schema.ParkingDetails.COLUMN_NAME_PARKING_DESC, Schema.ParkingDetails.COLUMN_NAME_PARKING_NOTIFICATION_TIME,Schema.ParkingDetails.COLUMN_NAME_PARKING_TYPE,Schema.ParkingDetails.COLUMN_NAME_UNPARKED_TIME},
					null,
					null,
					null,
					null,
					Schema.ParkingDetails.COLUMN_NAME_PARKED_TIME + " DESC LIMIT 5");

			getParkDataList().clear();
			if(result.isSuccess()) {
				Cursor cursor = result.getResult();
				while(cursor.moveToNext()){
                    String coordVal = cursor.getString(cursor.getColumnIndexOrThrow(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKED_COORDS));
					String desc = cursor.getString(cursor.getColumnIndexOrThrow(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKING_DESC));
					String notifTime = cursor.getString(cursor.getColumnIndexOrThrow(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKING_NOTIFICATION_TIME));
					String parkedTime = cursor.getString(cursor.getColumnIndexOrThrow(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKED_TIME));
					String parkedType = cursor.getString(cursor.getColumnIndexOrThrow(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKING_TYPE));
					String unParkedTime = cursor.getString(cursor.getColumnIndexOrThrow(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_UNPARKED_TIME));

                    Coordinate coords = new Coordinate(coordVal);
					int notifyTime = -1;
					try {
						notifyTime = Integer.parseInt(notifTime);
					} catch(NumberFormatException ex) {
						Log.d("Recorder", ex.getMessage());
					}
					ParkData parkData = new ParkData();
					if(parkedType.equals("PARKED"))
						savedParkData = populate(parkData, coords, desc, notifyTime, null, null, parkedType);
					getParkDataList().add(parkData);
				}
			}
		}
		catch(Exception ex) {
			Log.d("Recorder", ex.getMessage());
		}
	}

	private ParkData populate(ParkData data, Coordinate coords, String desc, int notifTime, Date parkedTime, Date unParkedTime, String parkedType) {
		data.setCoordinate(coords);
		data.setDescription(desc);
		data.setNotificationDuration(notifTime);
		data.setParkedTime(parkedTime);
		data.setParkingType(parkedType);
		data.setUnParkedTime(unParkedTime);
		return data; 
	}

	public List<ParkData> getParkDataList() {
		if(parkDataList == null)
			parkDataList = new LinkedList<ParkData>();
		return parkDataList;
	}

	public static Recorder getRecorder() {
		return instance;
	}

	public void record(Coordinate coord) {
		savedParkData = populate(new ParkData(), coord, "Your car is parked at ", 60, new Date(), null, "PARKED");
        ContentValues values = new ContentValues();
        values.put(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKING_DESC, "Your car is parked at ");
        values.put(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKED_COORDS, coord.toString());
        values.put(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKING_NOTIFICATION_TIME, "");
        values.put(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKED_TIME, new Date().toString());
        values.put(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_UNPARKED_TIME, "");
        values.put(com.trolp.lookupcar.recorder.Schema.ParkingDetails.COLUMN_NAME_PARKING_TYPE, "PARKED");
        parkingDetails.insert(Schema.ParkingDetails.TABLE_NAME, values);

        if(!getParkDataList().isEmpty())
            getParkDataList().remove(getParkDataList().size() - 1);
        getParkDataList().add(0, savedParkData);
	}

	public ParkData play() {
		return savedParkData;
	}

	public static Recorder init(Context ctx) {
		if(instance == null) {
			synchronized(lock) {
				if(instance == null) {
					instance = new Recorder(ctx);
					instance.load();
				}
			}
		}
		return instance;
	}

	public void update() {
		savedParkData = null;
		ContentValues values = new ContentValues();
		values.put(Schema.ParkingDetails.COLUMN_NAME_PARKING_TYPE, "UNPARKED");
		parkingDetails.update(Schema.ParkingDetails.TABLE_NAME, values, Schema.ParkingDetails.COLUMN_NAME_PARKING_TYPE+"=?", new String[]{"PARKED"});
	}
}
