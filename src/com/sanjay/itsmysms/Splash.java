package com.sanjay.itsmysms;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

public class Splash extends Activity {

	SharedPreferences data;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		Thread timer = new Thread() {

			public void run() {
				try {
					sleep(1500); // change it to 2000 later

				} catch (InterruptedException e) {
					e.printStackTrace();

				} finally {

					data = getSharedPreferences(Password.PASS_FILE, 0);
					String getString = data.getString("password", "UNKNOWN");

					if (getString.equals("UNKNOWN")) {

						Intent myIntent = new Intent(
								"com.sanjay.itsmysms.INTRO");
						startActivity(myIntent);

					} else {

						Intent myIntent = new Intent(
								"com.sanjay.itsmysms.PASSWORD");
						startActivity(myIntent);

					}

				}
			}
		};
		timer.start();

	}

	@Override
	protected void onPause() {

		this.finish();
		super.onPause();
	}

}
