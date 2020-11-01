package com.trolp.lookupcar.recorder;

import com.trolp.lookupcar.utils.CompleteListener;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ParkingDetails extends DBAider {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "ParkingDetails.db";

	public ParkingDetails(Context context, CompleteListener<DBAider.Result<?>, DBAider.Result<?>> listener) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, listener);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Schema.SQL_CREATE_PARKING_DETAILS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(Schema.SQL_DELETE_PARKING_DETAILS);
		onCreate(db);
	}
}
