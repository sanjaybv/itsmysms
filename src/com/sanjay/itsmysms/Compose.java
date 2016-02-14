package com.sanjay.itsmysms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

@TargetApi(12)
public class Compose extends Activity implements OnFocusChangeListener,
		OnItemClickListener, OnClickListener {

	private ArrayList<Map<String, String>> mPeopleList;

	private SimpleAdapter mAdapter;
	AutoCompleteTextView toName;
	EditText keyText, body, sKey;
	Button send;
	String dbPhoneNo = "", dbMesBody = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		Bundle basket = getIntent().getExtras();
		String phno = "";
		phno = basket.getString("PHNO");
		String fBody = "";
		fBody = basket.getString("BODY");

		phno = phno.replaceAll(" ", "");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);

		keyText = (EditText) findViewById(R.id.etKey);
		toName = (AutoCompleteTextView) findViewById(R.id.actvTo);
		mPeopleList = new ArrayList<Map<String, String>>();

		toName.setOnFocusChangeListener(this);
		toName.setOnItemClickListener(this);

		send = (Button) findViewById(R.id.bSend);
		send.setOnClickListener(Compose.this);

		body = (EditText) findViewById(R.id.etNewBody);
		sKey = (EditText) findViewById(R.id.etKey);

		body.setText(fBody);

		Thread tPop = new Thread(new Runnable() {

			public void run() {

				PopulatePeopleList();
			}
		});
		tPop.start();

		mAdapter = new SimpleAdapter(this, mPeopleList,
				R.layout.custom_content_view, new String[] { "Name", "Phone",
						"Type" }, new int[] { R.id.ccvName, R.id.ccvNumber,
						R.id.ccvType });

		toName.setAdapter(mAdapter);

		if (!phno.equals("")) {

			toName.setText(phno);
		}

	}

	// ////////////////
	public void sendSms(String phoneNumber, String message) {

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		dbPhoneNo = phoneNumber;
		dbMesBody = message;

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);
		ArrayList<PendingIntent> SentPIList = new ArrayList<PendingIntent>(1);
		SentPIList.add(sentPI);
		ArrayList<PendingIntent> DeliveredPIList = new ArrayList<PendingIntent>(
				1);
		DeliveredPIList.add(deliveredPI);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS sent",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No service",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}

		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS delivered",
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

		ContentValues values = new ContentValues();
		values.put("address", phoneNumber);
		values.put("body", message);

		this.getContentResolver().insert(Uri.parse("content://sms/sent"),
				values);

		SmsManager sms = SmsManager.getDefault();

		ArrayList<String> multiMessage = sms.divideMessage(message);

		sms.sendMultipartTextMessage(phoneNumber, null, multiMessage,
				SentPIList, DeliveredPIList);
		// sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	// ////////////////////

	public void PopulatePeopleList() {

		mPeopleList.clear();

		Cursor people = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		while (people.moveToNext()) {

			String contactName = people.getString(people
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

			String contactId = people.getString(people
					.getColumnIndex(ContactsContract.Contacts._ID));

			String hasPhone = people
					.getString(people
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if ((Integer.parseInt(hasPhone) > 0)) {

				// You know have the number so now query it like this
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);

				while (phones.moveToNext()) {

					// store numbers and display a dialog letting the user
					// select which.
					String phoneNumber = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					phoneNumber = phoneNumber.replaceAll(" ", "");

					String numberType = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

					Map<String, String> NamePhoneType = new HashMap<String, String>();

					NamePhoneType.put("Name", contactName);
					NamePhoneType.put("Phone", phoneNumber);

					if (numberType.equals("0"))
						NamePhoneType.put("Type", "Work");
					else if (numberType.equals("1"))
						NamePhoneType.put("Type", "Home");
					else if (numberType.equals("2"))
						NamePhoneType.put("Type", "Mobile");
					else
						NamePhoneType.put("Type", "Other");

					// Then add this map to the list.
					mPeopleList.add(NamePhoneType);
				}
				phones.close();
			}
		}
		people.close();

		// startManagingCursor(people);
	}

	@Override
	protected void onPause() {

		finish();
		super.onPause();
	}

	public void onFocusChange(View v, boolean f) {

		switch (v.getId()) {

		case R.id.actvTo:

			if (!f) {
				KeyDB checkKey = new KeyDB(this);
				checkKey.open();
				String oldKey = checkKey.getKey(toName.getText().toString());

				if (!oldKey.equals("UNKNOWN")) {

					keyText.setText(oldKey);
				}
				checkKey.close();
				break;
			}
		}
	}

	public void onItemClick(AdapterView<?> av, View v, int index, long arg3) {

		Map<String, String> map = (Map<String, String>) av
				.getItemAtPosition(index);
		Iterator<String> myIterator = map.keySet().iterator();
		while (myIterator.hasNext()) {
			String key = (String) myIterator.next();
			String value = (String) map.get(key);
			value = value.replaceAll(" ", "");
			toName.setText(value);
		}
	}

	public void onClick(View v) {

		// SmsManager sms = SmsManager.getDefault();
		// SmsMessage smsMsg = null;

		final String tempPhno = toName.getText().toString();
		String tempBody = body.getText().toString();
		final String tempKey = sKey.getText().toString();

		if (tempBody.equals("")) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						nSendsms(tempPhno,tempKey,"");
						break;
					case DialogInterface.BUTTON_NEGATIVE:

						dialog.cancel();
						return;
						// break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to send a blank message?")
					.setPositiveButton("Yes :P", dialogClickListener)
					.setNegativeButton("No :O", dialogClickListener).show();
		}else{
			nSendsms(tempPhno,tempKey,tempBody);
		}

	
		

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
	public void nSendsms(String tempPhno,String tempKey,String tempBody){

		KeyDB keychange = new KeyDB(this);
		keychange.open();

		if (!keychange.isExists(tempPhno)) {

			keychange.createRecord(tempPhno, tempKey, getName(tempPhno));
		}

		keychange.close();

		SharedPreferences data;
		data = getSharedPreferences(Password.PASS_FILE, 0);
		String selfno = data.getString("selfphno", "UNKNOWN");
		String lastNo = selfno.substring(selfno.length() - 3);
		String signature = data.getString("signature", "");
		tempBody = tempBody + "\n" + signature;

		try {
			tempBody = StringCryptor.encrypt(tempKey, tempBody);
			String attachedBody = StringCryptor.attach(lastNo, tempBody);
			//
			sendSms(tempPhno, attachedBody);
			//
			// sms.sendTextMessage(tempPhno, null, attachedBody, pi, null);

		} catch (Exception e) {

			e.printStackTrace();
		}

		long tempTime = System.currentTimeMillis();
		DBRow newRow = new DBRow(tempPhno, tempTime, 1, tempBody, 1);

		SmsDB entrySent = new SmsDB(getApplicationContext());

		entrySent.open();

		entrySent.createRecord(newRow);

		entrySent.close();

		Bundle phno = new Bundle();
		Intent toConversation = new Intent("com.sanjay.itsmysms.CONVERSATION");
		phno.putString("PHNO", tempPhno);
		toConversation.putExtras(phno);
		startActivity(toConversation);
	}

}
