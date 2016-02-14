package com.sanjay.itsmysms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Intro extends Activity implements OnClickListener{

	Button b;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		
		b = (Button) findViewById(R.id.bOk);
		b.setOnClickListener(this);
	}
	
	public void onClick(View v) {

		Intent myIntent = new Intent("com.sanjay.itsmysms.HELPPAGE");
		startActivity(myIntent);
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}
}
