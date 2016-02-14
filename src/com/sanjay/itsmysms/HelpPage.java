package com.sanjay.itsmysms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelpPage extends Activity implements OnClickListener{

	Button b;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_page);
		
		b = (Button) findViewById(R.id.bNext);
		b.setOnClickListener(this);
	}
	
	public void onClick(View v) {

		Intent myIntent = new Intent("com.sanjay.itsmysms.HELPPAGETWO");
		startActivity(myIntent);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}
}
