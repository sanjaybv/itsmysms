package com.sanjay.itsmysms;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeKey extends Activity implements OnClickListener{

	String phoneNumber;
	EditText changedKey;
	Button save;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_key);
		
		Bundle gotBasket = getIntent().getExtras();
		phoneNumber = gotBasket.getString("PHNO");
		
		changedKey = (EditText) findViewById(R.id.etChangeKey);
		save = (Button) findViewById(R.id.bSaveChangedKey);
		
		save.setOnClickListener(this);
		
		KeyDB keyentry = new KeyDB(ChangeKey.this);
		keyentry.open();
		String newName = keyentry.getKey(phoneNumber);
		keyentry.close();
		
		if (!newName.equals("UNKNOWN")) {
			
			changedKey.setText(newName);
		}
	}

	public void onClick(View arg0) {

		KeyDB keychange = new KeyDB(ChangeKey.this);
		keychange.open();
		int rows = keychange.updateKey(changedKey.getText().toString(), phoneNumber);
		keychange.close();
		
		Toast.makeText(getApplicationContext(), rows + " Affected", Toast.LENGTH_SHORT).show();
		
		this.finish();
		
	}
}
