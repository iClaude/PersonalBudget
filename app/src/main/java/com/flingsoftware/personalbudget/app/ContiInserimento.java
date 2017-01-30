/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.Conto;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.utility.Animazioni;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.CONTO_DEFAULT;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.ID_DATEPICKER;


public class ContiInserimento extends ActionBarActivity implements DatePickerFragment.DialogFinishedListener {
	
	// Variabili
	private EditText etNome;
	private EditText etSaldo;
	private EditText etData;
	private CheckBox cbContoDefault;
	
	private GregorianCalendar dataSaldo;
	private DateFormat df;
	private Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private Conto conto;
	private MenuItem menuAggiungi;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conti_inserimento);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		etNome = (EditText) findViewById(R.id.ci_etNome);
		etSaldo = (EditText) findViewById(R.id.ci_etSaldo);
		etData = (EditText) findViewById(R.id.ci_etData);
		etData.setOnClickListener(etDataOnClickListener);
		cbContoDefault = (CheckBox) findViewById(R.id.ci_cbContoDefault);
		findViewById(R.id.ci_tlConto_tableRowTitolo).setOnClickListener(schedaListener);
		
		df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
		dataSaldo = new GregorianCalendar();
		conto = new Conto();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conti_inserimento, menu);
		menuAggiungi = menu.getItem(0);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
	        
	        return true;
		case R.id.menu_ci_OK:
			menuAggiungi.setEnabled(false);
			conferma();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	// Selezione della data tramite finestra di dialogo
	public OnClickListener etDataOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putInt(ID_DATEPICKER, 0);
			dataFragment.setArguments(args);
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	// Ritorno dalla finestra di dialogo di selezione della data
	@Override
	public void onDialogFinished(int id, int year, int month, int day) {
		dataSaldo.set(GregorianCalendar.DATE, day);
		dataSaldo.set(GregorianCalendar.MONTH, month);
		dataSaldo.set(GregorianCalendar.YEAR, year);
		
		etData.setText(df.format(dataSaldo.getTime()));
	}
	
	
	// Grafica scheda inserimento dati: espansione e compressione
	private OnClickListener schedaListener = new OnClickListener() {
		boolean schedaEspansa = true;
		Animazioni animazioni = Animazioni.getInstance();
		
		@Override
		public void onClick(View v) {
			if(schedaEspansa) {
				findViewById(R.id.ci_llContoControlli).setVisibility(View.GONE);
				animazioni.ruotaFreccia(findViewById(R.id.ci_ivFrecciaConto), schedaEspansa);
				findViewById(R.id.ci_tlConto_tableRowBordo).setVisibility(View.GONE);
				schedaEspansa = false;
			}
			else {
				findViewById(R.id.ci_llContoControlli).setVisibility(View.VISIBLE);
				animazioni.ruotaFreccia(findViewById(R.id.ci_ivFrecciaConto), schedaEspansa);
				findViewById(R.id.ci_tlConto_tableRowBordo).setVisibility(View.VISIBLE);
				schedaEspansa = true;
			}
		}
	};
	
	
	// Conferma aggiunta nuovo conto
	private void conferma() {
		if(etNome.getText() == null || etNome.getText().toString().length() == 0) {
			new MioToast(ContiInserimento.this, getString(R.string.conti_inserimento_nomeContoMancante)).visualizza(Toast.LENGTH_SHORT);
			menuAggiungi.setEnabled(true);
		}
		else {
			double saldo = 0;
			try {
				saldo = Double.parseDouble(etSaldo.getText().toString());
			} 
			catch (NumberFormatException exc) {
				saldo = 0;
			}
			
			conto.setConto(etNome.getText().toString());
			conto.setSaldo(saldo);
			conto.setDataSaldo(dataSaldo.getTimeInMillis());

			// E' questo il conto di default?
			if(cbContoDefault.isChecked()) {
				impostaContoDefault();
			}
			
			new AggiungiContoTask().execute((Object[]) null);
		}		
	}


	// Se questo è il conto di default lo salvo nelle preferenze.
	private void impostaContoDefault() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(CONTO_DEFAULT, conto.getConto());
		prefEditor.apply();
	}

	
	// Aggiunta nuova conto in un thread separato
	private class AggiungiContoTask extends AsyncTask<Object, Object, Object> {
		DBCConti dbcConti = new DBCConti(ContiInserimento.this);
		
		protected Object doInBackground(Object... params) {
			dbcConti.openModifica();
			dbcConti.inserisciConto(conto);
			dbcConti.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			new MioToast(ContiInserimento.this, getString(R.string.conti_inserimento_contoAggiunto)).visualizza(Toast.LENGTH_SHORT);
			setResult(Activity.RESULT_OK);
			finish();
		}
	}
}
