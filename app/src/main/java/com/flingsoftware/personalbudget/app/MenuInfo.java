package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.ActionBarActivity;


public class MenuInfo extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_info);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	public void goGooglePlay(View v) {
		Intent i = new Intent(android.content.Intent.ACTION_VIEW);
		i.setData(Uri.parse("https://play.google.com/store/apps/developer?id=Fling%20Software%C2%A9&hl=it"));
		startActivity(i);
	}
	
	
	public void goVersioni(View v) {
		Intent i = new Intent(this, MenuInfoVersioni.class);
		startActivity(i);
	}
	
	
	public void goFacebook(View v) {
		try {
			Intent i = new Intent(android.content.Intent.ACTION_VIEW);
			i.setData(Uri.parse("fb://page/288981957941733"));
			startActivity(i);
		}
		catch(Exception e) {
			Intent i = new Intent(android.content.Intent.ACTION_VIEW);
			i.setData(Uri.parse("https://www.facebook.com/personalbudgetapp"));
			startActivity(i);
		}
	}


}
