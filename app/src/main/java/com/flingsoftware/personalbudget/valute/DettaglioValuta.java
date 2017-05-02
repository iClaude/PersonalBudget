/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.valute;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;

import java.text.DateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTADEFAULT_CODICE;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTA_CODICE;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTA_NOME;
import static com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.CostantiPubbliche.AZIONE_RECUPERA_CAMBIO;
import static com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.CostantiPubbliche.EXTRA_CAMBIO;


public class DettaglioValuta extends ActionBarActivity {
	
	//costanti pubbliche
	public interface CostantiPubbliche {
		String LOCAL_BROADCAST_RECUPERA_CAMBIO = "localBroadcastRecuperaCambio";
		String DETTAGLIO_VALUTA_TASSO = "tassoCambio";
		String DETTAGLIO_VALUTA_CODICE = "codiceValuta";
		String DETTAGLIO_VALUTA_SIMBOLO = "simboloValuta";
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.valute_dettagliovaluta);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		
		//recupero i dati (nome e codice valuta) passati dall'Activity chiamante
		Bundle extras = getIntent().getExtras();
		nomeValuta = extras.getString(ELENCO_VALUTE_VALUTA_NOME);
		codiceValuta = extras.getString(ELENCO_VALUTE_VALUTA_CODICE);
		
		//recupero la valuta principale dal file delle preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		codValutaPrincipale = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		
		//ricavo l'ultimo tasso di cambio utilizzato per la valuta selezionata
		SharedPreferences cambiSalvati = getSharedPreferences("cambi", MODE_PRIVATE);
		tassoCambio = cambiSalvati.getFloat(codiceValuta, 1f);
		tassoCambioPers = tassoCambio;
		
		//imposto i dati dei controlli del layout
		((TextView) findViewById(R.id.valute_dettagliovaluta_tvNomeValuta)).setText(nomeValuta + " - " + codiceValuta);
		((TextView) findViewById(R.id.valute_dettagliovaluta_tvTassoCambio)).setText(Float.valueOf(tassoCambio).toString());
		((TextView) findViewById(R.id.valute_dettagliovaluta_tvEsempioConversione)).setText("1 " + codiceValuta + " = " + Float.valueOf(tassoCambio) + " " + codValutaPrincipale);
		((EditText) findViewById(R.id.valute_dettagliovaluta_etCambioPersonalizzato)).setText(Float.valueOf(tassoCambioPers).toString());
		
		//listener per l'EditText del cambio personalizzato
		((EditText) findViewById(R.id.valute_dettagliovaluta_etCambioPersonalizzato)).addTextChangedListener(etCambioPersonalizzatoListener);
	
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//ricavo la valuta di default dal file delle preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String codiceValutaDefault = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		
		//lancio l'IntentService per recuperare il tasso di cambio della valuta selezionata
    	Intent intRecuperaCambio = new Intent(AZIONE_RECUPERA_CAMBIO);
        intRecuperaCambio.setClass(this, com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.class);
    	intRecuperaCambio.putExtra(ELENCO_VALUTE_VALUTA_CODICE, codiceValuta);
    	intRecuperaCambio.putExtra(ELENCO_VALUTE_VALUTADEFAULT_CODICE, codiceValutaDefault);
    	startService(intRecuperaCambio);
    	
    	//registro il BroadcastReceiver (local) per ottenere il tasso di cambio
    	attivaLocalBroadcastReceiverRecuperaCambio();
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		
		//deregistro il LocalBroadcastReceiver per risparmiare risorse
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.unregisterReceiver(mLocalReceiverRecuperaCambio);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_valute_dettagliovaluta, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_valuteDettaglioValuta_OK:
			conferma();

			return true;	
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	//listener per l'EditText del cambio personalizzato
	private TextWatcher etCambioPersonalizzatoListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			try {
				tassoCambioPers = Float.parseFloat(arg0.toString());
			}
			catch(NumberFormatException exc) {
				tassoCambioPers = 0f;
			}
			
			aggiornaInterfaccia();
		}
	};
	
	
	//aggiorno la GUI quando cambia il tasso di cambio
	private void aggiornaInterfaccia() {
		if(tassoCambioPers == 0) {
			tassoCambioPers = tassoCambio;
		}
		
		((TextView) findViewById(R.id.valute_dettagliovaluta_tvTassoCambio)).setText(Float.valueOf(tassoCambio).toString());
		((TextView) findViewById(R.id.valute_dettagliovaluta_tvEsempioConversione)).setText("1 " + codiceValuta + " = " + Float.valueOf(tassoCambioPers) + " " + codValutaPrincipale);
	}
	
	
	/*
	 * BroadcastReceiver che riceve il tasso di cambio comunicato da RecuperaCambioIntentService.
	 */
	private void attivaLocalBroadcastReceiverRecuperaCambio() {
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiPubbliche.LOCAL_BROADCAST_RECUPERA_CAMBIO);
		mLocalReceiverRecuperaCambio = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				float result = intent.getFloatExtra(EXTRA_CAMBIO, -1);

				if(result == -1) {
					new MioToast(DettaglioValuta.this, getString(R.string.toast_valute_dettaglioValuta_erroreRecuperoCambio)).visualizza(Toast.LENGTH_SHORT);
				}
				else {
					tassoCambio = result;
					tassoCambioPers = tassoCambio;
					String ultimoTassoNoto = getResources().getString(R.string.valute_dettagliovaluta_tassoDiCambio) + " (" + DateFormat.getDateInstance(DateFormat.SHORT, miaLocale).format(new Date()) + "):";
					
					//nascondo LinearLayout con la ProgressBar
					findViewById(R.id.valute_dettagliovaluta_llAggiornamentoCambio).setVisibility(View.GONE);
					
					((TextView) findViewById(R.id.valute_dettagliovaluta_tvUltimoCambio)).setText(ultimoTassoNoto);
					((TextView) findViewById(R.id.valute_dettagliovaluta_tvTassoCambio)).setText(Float.valueOf(tassoCambio).toString());
					((TextView) findViewById(R.id.valute_dettagliovaluta_tvEsempioConversione)).setText("1 " + codiceValuta + " = " + Float.valueOf(tassoCambio) + " " + codValutaPrincipale);
					((EditText) findViewById(R.id.valute_dettagliovaluta_etCambioPersonalizzato)).setText(Float.valueOf(tassoCambioPers).toString());
				
					//salvo il tasso di cambio nelle preferenze
					SharedPreferences cambiSalvati = getSharedPreferences("cambi", MODE_PRIVATE);
					SharedPreferences.Editor cambiEditor = cambiSalvati.edit();
					cambiEditor.putFloat(codiceValuta, tassoCambioPers);
					cambiEditor.apply();
				}
				
				localBroadcastManager.unregisterReceiver(mLocalReceiverRecuperaCambio);
				
			}		
		};
		localBroadcastManager.registerReceiver(mLocalReceiverRecuperaCambio, intentFilter);
	}
	
	
	private void conferma() {
		Currency valutaScelta = Currency.getInstance(codiceValuta);
		String simboloValuta = valutaScelta.getSymbol();
		
		//comunico i dettagli della valuta all'Activity chiamante
		Intent intRitorno = new Intent();
		intRitorno.putExtra(CostantiPubbliche.DETTAGLIO_VALUTA_CODICE, codiceValuta);
		intRitorno.putExtra(CostantiPubbliche.DETTAGLIO_VALUTA_SIMBOLO, simboloValuta);
		intRitorno.putExtra(CostantiPubbliche.DETTAGLIO_VALUTA_TASSO, tassoCambioPers);
		setResult(Activity.RESULT_OK, intRitorno);
		
		finish();
	}
	
	
	//variabili di istanza
	private String nomeValuta;
	private String codiceValuta;
	private String codValutaPrincipale;
	private float tassoCambio;
	private float tassoCambioPers;
	private BroadcastReceiver mLocalReceiverRecuperaCambio;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
}
