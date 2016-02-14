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
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.text.format.DateUtils;
import android.widget.Toast;

public class SmsDB {
	// KEY_PHNO KEY_NAME KEY_SENTREC KEY_BODY KEY_TIME
	public static final String KEY_PHNO = "pn_no";
	public static final String KEY_SENTREC = "sent_rec";
	public static final String KEY_BODY = "body";
	public static final String KEY_TIME = "time";
	public static final String KEY_KEY = "key";
	public static final String KEY_SEEN = "seen";

	private static final String DB_NAME = "SmsDB";
	private static final String DB_TABLE = "MessageList";
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
					+ KEY_TIME + " INTEGER, " + KEY_SENTREC + " INTEGER, "
					+ KEY_BODY + " TEXT, " + KEY_KEY + " TEXT, " + KEY_SEEN
					+ " INTEGER,  PRIMARY KEY(" + KEY_PHNO + "," + KEY_TIME
					+ "));");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			db.execSQL("DROP TALBE IF EXISTS " + DB_TABLE);
			onCreate(db);
		}

	}

	public SmsDB(Context c) {

		myContext = c;
	}

	public SmsDB open() throws SQLException {

		myHelper = new DBHelper(myContext);
		myDB = myHelper.getWritableDatabase();
		return this;
	}

	public void close() {

		myHelper.close();
	}

	public long createRecord(DBRow row) {

		ContentValues cv = new ContentValues();
		cv.put(KEY_PHNO, row.phno);
		cv.put(KEY_TIME, row.time);
		cv.put(KEY_SENTREC, row.sentrec);
		cv.put(KEY_BODY, row.body);
		cv.put(KEY_SEEN, row.seen);

		return myDB.insert(DB_TABLE, null, cv);
	}

	// Return the string type of each message row with all its details
	public ArrayList<String> getData() {

		String[] cols = new String[] { KEY_PHNO, KEY_TIME, KEY_SENTREC,
				KEY_BODY };
		Cursor c = myDB.query(DB_TABLE, cols, null, null, null, null, KEY_TIME
				+ " DESC");
		DBRow newRow = new DBRow();
		ArrayList<String> rows = new ArrayList<String>();
		rows.clear();

		int iPhno = c.getColumnIndex(KEY_PHNO);
		int iTime = c.getColumnIndex(KEY_TIME);
		int iSentrec = c.getColumnIndex(KEY_SENTREC);
		int iBody = c.getColumnIndex(KEY_BODY);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

			newRow.phno = c.getString(iPhno);
			newRow.time = Long.parseLong(c.getString(iTime));
			newRow.sentrec = Integer.parseInt(c.getString(iSentrec));
			newRow.body = c.getString(iBody);

			rows.add(newRow.toString());
		}

		return rows;
	}

	// Gets only the phone number
	public ArrayList<String> getPhoneList() {

		ArrayList<String> phnList = new ArrayList<String>();

		String[] cols = new String[] { KEY_PHNO };
		Cursor c = myDB.query(true, DB_TABLE, cols, null, null, null, null,
				null, null);

		int iPhno = c.getColumnIndex(KEY_PHNO);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

			phnList.add(c.getString(iPhno));
		}

		return phnList;
	}

	public ArrayList<Map<String, String>> getSmsThread(String phoneNumber) {

		ArrayList<Map<String, String>> thread = new ArrayList<Map<String, String>>();

		String[] cols = new String[] { KEY_BODY, KEY_TIME, KEY_SENTREC };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + " = '" + phoneNumber
				+ "'", null, null, null, KEY_TIME + " DESC");

		int iBody = c.getColumnIndex(KEY_BODY);
		int iTime = c.getColumnIndex(KEY_TIME);
		int iSR = c.getColumnIndex(KEY_SENTREC);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

			Map<String, String> oneSms = new HashMap<String, String>();

			KeyDB forName = new KeyDB(myContext);
			forName.open();
			String name = getName(phoneNumber);
			String password = forName.getKey(phoneNumber);
			forName.close();

			String body = c.getString(iBody);
			String SR = c.getString(iSR);
			String relTime = (String) DateUtils.getRelativeDateTimeString(
					myContext, Long.parseLong(c.getString(iTime)),
					DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS, 0);

			try {
				oneSms.put("body", StringCryptor.decrypt(password, body));
			} catch (BadPaddingException e) {
				oneSms.put("body", body);
			} catch (Exception e) {
				e.printStackTrace();
			}

			oneSms.put("time", relTime);

			if (Integer.parseInt(SR) == 1) {

				oneSms.put("contacter", "Me");
			} else {

				oneSms.put("contacter", name);
			}

			thread.add(oneSms);
		}

		return thread;
	}

	// Returns purely the messages list only
	public ArrayList<String> getMessages(String phoneNumber) {

		ArrayList<String> messages = new ArrayList<String>();

		String[] cols = new String[] { KEY_BODY, KEY_TIME };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + " = '" + phoneNumber
				+ "'", null, null, null, KEY_TIME + " DESC");

		int iBody = c.getColumnIndex(KEY_BODY);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

			messages.add(c.getString(iBody));
		}

		return messages;
	}

	public ArrayList<String> getTimeStamps(String delNumber) {

		ArrayList<String> time = new ArrayList<String>();

		String[] cols = new String[] { KEY_TIME };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + " = '" + delNumber
				+ "'", null, null, null, null);

		int iTime = c.getColumnIndex(KEY_TIME);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

			time.add(c.getString(iTime));
		}

		return time;
	}

	public void deleteConv(String delNumber) {

		myDB.delete(DB_TABLE, KEY_PHNO + " = '" + delNumber + "'", null);
	}

	public String getTime(int pos, String delNumber) {

		String[] cols = new String[] { KEY_TIME };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + " = '" + delNumber
				+ "'", null, null, null, KEY_TIME + " DESC");

		int iTime = c.getColumnIndex(KEY_TIME);

		c.moveToPosition(pos);

		return c.getString(iTime);

	}

	public void deleteConv(String delNumber, String time) {

		myDB.delete(DB_TABLE, KEY_PHNO + " = '" + delNumber + "' and "
				+ KEY_TIME + " = '" + time + "'", null);
	}

	public String getLastSms(String phno) {

		String[] cols = new String[] { KEY_BODY };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + " = '" + phno + "'",
				null, null, null, KEY_TIME + " DESC");

		int iBody = c.getColumnIndex(KEY_BODY);
		c.moveToNext();

		if (c.getCount() == 0)
			return "";
		else {
			return c.getString(iBody);
		}
	}

	public String getLastTime(String phno) {

		String[] cols = new String[] { KEY_TIME };
		Cursor c = myDB.query(DB_TABLE, cols, KEY_PHNO + " = '" + phno + "'",
				null, null, null, KEY_TIME + " DESC");

		int iTime = c.getColumnIndex(KEY_TIME);
		c.moveToNext();

		if (c.getCount() == 0)
			return "";
		else {

			// String relTime = (String) DateUtils.getRelativeDateTimeString(
			// myContext, Long.parseLong(c.getString(iTime)),
			// DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS, 0);

			return c.getString(iTime);
		}
	}

	public ArrayList<Map<String, String>> nameBodyMap() {

		ArrayList<Map<String, String>> phnList = new ArrayList<Map<String, String>>();

		Cursor c = myDB.rawQuery("SELECT " + KEY_SEEN + ", " + KEY_TIME + ", "
				+ KEY_PHNO + ", " + KEY_BODY + " FROM " + DB_TABLE + " WHERE "
				+ KEY_TIME + " IN ( SELECT MAX(" + KEY_TIME + ") FROM "
				+ DB_TABLE + " GROUP BY " + KEY_PHNO + ") ORDER BY " + KEY_TIME
				+ " DESC;", null);

		/*
		 * myDB .execSQL("SELECT " + KEY_TIME + ", " + KEY_PHNO + ", " +
		 * KEY_BODY + " FROM " + DB_TABLE + " WHERE " + KEY_TIME +
		 * " IN ( SELECT MAX(" + KEY_TIME + ") FROM " + DB_TABLE + " GROUP BY "
		 * + KEY_PHNO + ") ORDER BY " + KEY_TIME + " DESC;");
		 */

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

			Map<String, String> oneRow = new HashMap<String, String>();

			String enBody = c.getString(c.getColumnIndex(KEY_BODY));

			KeyDB forKey = new KeyDB(myContext);
			forKey.open();
			String deBody = enBody;
			try {
				deBody = StringCryptor.decrypt(
						forKey.getKey(c.getString(c.getColumnIndex(KEY_PHNO))),
						enBody);
			} catch (BadPaddingException e) {
				deBody = enBody;
			} catch (Exception e) {
				e.printStackTrace();
			}
			forKey.close();

			oneRow.put("body", deBody);

			String relTime = (String) DateUtils.getRelativeDateTimeString(
					myContext,
					Long.parseLong(c.getString(c.getColumnIndex(KEY_TIME))),
					DateUtils.SECOND_IN_MILLIS, DateUtils.SECOND_IN_MILLIS, 0);

			oneRow.put("time", relTime);

			String phno = c.getString(c.getColumnIndex(KEY_PHNO));
			String name = getName(phno);

			if (c.getString(c.getColumnIndex(KEY_SEEN)).equals("0")) {

				Cursor c2 = myDB
						.query(DB_TABLE, new String[] { KEY_SEEN }, KEY_SEEN
								+ " = 0 and " + KEY_PHNO + " = '" + phno + "'",
								null, null, null, null);

				oneRow.put("name", "(" + c2.getCount() + ")" + name);
			}

			else
				oneRow.put("name", name);

			phnList.add(oneRow);
		}

		return phnList;
	}

	public ArrayList<String> numberList() {

		ArrayList<String> phnList = new ArrayList<String>();

		Cursor c = myDB.rawQuery(" SELECT " + KEY_PHNO + " FROM " + DB_TABLE
				+ " GROUP BY " + KEY_PHNO + " ORDER BY " + " MAX(" + KEY_TIME
				+ ") DESC;", null);

		int iPhno = c.getColumnIndex(KEY_PHNO);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
			phnList.add(c.getString(iPhno));

		return phnList;
	}

	private String getName(String phno) {

		String name = null;
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phno));
		String[] proj = new String[] { Contacts.DISPLAY_NAME };

		String sortOrder1 = StructuredPostal.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor crsr = myContext.getContentResolver().query(lookupUri, proj,
				null, null, sortOrder1);

		if (crsr.getCount() == 0) {

			crsr.close();
			return "UNKNOWN";

		} else {

			crsr.moveToNext();
			name = crsr.getString(crsr.getColumnIndex(Contacts.DISPLAY_NAME));

			crsr.close();
			return name;
		}
	}

	public void updateToSeen(String phoneNumber) {

		ContentValues cv = new ContentValues();
		cv.put(KEY_SEEN, 1);
		myDB.update(DB_TABLE, cv, KEY_PHNO + " = '" + phoneNumber + "'", null);
	}

	public String getBody(int pos, String phoneNumber) {

		Cursor c = myDB.rawQuery(" SELECT " + KEY_BODY + " FROM " + DB_TABLE
				+ " WHERE " + KEY_PHNO + "='" + phoneNumber + "' ORDER BY "
				+ KEY_TIME + " DESC;", null);
		
		c.moveToPosition(pos);
		
		return c.getString(c.getColumnIndex(KEY_BODY));
	}
}
