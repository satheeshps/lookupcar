package com.trolp.lookupcar.recorder;

import android.provider.BaseColumns;

public final class Schema {
	public static abstract class GeneralSettings implements BaseColumns {
		public static final String TABLE_NAME = "general_settings";
		public static final String COLUMN_NAME_AUTO_NOTIFICATION = "auto_notification";
		public static final String COLUMN_NAME_DEFAULT_NOTIFICATION_DURATION = "default_notification_duration";
		public static final String COLUMN_NAME_DEFAULT_ROUTER_TYPE = "default_router_type";
	}

	public static abstract class ParkingDetails implements BaseColumns {
		public static final String TABLE_NAME = "parking_detials";
		public static final String COLUMN_NAME_PARKING_DETAILS_ID = "parking_details_id";
		public static final String COLUMN_NAME_PARKING_TYPE = "parking_type";
		public static final String COLUMN_NAME_PARKING_DESC = "parking_desc";
		public static final String COLUMN_NAME_PARKED_TIME = "parked_time";
		public static final String COLUMN_NAME_PARKING_NOTIFICATION_TIME = "parking_notification_time";
		public static final String COLUMN_NAME_UNPARKED_TIME = "unparked_time";
		public static final String COLUMN_NAME_PARKED_COORDS = "parked_coords";
	}

	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INT";
	private static final String DATE_TIME_TYPE = " DATETIME";
	private static final String COMMA_SEP = ",";

	public static final String SQL_CREATE_PARKING_DETAILS =
			"CREATE TABLE " + ParkingDetails.TABLE_NAME + " (" +
					ParkingDetails.COLUMN_NAME_PARKING_DETAILS_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
					ParkingDetails.COLUMN_NAME_PARKING_TYPE + TEXT_TYPE + COMMA_SEP +
					ParkingDetails.COLUMN_NAME_PARKING_DESC + TEXT_TYPE + COMMA_SEP +
					ParkingDetails.COLUMN_NAME_PARKED_TIME + DATE_TIME_TYPE + COMMA_SEP +
					ParkingDetails.COLUMN_NAME_PARKING_NOTIFICATION_TIME + INT_TYPE + COMMA_SEP +
					ParkingDetails.COLUMN_NAME_UNPARKED_TIME + TEXT_TYPE + COMMA_SEP +
					ParkingDetails.COLUMN_NAME_PARKED_COORDS + TEXT_TYPE +
					" )";

	public static final String SQL_DELETE_PARKING_DETAILS =
			"DROP TABLE IF EXISTS " + ParkingDetails.TABLE_NAME;

	public static final String SQL_CREATE_GENERAL_SETTINGS =
			"CREATE TABLE " + GeneralSettings.TABLE_NAME + " (" +
					GeneralSettings.COLUMN_NAME_AUTO_NOTIFICATION + " INTEGER PRIMARY KEY" + COMMA_SEP +
					GeneralSettings.COLUMN_NAME_DEFAULT_NOTIFICATION_DURATION + TEXT_TYPE + COMMA_SEP +
					GeneralSettings.COLUMN_NAME_DEFAULT_ROUTER_TYPE + TEXT_TYPE + 
					" )";

	public static final String SQL_DELETE_GENERAL_SETTINGS =
			"DROP TABLE IF EXISTS " + GeneralSettings.TABLE_NAME;
}
