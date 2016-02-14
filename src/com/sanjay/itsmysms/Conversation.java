package com.sanjay.itsmysms;

import java.util.ArrayList;
import java.util.Map;

import javax.crypto.BadPaddingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class Conversation extends Activity implements
		android.content.DialogInterface.OnCancelListener, OnClickListener {

	static ArrayList<Map<String, String>> messages = new ArrayList<Map<String, String>>();
	ArrayList<String> decryptedMessages = new ArrayList<String>();
	long tempTime;

	private Uri deleteUri = Uri.parse("content://sms");

	static String phoneNumber;
	static String password;
	static ListView messageList;
	static TextView ph;
	EditText body;
	Button send;
	Context parent;
	static Context myContext;
	int pos;

	static boolean active = false;

	Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);

		Bundle gotBasket = getIntent().getExtras();
		phoneNumber = gotBasket.getString("PHNO");

		ph = (TextView) findViewById(R.id.tvSender);
		messageList = (ListView) findViewById(R.id.lvMessages);

		send = (Button) findViewById(R.id.bSend);
		body = (EditText) findViewById(R.id.etNewBody);
		send.setOnClickListener(this);

		registerForContextMenu(messageList);

		myContext = this;

		// showKey();

		KeyDB keyentry = new KeyDB(Conversation.this);
		keyentry.open();
		String newKey = keyentry.getKey(phoneNumber);
		keyentry.close();

		if (newKey.equals("UNKNOWN")) {

			makeDialog();
			dialog.show();
		}

		QuickContactBadge badge = (QuickContactBadge) findViewById(R.id.qcbPhoto);
		new QuickContactHelper(this, badge, phoneNumber).addThumbnail();
		badge.assignContactFromPhone(phoneNumber, false);

		SmsDB upSeen = new SmsDB(this);
		upSeen.open();
		upSeen.updateToSeen(phoneNumber);
		upSeen.close();

		displayMessages();

	}

	/*
	 * private void showKey() {
	 * 
	 * }
	 */

	@Override
	protected void onStart() {

		EasyTracker.getInstance().activityStart(this); // Add this method.

		super.onStart();
	}

	public static void displayMessages() {

		KeyDB keyentry = new KeyDB(myContext);
		keyentry.open();

		String newKey = keyentry.getKey(phoneNumber);
		keyentry.close();

		password = newKey;
		ph.setText(getName(phoneNumber));

		SmsDB msg = new SmsDB(myContext);
		msg.open();
		messages = msg.getSmsThread(phoneNumber);
		msg.close();

		messageList.setAdapter(new SimpleAdapter(myContext, messages,
				R.layout.cust_conv,
				new String[] { "contacter", "time", "body" }, new int[] {
						R.id.tvccName, R.id.tvccTime, R.id.tvccBody }));

	}

	public void onClick(View v) {

		String tempBody = body.getText().toString();

		if (tempBody.equals("")) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:

						nSendsms("");
						break;

					case DialogInterface.BUTTON_NEGATIVE:

						return;
						// break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to send a blank message?")
					.setPositiveButton("Yes :P", dialogClickListener)
					.setNegativeButton("No :O", dialogClickListener).show();
		} else {
			nSendsms(tempBody);
		}

		body.setText("");
		body.clearFocus();

	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {

		super.onCreateOptionsMenu(menu);
		MenuInflater upMenu = getMenuInflater();
		upMenu.inflate(R.menu.conversation_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.changeKey:

			makeDialog();
			dialog.show();

			break;
		}

		return false;
	}

	void makeDialog() {

		dialog = new Dialog(Conversation.this);

		dialog.setContentView(R.layout.change_key);
		dialog.setTitle("Change the key");

		final EditText chKey = (EditText) dialog.findViewById(R.id.etChangeKey);
		Button saveChKey = (Button) dialog.findViewById(R.id.bSaveChangedKey);

		KeyDB upKey = new KeyDB(Conversation.this);
		upKey.open();
		String oldKey = upKey.getKey(phoneNumber);
		if (!oldKey.equals("UNKNOWN"))
			chKey.setText(oldKey);
		upKey.close();

		saveChKey.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				KeyDB upKey = new KeyDB(Conversation.this);
				upKey.open();
				upKey.updateKey(chKey.getText().toString(), phoneNumber);
				upKey.close();

				Toast.makeText(getApplicationContext(), "Key changed!",
						Toast.LENGTH_SHORT).show();

				dialog.cancel();

			}
		});

		dialog.setOnCancelListener(this);
	}

	public void onCancel(DialogInterface dialog) {

		displayMessages();
	}

	public void sendSms(String phoneNumber, String message) {

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

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

				unregisterReceiver(this);
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

				unregisterReceiver(this);
			}
		}, new IntentFilter(DELIVERED));

		ContentValues values = new ContentValues();
		values.put("address", phoneNumber);
		values.put("body", message);
		values.put("date", tempTime);

		this.getContentResolver().insert(Uri.parse("content://sms/sent"),
				values);

		SmsManager sms = SmsManager.getDefault();

		ArrayList<String> multiMessage = sms.divideMessage(message);

		sms.sendMultipartTextMessage(phoneNumber, null, multiMessage,
				SentPIList, DeliveredPIList);
		// sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		if (v.getId() == R.id.lvMessages) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			pos = info.position;

			// menu.setHeaderTitle("Options");
			menu.add(Menu.NONE, 0, 0, "Delete Message");
			menu.add(Menu.NONE, 1, 0, "Forward");
		}
	}

	private static String getName(String phno) {

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

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case 0:

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:

						String delNumber = phoneNumber;

						SmsDB getTimes = new SmsDB(getApplicationContext());
						getTimes.open();

						String time = getTimes.getTime(pos, delNumber);

						getApplicationContext().getContentResolver().delete(
								deleteUri, "date=?", new String[] { time });

						getTimes.deleteConv(delNumber, time);
						getTimes.close();

						displayMessages();

						break;

					case DialogInterface.BUTTON_NEGATIVE:

						dialog.cancel();
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"This would delete the message from your phone. Are you sure?")
					.setPositiveButton("Yes :P", dialogClickListener)
					.setNegativeButton("No :O", dialogClickListener).show();

			break;

		case 1:

			String newDeBody = "";

			SmsDB forForward = new SmsDB(this);
			forForward.open();
			String newEnBody = forForward.getBody(pos, phoneNumber);
			forForward.close();

			boolean flag = true;

			KeyDB forKey = new KeyDB(this);
			forKey.open();
			String key = forKey.getKey(phoneNumber);
			forKey.close();

			try {
				newDeBody = StringCryptor.decrypt(key, newEnBody);

			} catch (BadPaddingException e) {

				flag = false;
				Toast.makeText(this, "Wrong key. Forwarding disabled. :P",
						Toast.LENGTH_SHORT).show();

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (flag) {

				Intent myIntent = new Intent("com.sanjay.itsmysms.COMPOSE");
				myIntent.putExtra("BODY", newDeBody);
				myIntent.putExtra("PHNO", "");

				startActivity(myIntent);

			}

			break;
		}

		return true;
	}

	public void nSendsms(String tempBody) {
		SharedPreferences data;
		data = getSharedPreferences(Password.PASS_FILE, 0);
		String selfno = data.getString("selfphno", "UNKNOWN");
		String lastNo = selfno.substring(selfno.length() - 3);
		String signature = data.getString("signature", "");
		tempBody = tempBody + "\n" + signature;
		try {
			KeyDB keyentry = new KeyDB(Conversation.this);
			keyentry.open();
			String nKey = keyentry.getKey(phoneNumber);
			keyentry.close();
			tempBody = StringCryptor.encrypt(nKey, tempBody);
			String attachedBody = StringCryptor.attach(lastNo, tempBody);

			tempTime = System.currentTimeMillis();
			//
			sendSms(phoneNumber, attachedBody);
			//
			// sms.sendTextMessage(tempPhno, null, attachedBody, pi, null);

		} catch (Exception e) {

			e.printStackTrace();
		}

		DBRow newRow = new DBRow(phoneNumber, tempTime, 1, tempBody, 1);

		SmsDB entrySent = new SmsDB(getApplicationContext());

		entrySent.open();

		entrySent.createRecord(newRow);

		entrySent.close();
		displayMessages();
	}

	@Override
	protected void onStop() {

		active = false;
		EasyTracker.getInstance().activityStop(this);

		super.onStop();

	}

	@Override
	protected void onResume() {

		active = true;
		super.onResume();
		displayMessages();
	}

	@Override
	protected void onPause() {

		active = false;
		super.onPause();
	}

}
