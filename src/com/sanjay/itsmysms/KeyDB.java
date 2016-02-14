package com.sanjay.itsmysms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KeyDB {

	// KEY_PHNO KEY_NAME KEY_SENTREC KEY_BODY KEY_TIME
	public static final String KEY_PHNO = "pn_no";
	public static final String KEY_KEY = "key";
	public static final String KEY_NAME = "name";

	private static final String DB_NAME = "KeyDB";
	private static final String DB_TABLE = "KeyList";
	private static final int DB_VERSION = 1;

	private DBHelper myHelper;
	private final Context myContext;
	private SQLiteDatabase myDB;

	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {

			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + DB_TABLE + " (" + KEY_PHNO + " TEXT, "
					+ KEY_KEY + " TEXT, " + KEY_NAME + " TEXT, PRIMARY KEY("
					+ KEY_PHNO + "));");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TALBE IF EXISTS " + DB_TABLE);
			onCreate(db);
		}

	}

	public KeyDB(Context c) {

		myContext = c;
	}

	public KeyDB open() throws SQLException {

		myHelper = new DBHelper(myContext);
		myDB = myHelper.getWritableDatabase();
		return this;
	}

	public void close() {

		myHelper.close();
	}

	public long createRecord(String phno, String key, String name) {

		ContentValues cv = new ContentValues();
		cv.put(KEY_PHNO, phno);
		cv.put(KEY_KEY, key);
		cv.put(KEY_NAME, name);

		return myDB.insert(DB_TABLE, null, cv);
	}

	// Return the string type of each message row with all its details
	public String getKey(String phno) {

		String key;
		String[] cols = new String[] { KEY_KEY };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + "='" + phno + "'",
				null, null, null, null);

		if (c.getCount() == 0)
			return "UNKNOWN";
		else {
			c.moveToNext();
			key = c.getString(c.getColumnIndex(KEY_KEY));
			return key;
		}
	}

	public ArrayList<String> nameList() {

		ArrayList<String> phnList = new ArrayList<String>();

		String[] cols = new String[] { KEY_PHNO, KEY_NAME };
		Cursor c = myDB.query(DB_TABLE, cols, null, null, null, null, null,
				null);

		int iName = c.getColumnIndex(KEY_NAME);
		int iPhno = c.getColumnIndex(KEY_PHNO);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String temp = c.getString(iName);
			if (temp.equals("UNKNOWN")) {
				phnList.add(c.getString(iPhno));
			} else {
				phnList.add(temp);
			}

		}

		return phnList;

	}

	/*
	public ArrayList<String> numberList() {

		ArrayList<String> phnList = new ArrayList<String>();

		String[] cols = new String[] { KEY_PHNO };
		Cursor c = myDB.query(DB_TABLE, cols, null, null, null, null, null,
				null);

		int iPhno = c.getColumnIndex(KEY_PHNO);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
			phnList.add(c.getString(iPhno));

		return phnList;
	}
*/
	
	
	public int updateKey(String newKey, String phNo) {

		ContentValues cv = new ContentValues();
		cv.put(KEY_KEY, newKey);

		return myDB.update(DB_TABLE, cv, KEY_PHNO + " = '" + phNo + "'", null);
	}
	
	
	public String getDBName(String phno) {

		String name;
		String[] cols = new String[] { KEY_NAME };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + "='" + phno + "'",
				null, null, null, null);

		c.moveToNext();

		if (c.getCount() == 0)
			return "UNKNOWN";
		else
			name = c.getString(c.getColumnIndex(KEY_NAME));
		return name;
	}
	

	public void deleteRecords() {

		myDB.delete(DB_TABLE, null, null);
	}

	public boolean isExists(String phno) {

		String[] cols = new String[] { KEY_NAME };

		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + "='" + phno + "'",
				null, null, null, null);

		c.moveToNext();

		if (c.getCount() == 0)
			return false;
		else
			return true;
	}

	public void deleteConv(String delNumber) {

		myDB.delete(DB_TABLE, KEY_PHNO + "='" + delNumber + "'", null);
	}

	/*
	public ArrayList<Map<String, String>> nameBodyMap() {

		
		ArrayList<Map <String, String>> phnList = new ArrayList<Map<String, String>>();

		String[] cols = new String[] { KEY_PHNO, KEY_NAME, KEY_KEY  };
		
		Cursor c = myDB.query(DB_TABLE, cols, null, null, null, null, null,
				null);
		
		SmsDB getLast = new SmsDB(myContext);
		getLast.open();

		int iName = c.getColumnIndex(KEY_NAME);
		int iPhno = c.getColumnIndex(KEY_PHNO);
		int iKey  = c.getColumnIndex(KEY_KEY);
		
		phnList.clear();

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			
			Map<String, String> oneRow = new HashMap<String, String>();
			
			
			
			String temp = c.getString(iName);
			
			String key = c.getString(iKey);
			
			String phno = c.getString(iPhno);
			
			String lastBody = getLast.getLastSms(phno);
			try{
			lastBody = StringCryptor.decrypt(key, lastBody);
			}
			catch(BadPaddingException e){
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String lTime = getLast.getLastTime(phno);
			
		
			
			
			

			if (temp.equals("UNKNOWN")) {
				oneRow.put("name", phno);
			} else {
				oneRow.put("name", temp);
			}
			oneRow.put("body", lastBody);
			oneRow.put("time", lTime);
			
			phnList.add(oneRow);
			
			
			
			
			

		}
		
		getLast.close();
		
		ArrayList<Map <String, String>> sortPhnList = new ArrayList<Map<String, String>>();
		
		int i = 0;
		for (Map<String, String> row : phnList) {
			
			
			
			
			i++;
		}
		
		return phnList;
	}
	
	*/
}
