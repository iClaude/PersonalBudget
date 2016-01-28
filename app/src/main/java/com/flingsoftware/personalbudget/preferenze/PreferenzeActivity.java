package com.flingsoftware.personalbudget.preferenze;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.support.v7.app.ActionBarActivity;

import com.flingsoftware.personalbudget.R;


public class PreferenzeActivity extends ActionBarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.preferenze);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		// Display the fragment as the main content.�
		getFragmentManager().beginTransaction()
			.replace(R.id.preferenze_fl, new PreferenzeFragment())
			.commit();

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
}
