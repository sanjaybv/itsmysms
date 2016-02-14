package com.sanjay.itsmysms;

import java.net.URLDecoder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Password extends Activity implements OnClickListener {

	Button num1, num2, num3, num4, num5, num6, num7, num8, num9, num0, go,
			back;
	String currPass, hidPass, toNum;
	public final static String PASS_FILE = "itsmysmsPasswordFile";
	SharedPreferences data;
	TextView password;
	boolean sendToFlag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Intent i = getIntent();
		Uri intentUri = i.getData();

		if (i.getAction() != null) {
			if (i.getAction().equals("android.intent.action.SENDTO")) {

				@SuppressWarnings("deprecation")
				String to = URLDecoder.decode(String.valueOf(intentUri)
						.replace("smsto:", ""));
				toNum = to;
				sendToFlag = true;
			}
			if (i.getAction().equals("itsmysms.openconv")) {

				Bundle bAdd = i.getExtras();
				toNum = bAdd.getString("address");
				sendToFlag = true;
			}
		}

		// Toast.makeText(getApplicationContext(), to,
		// Toast.LENGTH_SHORT).show();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);

		currPass = "";
		hidPass = "";

		password = (TextView) findViewById(R.id.tvPassword);

		num1 = (Button) findViewById(R.id.bNum1);
		num2 = (Button) findViewById(R.id.bNum2);
		num3 = (Button) findViewById(R.id.bNum3);
		num4 = (Button) findViewById(R.id.bNum4);
		num5 = (Button) findViewById(R.id.bNum5);
		num6 = (Button) findViewById(R.id.bNum6);
		num7 = (Button) findViewById(R.id.bNum7);
		num8 = (Button) findViewById(R.id.bNum8);
		num9 = (Button) findViewById(R.id.bNum9);
		num0 = (Button) findViewById(R.id.bNum0);
		go = (Button) findViewById(R.id.bGo);
		back = (Button) findViewById(R.id.bBack);

		num1.setOnClickListener(this);
		num2.setOnClickListener(this);
		num3.setOnClickListener(this);
		num4.setOnClickListener(this);
		num5.setOnClickListener(this);
		num6.setOnClickListener(this);
		num7.setOnClickListener(this);
		num8.setOnClickListener(this);
		num9.setOnClickListener(this);
		num0.setOnClickListener(this);
		go.setOnClickListener(this);
		back.setOnClickListener(this);

	}

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.bNum1:

			currPass = currPass + "1";
			break;

		case R.id.bNum2:

			currPass = currPass + "2";
			break;

		case R.id.bNum3:

			currPass = currPass + "3";
			break;

		case R.id.bNum4:

			currPass = currPass + "4";
			break;

		case R.id.bNum5:

			currPass = currPass + "5";
			break;

		case R.id.bNum6:

			currPass = currPass + "6";
			break;

		case R.id.bNum7:

			currPass = currPass + "7";
			break;

		case R.id.bNum8:

			currPass = currPass + "8";
			break;

		case R.id.bNum9:

			currPass = currPass + "9";
			break;

		case R.id.bNum0:

			currPass = currPass + "0";
			break;

		case R.id.bBack:

			try {
				currPass = currPass.substring(0, currPass.length() - 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case R.id.bGo:

			currPass = "";
			if (!checkPassword()) {

				Toast.makeText(getApplicationContext(),
						"Sorry, wrong password!", Toast.LENGTH_LONG).show();
			}

			break;

		}

		hidPass="";
		for (int i = 0; i < currPass.length(); i++)
			hidPass += "*";
		password.setText(hidPass);

		Thread tPass = new Thread(new Runnable() {

			public void run() {

				checkPassword();
			}
		});
		tPass.start();

	}

	private boolean checkPassword() {

		data = getSharedPreferences(PASS_FILE, 0);
		String getString = data.getString("password", "UNKNOWN");

		if (!getString.equals(currPass))
			return false;
		else {

			if (!sendToFlag) {

				Intent myIntent = new Intent(
						"com.sanjay.itsmysms.CONVERSATIONLIST");
				startActivity(myIntent);

			} else {

				KeyDB forKey = new KeyDB(this);
				forKey.open();

				if (forKey.isExists(toNum)) {

					Intent myIntent = new Intent(
							"com.sanjay.itsmysms.CONVERSATION");
					Bundle basket = new Bundle();
					basket.putString("PHNO", toNum);
					myIntent.putExtras(basket);

					startActivity(myIntent);

				} else {
					Intent myIntent1 = new Intent("com.sanjay.itsmysms.COMPOSE");
					myIntent1.putExtra("PHNO", toNum);
					myIntent1.putExtra("BODY", "");
					startActivity(myIntent1);
				}

				forKey.close();
			}

			return true;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}
}
