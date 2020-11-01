package com.trolp.lookupcar.recorder;

import com.trolp.lookupcar.utils.CompleteListener;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class GeneralSettings extends DBAider {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "GeneralSettings.db";

	public GeneralSettings(Context context, CompleteListener<DBAider.Result<?>, DBAider.Result<?>> listener) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, listener);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Schema.SQL_CREATE_GENERAL_SETTINGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(Schema.SQL_DELETE_GENERAL_SETTINGS);
		onCreate(db);
	}
}
