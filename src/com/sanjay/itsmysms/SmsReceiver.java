package com.sanjay.itsmysms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;

@SuppressLint("NewApi")
public class SmsReceiver extends BroadcastReceiver {

	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";

	public static final String ADDRESS = "address";
	public static final String PERSON = "person";
	public static final String DATE = "date";
	public static final String READ = "read";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String BODY = "body";
	public static final String SEEN = "seen";

	public static final int MESSAGE_TYPE_INBOX = 1;
	public static final int MESSAGE_TYPE_SENT = 2;

	public static final int MESSAGE_IS_NOT_READ = 0;
	public static final int MESSAGE_IS_READ = 1;

	public static final int MESSAGE_IS_NOT_SEEN = 0;
	public static final int MESSAGE_IS_SEEN = 1;

	@Override
	public void onReceive(Context context, Intent intent) {

		Bundle extras = intent.getExtras();
		String fullBody = "";
		String tempAddr = "";
		int r = 0;

		String messages = "";

		if (extras != null) {
			// Get received SMS array
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
			String address = "";
			long time = 0;

			// Get ContentResolver object for pushing encrypted SMS to incoming
			// folder
			ContentResolver contentResolver = context.getContentResolver();

			for (int i = 0; i < smsExtra.length; ++i) {
				SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

				String body = sms.getMessageBody().toString();
				address = sms.getOriginatingAddress();
				time = sms.getTimestampMillis();

				fullBody += body;
			}

			String newAdd = address.substring(address.length() - 3);

			int flag = 0;

			try {
				Integer.parseInt(newAdd);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				flag = 1;
			}

			if (flag == 0) {
				r = StringCryptor.checkfor(newAdd, fullBody);

				if (r == 1) {

					putSmsToDatabase(fullBody, address, time, context);
					tempAddr = getName(address, contentResolver);
					if (tempAddr.equalsIgnoreCase("UNKNOWN")) {
						tempAddr = address;
					}
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							context).setSmallIcon(R.drawable.app_logo)
							.setContentTitle(tempAddr).setContentText(fullBody);

					mBuilder.setAutoCancel(true);
					// $Creates an explicit intent for an Activity in your app
					Intent resultIntent;

					if (Conversation.active || ConversationList.active) {
						
						//"com.sanjay.itsmysms.CONVERSATION"
						resultIntent = new Intent(context, Conversation.class);
						resultIntent.putExtra("PHNO", address);

					} else {
						resultIntent = new Intent(context, Password.class);
						resultIntent.putExtra("address", address);
					}

					resultIntent.setAction("itsmysms.openconv");

					// $The stack builder object will contain an artificial back
					// $stack for the
					// $started Activity.
					// $This ensures that navigating backward from the Activity
					// $leads out of
					// $your application to the Home screen.
					TaskStackBuilder stackBuilder = TaskStackBuilder
							.create(context);
					// Adds the back stack for the Intent (but not the Intent
					// itself)

					// stackBuilder.addParentStack(ConversationList.class);
					if (Conversation.active || ConversationList.active) {
						stackBuilder.addParentStack(Conversation.class);
					} else {
						stackBuilder.addParentStack(Password.class);
					}
					// Adds the Intent that starts the Activity to the top of
					// the stack
					stackBuilder.addNextIntent(resultIntent);

					PendingIntent resultPendingIntent = stackBuilder
							.getPendingIntent(0,
									PendingIntent.FLAG_UPDATE_CURRENT);

					mBuilder.setContentIntent(resultPendingIntent);
					mBuilder.setDeleteIntent(resultPendingIntent);

					NotificationManager mNotificationManager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);

					// mId allows you to update the notification later on.
					mNotificationManager.notify(5678,
							mBuilder.getNotification());

					/*
					 * Notification noti = new Notification.Builder(context)
					 * .setContentTitle( "New encrypted sms from " +
					 * address.toString()) .setContentText("")
					 * .setSmallIcon(R.drawable.app_logo)
					 * .setLargeIcon(null).build();
					 */
					/*
					 * Notification noti = new Notification(); PendingIntent
					 * resultIntent; noti.setLatestEventInfo(context,
					 * "Its MY SMS", "hi", resultIntent );
					 * 
					 * NotificationManager nm = (NotificationManager) context
					 * .getSystemService(Context.NOTIFICATION_SERVICE);
					 * nm.notify(561, noti);
					 */

					/*
					 * Toast.makeText(context,
					 * "An \"it's MY sms\" message received!",
					 * Toast.LENGTH_SHORT).show();
					 */

					if (Conversation.active)
						Conversation.displayMessages();

					if (ConversationList.active)
						ConversationList.displayPhoneList();

				}
			}

		}

	}

	// context.unregisterReceiver(this);

	private void putSmsToDatabase(String body, String address, long time,
			Context context) {

		body = StringCryptor.detach(1, body);

		SmsDB recEntry = new SmsDB(context);
		recEntry.open();

		DBRow row = new DBRow(address, time, 0, body, 0);

		recEntry.createRecord(row);
		recEntry.close();

		KeyDB kEntry = new KeyDB(context);
		kEntry.open();

		kEntry.createRecord(address, "UNKNOWN",
				getName(address, context.getContentResolver()));

		kEntry.close();
	}

	private String getName(String phno, ContentResolver contentResolver) {

		String name = null;
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phno));
		String[] proj = new String[] { Contacts.DISPLAY_NAME };

		String sortOrder1 = StructuredPostal.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		Cursor crsr = contentResolver.query(lookupUri, proj, null, null,
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
