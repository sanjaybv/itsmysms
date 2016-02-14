package com.sanjay.itsmysms;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelpPageTwo extends Activity implements OnClickListener {
	
	SharedPreferences data;
	Button b1, b2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_page_2);
		
		b1 = (Button) findViewById(R.id.bBack);
		b1.setOnClickListener(this);
		
		b2 = (Button) findViewById(R.id.bFinish);
		b2.setOnClickListener(this);
	}

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.bFinish:

			data = getSharedPreferences(Password.PASS_FILE, 0);
			String getString = data.getString("password", "UNKNOWN");

			if (getString.equals("UNKNOWN")) {

				Intent myIntent = new Intent(
						"com.sanjay.itsmysms.SETPASSWORD");
				startActivity(myIntent);

			} else {

				Intent myIntent = new Intent(
						"com.sanjay.itsmysms.CONVERSATIONLIST");
				startActivity(myIntent);

			}
			break;

		case R.id.bBack:

			Intent myIntent1 = new Intent("com.sanjay.itsmysms.HELPPAGE");
			startActivity(myIntent1);
			break;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}
}