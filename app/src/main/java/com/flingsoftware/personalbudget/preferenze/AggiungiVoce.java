package com.flingsoftware.personalbudget.preferenze;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;


public class AggiungiVoce extends ActionBarActivity {
	
	//costanti
	public static final String EXTRA_NUOVA_ICONA = "nuova_icona";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferenze_aggiungivoce);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		iconeVeloci = new ListViewIconeVeloce(this);
		etVoce = (EditText) findViewById(R.id.preferenze_aggiungivoce_etVoce);
		ibIcona = ((ImageButton) findViewById(R.id.preferenze_aggiungivoce_ibIcon));
		
		new PlaceHolderWorkerTask().execute(R.drawable.tag_0, R.drawable.tag_1);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_preferenze_aggiungivoce, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		switch(item.getItemId()) {
		case R.id.menu_preferenze_aggiungivoce_conferma:
			String voce = etVoce.getText().toString();
			if(voce == null || voce.length() == 0) {
				new MioToast(AggiungiVoce.this, getString(R.string.toast_preferenze_aggiungivoce_inserireUnTag)).visualizza(Toast.LENGTH_SHORT);
			}
			else {
				if(voceSpesa) {
					new AggiungiVoceSpesaTask().execute(voce);
				}
				else {
					new AggiungiVoceEntrataTask().execute(voce);
				}
			}

			return true;
		case android.R.id.home:
			setResult(Activity.RESULT_CANCELED);
			finish();
	        
	        return true;     
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();

	    switch(view.getId()) {
	        case R.id.preferenze_aggiungivoce_rbSpese:
	            if (checked)
	                nuovaIcona = 0;
	            	voceSpesa = true;
	            	ibIcona.setImageBitmap(mPlaceHolderBitmapSpese);
	            break;
	        case R.id.preferenze_aggiungivoce_rbEntrate:
	            if (checked)
	            	nuovaIcona = 1;
	            	voceSpesa = false;
	            	ibIcona.setImageBitmap(mPlaceHolderBitmapEntrate);
	            break;
	    }
	}
	
	
	//clicco sull'icona per selezionare l'icona da un elenco
	public void selezionaIcona(View v) {
		Intent intent = new Intent(this, IconeSelezione.class);
		startActivityForResult(intent, 0);
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			nuovaIcona = data.getExtras().getInt(EXTRA_NUOVA_ICONA);
			iconeVeloci.loadBitmap(nuovaIcona, ibIcona, mPlaceHolderBitmapSpese, 100, 100);
		}
	}
	
	
	private class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {   
	    @Override
	    protected Object doInBackground(Integer... params) {
	    	mPlaceHolderBitmapSpese = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 100, 100);
	    	mPlaceHolderBitmapEntrate = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[1], 100, 100);
	    	return null;
	    }
	}
	
	
	//AsyncTask x aggiungere una nuova voce di spesa
	private class AggiungiVoceSpesaTask extends AsyncTask<String, Object, Boolean> {
		
		protected Boolean doInBackground(String... params) {
			boolean risultato = true;
			
			DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(AggiungiVoce.this);
			dbcSpeseVoci.openModifica();
			try {
				dbcSpeseVoci.inserisciVoceSpesaEccezione(params[0], nuovaIcona);
			}
			catch(Exception exc) {
				risultato = false;
			}
			dbcSpeseVoci.close();
			
			return risultato;
		}
		
		protected void onPostExecute(Boolean risultato) {
			if(risultato) {
				setResult(Activity.RESULT_OK);
				finish();
			}
			else {
				new MioToast(AggiungiVoce.this, getString(R.string.toast_preferenze_aggiungivoce_voceGiaPresente)).visualizza(Toast.LENGTH_SHORT);
			}
		}
	}
	
	
	//AsyncTask x aggiungere una nuova voce di entrata
	private class AggiungiVoceEntrataTask extends AsyncTask<String, Object, Boolean> {
		
		protected Boolean doInBackground(String... params) {
			boolean risultato = true;
			
			DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(AggiungiVoce.this);
			dbcEntrateVoci.openModifica();
			try {
				dbcEntrateVoci.inserisciVoceEntrataEccezione(params[0], nuovaIcona);
			}
			catch(Exception exc) {
				risultato = false;
			}
			dbcEntrateVoci.close();
			
			return risultato;
		}
		
		protected void onPostExecute(Boolean risultato) {
			if(risultato) {
				setResult(Activity.RESULT_OK);
				finish();
			}
			else {
				new MioToast(AggiungiVoce.this, getString(R.string.toast_preferenze_aggiungivoce_voceGiaPresente)).visualizza(Toast.LENGTH_SHORT);
			}
		}
	}
	
	
	//variabili di istanza
	private EditText etVoce;
	private ImageButton ibIcona;
	private boolean voceSpesa = true;
	private int nuovaIcona = 0;
	private ListViewIconeVeloce iconeVeloci;
	private Bitmap mPlaceHolderBitmapSpese;
	private Bitmap mPlaceHolderBitmapEntrate;
}
