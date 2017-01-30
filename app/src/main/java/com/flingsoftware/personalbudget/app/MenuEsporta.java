/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiNotifiche;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService;
import com.flingsoftware.personalbudget.utility.UtilityVarious;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_FINE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_INIZIO;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.AZIONE_CSV;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.AZIONE_PDF;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.AZIONE_XLS;


public class MenuEsporta extends ActionBarActivity {

	private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_esporta);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		//ricavo dataInizio e dataFine passati dall'Activity chiamante
		Bundle extras = getIntent().getExtras();
		dataInizio = extras.getLong(DATA_INIZIO);
		dataFine = extras.getLong(DATA_FINE);
		
		etEmail = (EditText) findViewById(R.id.menu_esporta_etEmail);
		((CheckBox) findViewById(R.id.menu_esporta_cbSpese)).setChecked(true);
		((CheckBox) findViewById(R.id.menu_esporta_cbEntrate)).setChecked(true);
		esportaSpese = true;
		esportaEntrate = true;
		formatoExport = AZIONE_PDF;
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_menuesporta, menu);
		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_menuEsporta_OK:
			controllaPermessiEEsporta();

			return true;	
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	//gestione dei CheckBox per la scelta delle tabelle da esportare
	public void onCheckboxClicked(View view) {
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    // Check which checkbox was clicked
	    switch(view.getId()) {
	        case R.id.menu_esporta_cbSpese:
				esportaSpese = checked;
	            break;
	        case R.id.menu_esporta_cbEntrate:
				esportaEntrate = checked;
	            break;
	    }
	}
	
	//scelta del formato di esportazione sui RadioButton
	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    switch(view.getId()) {
	        case R.id.menu_esporta_rbTesto:
	            if (checked) {
	            	formatoExport = AZIONE_CSV;
					((ImageView) findViewById(R.id.ivIcon)).setImageDrawable(getResources().getDrawable(R.drawable.img_formato_csv));
				}
	            
	            break;
	        case R.id.menu_esporta_rbExcel:
	            if (checked){
	            	formatoExport = AZIONE_XLS;
					((ImageView) findViewById(R.id.ivIcon)).setImageDrawable(getResources().getDrawable(R.drawable.img_formato_xls));
				}
	                
	            break;
	        case R.id.menu_esporta_rbPdf:
	            if (checked){
	            	formatoExport = AZIONE_PDF;
					((ImageView) findViewById(R.id.ivIcon)).setImageDrawable(getResources().getDrawable(R.drawable.img_formato_pdf));
				}
	                
	            break;
	    }
	}


	/*
	Verifico che la versione di Android non sia Marshmallow. In questo caso bisogna verificare se
	c'? il permesso dangerous WRITE_EXTERNAL_STORAGE ed eventualmente richiederlo all'utente.
	 */
	private void controllaPermessiEEsporta() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
			}
			else {
				esporta();
			}
		}
		else { // versione precedente a Marshmallow (permesso gi? concesso all'installazione)
			esporta();
		}
	}


	// L'utente ha concesso il permesso di scrittura o no? (Marshmallow)
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_WRITE_EXTERNAL_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					esporta();
				} else {
					UtilityVarious.visualizzaDialogOKAnnulla(this, getResources().getString(R.string.permessi_negati_titolo), getResources().getString(R.string.permessi_negati_descrizione), getResources().getString(R.string.ok), false, null, R.drawable.ic_action_warning, null);
				}
				return; // prova
			}
		}
	}


	//esportazione del database
	private void esporta() {
    	if(!esportaSpese && !esportaEntrate) {
			new MioToast(MenuEsporta.this, getString(R.string.toast_menuesporta_nessunatabellaselezionata)).visualizza(Toast.LENGTH_SHORT);
    		
    		return;
    	}
    	
		//creo notifica per l'operazione in background di esportazione del database
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_notifica);
		builder.setContentTitle(getString(R.string.notifica_esportaDatabase_titolo));
		builder.setContentText(getString(R.string.notifica_esportaDatabase_testo));
		builder.setProgress(0, 0, true);
		Notification notification = builder.build();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(CostantiNotifiche.NOTIFICA_ESPORTA_DATABASE, notification);

		// Start the MenuEsportaIntentService.
		Intent intent = MenuEsportaIntentService.makeIntent(this, formatoExport, esportaSpese, esportaEntrate, dataInizio, dataFine, etEmail.getText().toString());
    	MenuEsporta.this.startService(intent);

    	setResult(Activity.RESULT_OK);
    	finish();
	}
	
	
	//variabili d'istanza
	private boolean esportaSpese;
	private boolean esportaEntrate;
	private String formatoExport;
	private long dataInizio;
	private long dataFine;
	
	private EditText etEmail;
}
