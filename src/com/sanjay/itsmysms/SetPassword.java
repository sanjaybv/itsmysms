package com.sanjay.itsmysms;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetPassword extends Activity implements OnClickListener {

	static SharedPreferences data;
	EditText selfphno, pwd, conpwd;
	Button save;

	ArrayList<String> phnList = new ArrayList<String>();
	ArrayList<String> keyPhnList = new ArrayList<String>();
	ArrayList<String> NameList = new ArrayList<String>();

	public static final String BODY = "body";
	public static final String ADDRESS = "address";
	public static final String DATE = "date";

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_password);

		selfphno = (EditText) findViewById(R.id.etSelfNumber);
		pwd = (EditText) findViewById(R.id.etPassword);
		conpwd = (EditText) findViewById(R.id.etConfirmPassword);
		save = (Button) findViewById(R.id.bSave);

		save.setOnClickListener(this);

	}

	public void onClick(View v) {

		if (pwd.getText().toString().equals(conpwd.getText().toString())) {

			if (selfphno.getText().toString().length() > 4) {

				Toast.makeText(getApplicationContext(),
						"Password has been set", Toast.LENGTH_LONG).show();

				data = getSharedPreferences(Password.PASS_FILE, 0);
				SharedPreferences.Editor editor = data.edit();
				editor.putString("password", pwd.getText().toString());
				editor.putString("selfphno", selfphno.getText().toString());
				editor.putString("signature", "");
				editor.commit();

				final ProgressDialog progDailog = ProgressDialog.show(this,
						"Processing", "hold on...", true, true);

				new Thread(new Runnable() {
					public void run() {
						// your loading code goes here
						updateMessages();
					}
				}).start();

				Handler progressHandler = new Handler() {

					public void handleMessage(Message msg1) {

						progDailog.dismiss();
					}
				};

				Intent myIntent = new Intent("com.sanjay.itsmysms.PASSWORD");
				startActivity(myIntent);

			} else {

				Toast.makeText(getApplicationContext(),
						"Please enter a valid phone number.", Toast.LENGTH_LONG)
						.show();

			}

		} else {

			Toast.makeText(getApplicationContext(),
					"Password doesnt match. Please enter again.",
					Toast.LENGTH_LONG).show();

		}
	}

	@Override
	protected void onPause() {

		this.finish();
		super.onPause();
	}

	void updateMessages() {

		ContentResolver contentResolver = getContentResolver();
		Cursor cursor = null;
		DBRow newRow = new DBRow();
		SmsDB entry = new SmsDB(SetPassword.this);
		entry.open();

		for (int i = 0; i < 2; i++) {

			if (i == 0) {
				cursor = contentResolver.query(
						Uri.parse("content://sms/inbox"), null, null, null,
						null);
			} else {
				cursor = contentResolver.query(Uri.parse("content://sms/sent"),
						null, null, null, null);
			}

			int iBody = cursor.getColumnIndex(BODY);
			int iAddr = cursor.getColumnIndex(ADDRESS);
			int iTime = cursor.getColumnIndex(DATE);
			String checkno0;

			if (iBody < 0 || !cursor.moveToFirst())
				return;

			do {

				String checkbody = cursor.getString(iBody);
				if (i == 0)
					checkno0 = cursor.getString(iAddr);
				else
					checkno0 = selfphno.getText().toString();
				if (checkno0.length() > 4) {
					String checkno = checkno0.substring(checkno0.length() - 3);

					try {
						Integer.parseInt(checkno);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						continue;
					}

					int flag = StringCryptor.checkfor(checkno, checkbody);

					if (flag == 1) {
						String finalbody;
						finalbody = StringCryptor.detach(flag, checkbody);
						newRow.body = finalbody;// cursor.getString(iBody);

						newRow.phno = cursor.getString(iAddr);

						newRow.time = Long.parseLong(cursor.getString(iTime));
						newRow.sentrec = i;
						newRow.seen = 0;

						entry.createRecord(newRow);
					}
				}

			} while (cursor.moveToNext());
		}

		KeyDB keyentry = new KeyDB(SetPassword.this);
		keyentry.open();
		// keyentry.deleteRecords();

		phnList = entry.getPhoneList();

		for (String phno : phnList) {
			String newName = getName(phno);
			keyentry.createRecord(phno, "UNKNOWN", newName);
		}

		keyentry.close();
		entry.close();

	}

	private String getName(String phno) {

		String name = null;
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phno));
		String[] proj = new String[] { Contacts.DISPLAY_NAME };

		String sortOrder1 = StructuredPostal.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor crsr = getContentResolver().query(lookupUri, proj, null, null,
				sortOrder1);

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

}
