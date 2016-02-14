package com.sanjay.itsmysms;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePassword extends Activity implements OnClickListener {

	Button saveKey;
	EditText oldPass, newPass, conPass;

	SharedPreferences data;
	String oPass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);

		saveKey = (Button) findViewById(R.id.bSavePass);

		oldPass = (EditText) findViewById(R.id.etOldPass);

		newPass = (EditText) findViewById(R.id.etNewPass);

		conPass = (EditText) findViewById(R.id.etConPass);

		data = getSharedPreferences(Password.PASS_FILE, 0);
		oPass = data.getString("password", "UNKNOWN");

		saveKey.setOnClickListener(this);

	}

	public void onClick(View v) {

		if ((oldPass.getText().toString().equals(oPass))) {

			String nPass, cPass;
			nPass = newPass.getText().toString();
			cPass = conPass.getText().toString();

			if (nPass.equals(cPass)) {
				Toast.makeText(getApplicationContext(),
						"Password has been changed", Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor editor = data.edit();
				editor.putString("password", newPass.getText().toString());
				editor.commit();
				finish();

			} else {
				Toast.makeText(getApplicationContext(),
						"Password doesnt match. Please enter again.",
						Toast.LENGTH_SHORT).show();
			}

		} else {
			Toast.makeText(getApplicationContext(), "Sorry,Wrong Password",
					Toast.LENGTH_SHORT).show();
		}
		// TODO Auto-generated method stub

	}

	protected void onPause() {

		this.finish();
		super.onPause();
	}
}
