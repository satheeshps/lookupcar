package com.trolp.lookupcar.recorder;

import java.util.concurrent.ExecutionException;

import com.trolp.lookupcar.utils.CompleteListener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public abstract class DBAider extends SQLiteOpenHelper {
	private enum SQLCommands {
		INSERT,
		UPDATE,
		DELETE,
		READ;
	}

	public static class Result<T> {
		private boolean isSuccess;
		private SQLCommands command;
		private T result;

		public Result(SQLCommands command, T result, boolean isSuccess) {
			this.result = result;
			this.isSuccess = isSuccess;
			this.command = command;
		}

		public T getResult() {
			return result;
		}

		public boolean isSuccess() {
			return isSuccess;
		}
		
		public SQLCommands getSQLCommand() {
			return command;
		}
	}

	private class DBCaller extends AsyncTask<Object, Void, DBAider.Result<?>> {
		@Override
		protected DBAider.Result<?> doInBackground(Object... params) {
			DBAider.Result<?> result = null;
			SQLCommands sqlCmd = null;
			try {
				sqlCmd = (SQLCommands) params[0];
				String table = (String) params[1];
				switch (sqlCmd) {
				case INSERT: {
					String columnHack = (String) params[2];
					ContentValues contentVals = (ContentValues) params[3];
					long ret = DBAider.this.getWritableDatabase().insert(table, columnHack, contentVals);
					result = new Result<Long>(sqlCmd, ret, true);
				}
				break;
				case UPDATE: {
					ContentValues contentVals = (ContentValues) params[2];
					String whereClause = (String) params[3];
					String[] whereArgs = (String[]) params[4];
					int ret = DBAider.this.getWritableDatabase().update(table, contentVals, whereClause, whereArgs);
					result = new Result<Integer>(sqlCmd, ret, true);
				}
				break;
				case DELETE: {
					String selection = (String) params[2];
					String[] selectionArgs = (String[]) params[3];
					int ret = DBAider.this.getWritableDatabase().delete(table, selection, selectionArgs);
					result = new Result<Integer>(sqlCmd, ret, true);
				}
				break;
				case READ: {
					String[] columns = (String[]) params[2];
					String selection = (String) params[3];
					String[] selectionArgs = (String[]) params[4];
					String groupBy = (String) params[5];
					String having = (String) params[6];
					String orderBy = (String) params[7];
					Cursor cursor = DBAider.this.getReadableDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
					result = new Result<Cursor>(sqlCmd, cursor, true);
				}
				break;
				}
			} catch(Exception ex) {
				result = new Result<Void>(sqlCmd, null, false);
			}
			return result;
		}

		@Override
		protected void onPostExecute(Result<?> result) {
			super.onPostExecute(result);
			if(listener != null) {
				if(result.isSuccess)
					listener.onSuccess(result);
				else
					listener.onFailure(result);
			}
		}
	}

	private CompleteListener<DBAider.Result<?>, DBAider.Result<?>> listener;

	public DBAider(Context context, String name, CursorFactory factory,
			int version, CompleteListener<DBAider.Result<?>, DBAider.Result<?>> listener) {
		super(context, name, factory, version);
	}

	public void insert(String table,ContentValues values) {
		new DBCaller().execute(SQLCommands.INSERT, table, null, values);
	}

	public void update(String table, ContentValues values, String whereClause, String... whereArgs) {
		new DBCaller().execute(SQLCommands.UPDATE, table, values, whereClause, whereArgs);
	}
	
	public void delete(String table,String whereClause, String... whereArgs) {
		new DBCaller().execute(SQLCommands.DELETE, whereClause, whereArgs);
	}

	public void read(String table, String[] cols, String selection, String[] selectionArgs, String groupy, String having, String orderBy) {
		new DBCaller().execute(SQLCommands.READ, table, cols, selection, selectionArgs, groupy, having, orderBy);
	}
	
	public DBAider.Result<?> readNow(String table, String[] cols, String selection, String[] selectionArgs, String groupy, String having, String orderBy) throws InterruptedException, ExecutionException {
		DBCaller db = new DBCaller();
		db.execute(SQLCommands.READ, table, cols, selection, selectionArgs, groupy, having, orderBy);
		return db.get();
	}

	protected CompleteListener<DBAider.Result<?>, DBAider.Result<?>> getCompleteListener() {
		return listener;
	}
}
