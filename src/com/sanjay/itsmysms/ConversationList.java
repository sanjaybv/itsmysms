package com.sanjay.itsmysms;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

public class ConversationList extends Activity implements OnClickListener,
		OnItemClickListener {

	public static final String BODY = "body";
	public static final String ADDRESS = "address";
	public static final String DATE = "date";

	private static ArrayList<Map<String, String>> mPeopleList;

	private Uri deleteUri = Uri.parse("content://sms");

	Button compose;

	SharedPreferences data;

	static Context myContext;

	Dialog dialog;

	int pos;

	static boolean active = false;

	ArrayList<String> phnList = new ArrayList<String>();
	static ArrayList<String> keyPhnList = new ArrayList<String>();
	ArrayList<String> NameList = new ArrayList<String>();

	static ListView convListView;
	String selfno;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation_list);

		myContext = this;

		compose = (Button) findViewById(R.id.bCompose);
		convListView = (ListView) findViewById(R.id.lvConv);

		compose.setOnClickListener(this);
		convListView.setOnItemClickListener(ConversationList.this);

		mPeopleList = new ArrayList<Map<String, String>>();

		registerForContextMenu(convListView);

		SharedPreferences data;
		data = getSharedPreferences(Password.PASS_FILE, 0);
		selfno = data.getString("selfphno", "UNKNOWN");

		displayPhoneList();

	}

	@Override
	public void onBackPressed() {

		active = false;
		this.finish();
		super.onBackPressed();
	}

	public static void displayPhoneList() {

		mPeopleList.clear();

		ArrayList<Map<String, String>> nameBodyList = new ArrayList<Map<String, String>>();

		/*
		 * KeyDB display = new KeyDB(myContext);
		 * 
		 * display.open(); keyPhnList = display.numberList(); nameBodyList =
		 * display.nameBodyMap(); display.close();
		 */

		SmsDB display = new SmsDB(myContext);

		display.open();
		keyPhnList = display.numberList();
		nameBodyList = display.nameBodyMap();
		display.close();

		// convListView.setAdapter(new ArrayAdapter<T>(this,
		// R.layout.list_item_conv, NameList));
		convListView.setAdapter(new SimpleAdapter(myContext, nameBodyList,
				R.layout.cust_conv_list,
				new String[] { "name", "body", "time" }, new int[] {
						R.id.tvLName, R.id.tvLBody, R.id.tvLTime }));

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

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.bCompose:

			Intent myIntent = new Intent("com.sanjay.itsmysms.COMPOSE");
			myIntent.putExtra("PHNO", "");
			myIntent.putExtra("BODY", "");

			startActivityForResult(myIntent, 0);

			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		MenuInflater upMenu = getMenuInflater();
		upMenu.inflate(R.menu.conversation_list_menu, menu);

		return true;
	}

	void makeDialog() {

		dialog = new Dialog(ConversationList.this);

		dialog.setContentView(R.layout.change_key);
		dialog.setTitle("Change the signature");

		final EditText siKey = (EditText) dialog.findViewById(R.id.etChangeKey);
		Button saveChKey = (Button) dialog.findViewById(R.id.bSaveChangedKey);

		siKey.setHint("Enter signature");
		data = getSharedPreferences(Password.PASS_FILE, 0);
		String oldSign = data.getString("signature", "");

		siKey.setText(oldSign);

		saveChKey.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				String newSignature = siKey.getText().toString();

				data = getSharedPreferences(Password.PASS_FILE, 0);
				SharedPreferences.Editor editor = data.edit();
				editor.putString("signature", newSignature);
				editor.commit();

				Toast.makeText(getApplicationContext(), "Signature changed!",
						Toast.LENGTH_SHORT).show();

				dialog.cancel();

			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.aboutUs:

			dialog = new Dialog(this);
			dialog.setContentView(R.layout.about_us);
			dialog.setTitle("About");

			Button mailTo = (Button) dialog.findViewById(R.id.bMail);

			mailTo.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					String mailid[] = { "androbuddies@gmail.com" };

					Intent emailIntent = new Intent(
							android.content.Intent.ACTION_SEND);
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							mailid);
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							"it's MY sms - Feedback");
					emailIntent.setType("plain/text");
					startActivity(emailIntent);

				}
			});
			dialog.show();

			break;

		case R.id.help:

			Intent i = new Intent("com.sanjay.itsmysms.HELPPAGE");
			startActivity(i);
			break;

		case R.id.changePassword:

			Intent myIntent = new Intent("com.sanjay.itsmysms.CHANGEPASSWORD");
			startActivity(myIntent);
			break;

		case R.id.changeSignature:
			makeDialog();
			dialog.show();

			break;
		case R.id.eXit:
			this.finish();
			break;

		}

		return false;

	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		Bundle basket = new Bundle();
		basket.putString("PHNO", keyPhnList.get(position));

		Intent myIntent = new Intent("com.sanjay.itsmysms.CONVERSATION");
		myIntent.putExtras(basket);

		startActivity(myIntent);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		if (v.getId() == R.id.lvConv) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			pos = info.position;
			// menu.setHeaderTitle("Options");
			menu.add(Menu.NONE, 0, 0, "Delete Conversation");
			menu.add(Menu.NONE, 1, Menu.NONE, "Call Contact");
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

						String delNumber = keyPhnList.get(pos);

						SmsDB getTimes = new SmsDB(getApplicationContext());
						getTimes.open();

						ArrayList<String> timestamps = getTimes
								.getTimeStamps(delNumber);
						
						

						for (String time : timestamps) {
							getApplicationContext().getContentResolver()
									.delete(deleteUri, "date=?",
											new String[] { time });

						}
						getTimes.deleteConv(delNumber);
						getTimes.close();

						KeyDB delNum = new KeyDB(getApplicationContext());
						delNum.open();
						delNum.deleteConv(delNumber);
						delNum.close();

						displayPhoneList();
						break;

					case DialogInterface.BUTTON_NEGATIVE:

						dialog.cancel();
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"This would delete the conversation from your phone. Are you sure?")
					.setPositiveButton("Yes :P", dialogClickListener)
					.setNegativeButton("No :O", dialogClickListener).show();

			break;

		case 1:

			String callNumber = keyPhnList.get(pos);

			String uri = "tel:" + callNumber.trim();
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse(uri));
			startActivity(intent);

			break;

		}

		return true;
	}

	@Override
	protected void onStart() {

		EasyTracker.getInstance().activityStart(this);
		super.onStart();
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
		displayPhoneList();
		super.onResume();
	}

	@Override
	protected void onPause() {

		active = false;
		super.onPause();
	}

}
