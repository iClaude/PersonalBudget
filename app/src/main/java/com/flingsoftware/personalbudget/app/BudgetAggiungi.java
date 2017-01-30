/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.utility.Animazioni;
import com.flingsoftware.personalbudget.utility.SoundEffectsManager;
import com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche;
import com.flingsoftware.personalbudget.valute.ElencoValute;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_CORRENTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.ID_DATEPICKER;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_CODICE;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_SIMBOLO;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_TASSO;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTADEFAULT_CODICE;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTA_CODICE;
import static com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.CostantiPubbliche.AZIONE_RECUPERA_CAMBIO;
import static com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.CostantiPubbliche.EXTRA_CAMBIO;

public class BudgetAggiungi extends ActionBarActivity implements DatePickerFragment.DialogFinishedListener {
	
	//costanti
	private static final int DATA_INIZIO = 0;
	private static final int DATA_FINE = 1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.budget_aggiungi);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		//inizializzazione variabili di istanza
		voce = new String();
		dataInizioBudgetGreg = new GregorianCalendar();
		dataFineBudgetGreg = new GregorianCalendar();
		dataInizioBudgetGreg.setTimeInMillis(FunzioniComuni.getDataAttuale());
		dataFineBudgetGreg.setTimeInMillis(FunzioniComuni.getDataAttuale());
		
		//gestione valute
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		valutaPrincipale = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		ricavaValutaCorrente();
		tassoCambio = 1f;
		Currency curValuta = Currency.getInstance(valutaCorrente);
		((Button) findViewById(R.id.budget_aggiungi_bValuta)).setText(curValuta.getSymbol());
		//imposta sezione conversione valute
		impostaSezioneConversioneValute();	
				
		//aggiunta budget nuovo
		((TextView) findViewById(R.id.budget_aggiungi_tvTitolo)).setText(R.string.budget_aggiungi_aggiungibudget);
		
		//ottengo i reference ai vari widget
		etImportoBudget = (EditText) findViewById(R.id.budget_aggiungi_etImportoBudget);
		etImportoBudget.setTextColor(Color.RED);
		Spinner spRipetizioni = (Spinner) findViewById(R.id.budget_aggiungi_spRipetizione);
		Button bTagsSpecifici = (Button) findViewById(R.id.budget_aggiungi_bTagsSpecifici);
		Button bAllTagsExcept = (Button) findViewById(R.id.budget_aggiungi_bAllTagsExcept);
		Button bAllTags = (Button) findViewById(R.id.budget_aggiungi_bAllTags);
		etTags = (AutoCompleteTextView) findViewById(R.id.budget_aggiungi_etTag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.card_view_importo).setElevation(16);
        }

		//imposto i listener gli EditText
		etImportoBudget.addTextChangedListener(etImportoListener);
		findViewById(R.id.budget_aggiungi_etDataInizio).setOnClickListener(etDataInizioListener);
		findViewById(R.id.budget_aggiungi_etDataFine).setOnClickListener(etDataFineListener);
		
		//imposto l'adapter per la casella dei tag autocompletante
		dbcSpeseVociPerAutocomplete = new DBCSpeseVoci(this);
		dbcSpeseVociPerAutocomplete.openLettura();
		etTagAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, new String[] {"voce"}, new int[] { android.R.id.text1 }, 0);
		etTagAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
		    public Cursor runQuery(CharSequence constraint) {

		        if (constraint == null || constraint.equals(""))
		            return etTagAdapter.getCursor();
		        
		        Cursor curVoceFiltrata = dbcSpeseVociPerAutocomplete.getVociContenentiStringa(constraint.toString());
		        
		        return curVoceFiltrata;
		    }
		});
		etTagAdapter.setCursorToStringConverter(new CursorToStringConverter() {
			@Override
		    public CharSequence convertToString(Cursor c) {
				return c.getString(c.getColumnIndexOrThrow("voce"));
			}
		});
		etTags.setAdapter(etTagAdapter);
		
		//riquadro delle ripetizioni
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ripetizioni_budget, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spRipetizioni.setAdapter(adapter);
		spRipetizioni.setOnItemSelectedListener(spRipetizioniListener);
		ripetizione = "una_tantum";
				
		//impostazioni EditText date iniziale
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
		EditText etDataInizio = (EditText) findViewById(R.id.budget_aggiungi_etDataInizio);
		etDataInizio.setText(getResources().getString(R.string.menu_inizia_da) + " " +  df.format(dataInizioBudgetGreg.getTime()));
		EditText etDataFine = (EditText) findViewById(R.id.budget_aggiungi_etDataFine);
		etDataFine.setText(getResources().getString(R.string.menu_termina_a) + " " + df.format(dataFineBudgetGreg.getTime()));
		
		//listener per i bottoni dei tag
		bTagsSpecifici.setOnClickListener(bTagsSpecificiListener);
		bAllTagsExcept.setOnClickListener(bAllTagsExceptListener);
		bAllTags.setOnClickListener(bAllTagsListener);
		
		etImportoBudget.requestFocus();
		
		//gestione delle schede di inserimento dati
		findViewById(R.id.budget_aggiungi_tlImporto_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.budget_aggiungi_tlTags_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.budget_aggiungi_tlRipetizione_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.budget_aggiungi_tlPeriodo_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.budget_aggiungi_tlRisparmi_tableRowTitolo).setOnClickListener(titoliSchedeListener);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_budgetaggiungi, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_budgetAggiungi_OK:
			conferma();

			return true;	
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//recupero l'elenco delle voci di spesa in un thread separato
		new RecuperaVociSpesaTask().execute((Object) null);
	}
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		//deregistro il LocalBroadcastReceiver per risparmiare risorse
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.unregisterReceiver(mLocalReceiverRecuperaCambio);
		
		//nascondo la progressbar_standard per la valuta e la relativa label
		findViewById(R.id.budget_aggiungi_pbAggiornamento).setVisibility(View.GONE);
		findViewById(R.id.budget_aggiungi_tvConnessioneYahoo).setVisibility(View.INVISIBLE);
		findViewById(R.id.budget_aggiungi_tvConversione).setVisibility(View.VISIBLE);
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (conferma) {
			soundEffectsManager.playSound(SoundEffectsManager.SOUND_ADDED);
		}
	}


	/*
	 * Ricavo la valuta principale salvata nelle preferenze.
	 */
	private void ricavaValutaCorrente() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		valutaCorrente = pref.getString(VALUTA_CORRENTE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}
	
	
	/*
	 * Imposto la sezione relativa alla conversione delle valute estere.
	 */
	private void impostaSezioneConversioneValute() {
		if(valutaCorrente.equals(valutaPrincipale)) {
			findViewById(R.id.budget_aggiungi_pbAggiornamento).setVisibility(View.GONE);
			findViewById(R.id.budget_aggiungi_tvConnessioneYahoo).setVisibility(View.INVISIBLE);
			findViewById(R.id.budget_aggiungi_tvConversione).setVisibility(View.VISIBLE);
		
			Currency curValuta = Currency.getInstance(valutaCorrente);
			((TextView) findViewById(R.id.budget_aggiungi_tvConversione)).setText("1 " + curValuta.getSymbol() + " = 1 " + curValuta.getSymbol());
		}
		else {
			findViewById(R.id.budget_aggiungi_pbAggiornamento).setVisibility(View.VISIBLE);
			findViewById(R.id.budget_aggiungi_tvConnessioneYahoo).setVisibility(View.VISIBLE);
			findViewById(R.id.budget_aggiungi_tvConversione).setVisibility(View.INVISIBLE);
			aggiornaAnteprimaConversione();
			
			//ricerco su Yahoo Finance il cambio di oggi
	    	Intent intRecuperaCambio = new Intent(AZIONE_RECUPERA_CAMBIO);
            intRecuperaCambio.setClass(this, com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.class);
	    	intRecuperaCambio.putExtra(ELENCO_VALUTE_VALUTA_CODICE, valutaCorrente);
	    	intRecuperaCambio.putExtra(ELENCO_VALUTE_VALUTADEFAULT_CODICE, valutaPrincipale);
	    	startService(intRecuperaCambio);
	    	
	    	//registro il BroadcastReceiver (local) per ottenere il tasso di cambio
	    	attivaLocalBroadcastReceiverRecuperaCambio();
		}
	}
	
	
	/*
	 * BroadcastReceiver che riceve il tasso di cambio comunicato da RecuperaCambioIntentService.
	 */
	private void attivaLocalBroadcastReceiverRecuperaCambio() {
		//se ho gi� ottenuto il cambio da Yahoo non lo cerco di nuovo
		if(tassoCambio != 1) return;
		
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiPubbliche.LOCAL_BROADCAST_RECUPERA_CAMBIO);
		mLocalReceiverRecuperaCambio = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				float result = intent.getFloatExtra(EXTRA_CAMBIO, -1);

				if(result == -1) {
					new MioToast(BudgetAggiungi.this, getString(R.string.toast_valute_dettaglioValuta_erroreRecuperoCambio)).visualizza(Toast.LENGTH_SHORT);
				}
				else {
					tassoCambio = result;
					//salvo il tasso di cambio nelle preferenze
					SharedPreferences prefCambi = getSharedPreferences("cambi", MODE_PRIVATE);
					SharedPreferences.Editor prefCambiEditor = prefCambi.edit();
					prefCambiEditor.putFloat(valutaCorrente, tassoCambio);
					prefCambiEditor.apply();
										
					//aggiorno la GUI
					findViewById(R.id.budget_aggiungi_pbAggiornamento).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_tvConnessioneYahoo).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_tvConversione).setVisibility(View.VISIBLE);
				
					aggiornaAnteprimaConversione();
				}
				
				localBroadcastManager.unregisterReceiver(mLocalReceiverRecuperaCambio);
				
			}		
		};
		localBroadcastManager.registerReceiver(mLocalReceiverRecuperaCambio, intentFilter);
	}
	
	
	/*
	 * Listener per l'EditText etImporto, per la conversione istantanea delle valute.
	 */
	private TextWatcher etImportoListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				importoBudget = Double.parseDouble(etImportoBudget.getText().toString());
			} 
			catch (NumberFormatException exc) {
				importoBudget = 0;
			}
			
			aggiornaAnteprimaConversione();
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
	};
	
	
	/*
	 * Aggiorno la TextView con l'anteprima della conversione valuta.
	 */
	private void aggiornaAnteprimaConversione() {
		importoBudgetValprin = importoBudget * tassoCambio;
		
		NumberFormat nf1 = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf1.setCurrency(Currency.getInstance(valutaCorrente));
		NumberFormat nf2 = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf2.setCurrency(Currency.getInstance(valutaPrincipale));
		
		String conversione = nf1.format(importoBudget) + " = " + nf2.format(importoBudgetValprin);
		((TextView) findViewById(R.id.budget_aggiungi_tvConversione)).setText(conversione);
	}
	
	
	//listener di ibCalc: lancia la calcolatrice
	public void lanciaCalcolatrice(View view) {
		ArrayList<HashMap<String,Object>> items =new ArrayList<HashMap<String,Object>>();
		final PackageManager pm = getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);  
		for (PackageInfo pi : packs) {
			if( pi.packageName.toString().toLowerCase(Locale.getDefault()).contains("calcul")) {
			    HashMap<String, Object> map = new HashMap<String, Object>();
			    map.put("appName", pi.applicationInfo.loadLabel(pm));
			    map.put("packageName", pi.packageName);
			    items.add(map);
			}
		}
		
		if(items.size()>=1) {
			String packageName = (String) items.get(0).get("packageName");
			Intent i = pm.getLaunchIntentForPackage(packageName);
			if (i != null) {
			  startActivity(i);
			} 
			else {
				new MioToast(BudgetAggiungi.this, getString(R.string.aggiungi_voce_appCalcolatriceNonTrovata)).visualizza(Toast.LENGTH_SHORT);
			}
		}
	}
	
	
	//listener per il bottone delle valute
	public void scegliValuta(View view) {
	    Intent intScegliValuta = new Intent(this, ElencoValute.class);
	    startActivityForResult(intScegliValuta, 0);
	}
	
	
	//ricavo i dati della valuta scelta
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			//recupero dati passati dall'Activity ElencoValute
			valutaCorrente = data.getStringExtra(DETTAGLIO_VALUTA_CODICE);
			String simboloValuta = data.getStringExtra(DETTAGLIO_VALUTA_SIMBOLO);
			tassoCambio = data.getFloatExtra(DETTAGLIO_VALUTA_TASSO, 1f);
			
			//imposto simbolo sul bottone di scelta valuta
			((Button) findViewById(R.id.budget_aggiungi_bValuta)).setText(simboloValuta);
			
			//aggiorno la sezione conversione valute
			findViewById(R.id.budget_aggiungi_pbAggiornamento).setVisibility(View.GONE);
			findViewById(R.id.budget_aggiungi_tvConnessioneYahoo).setVisibility(View.INVISIBLE);
			findViewById(R.id.budget_aggiungi_tvConversione).setVisibility(View.VISIBLE);
			aggiornaAnteprimaConversione();
			
			//registro la valuta corrente nelle preferenze
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor prefEditor = pref.edit();
			prefEditor.putString(VALUTA_CORRENTE, valutaCorrente);
			prefEditor.apply();
		}
		else {
			impostaSezioneConversioneValute();
		}
	}
	
	
	//listener per i bottoni dei tag
	public OnClickListener bTagsSpecificiListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			voce = "";
			AlertDialog.Builder vociBuilder = new AlertDialog.Builder(BudgetAggiungi.this);
			vociBuilder.setTitle(R.string.budget_aggiungi_selezionavocispecifichetitolo);
			vociBuilder.setMultiChoiceItems(arrVoci, null, new DialogInterface.OnMultiChoiceClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if(isChecked) {
						if(!voce.contains(arrVoci[which])) {
							voce = voce + arrVoci[which] + ",";
						}
					}
					else {
						if(voce.contains(arrVoci[which])) {
							voce = voce.replace(arrVoci[which] + ",", "");
						}
					}
	
				}
			});
			vociBuilder.setPositiveButton(R.string.budget_aggiungi_selezionavocispecificheok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					etTags.setText(voce);
				}
			});
			AlertDialog vociDialog = vociBuilder.create();
			vociDialog.show();
		}
	};
	
	
	public OnClickListener bAllTagsExceptListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//metto in voce tutti i tag presenti
			voce = "";
			for(String tmp : arrVoci) {
				voce = voce + tmp + ",";
			}
			
			//modifico voce a secondo degli elementi selezionati dalla lista
			AlertDialog.Builder vociBuilder = new AlertDialog.Builder(BudgetAggiungi.this);
			vociBuilder.setTitle(R.string.budget_aggiungi_selezionatuttitagseccettotitolo);
			vociBuilder.setMultiChoiceItems(arrVoci, null, new DialogInterface.OnMultiChoiceClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if(isChecked) {
						if(voce.contains(arrVoci[which])) {
							voce = voce.replace(arrVoci[which] + ",", "");
						}
					}
					else {
						if(!voce.contains(arrVoci[which])) {
							voce = voce + arrVoci[which] + ",";
						}
					}
	
				}
			});
			vociBuilder.setPositiveButton(R.string.budget_aggiungi_selezionavocispecificheok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					etTags.setText(voce);
				}
			});
			AlertDialog vociDialog = vociBuilder.create();
			vociDialog.show();
		}
	};
	
	
	public OnClickListener bAllTagsListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			voce = "";
			for(String tmp : arrVoci) {
				voce = voce + tmp + ",";
			}
			etTags.setText(voce);
		}
	};
	
	
	//listener per lo Spinner delle ripetizioni
	private AdapterView.OnItemSelectedListener spRipetizioniListener = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			String ripetizioni[] = {"una_tantum", "giornaliero", "settimanale", "bisettimanale", "mensile", "annuale"};
			ripetizione = ripetizioni[pos];
			
			impostaDataFinale();
			etImportoBudget.clearFocus();
		}
		
	    public void onNothingSelected(AdapterView<?> parent) {
	        // Another interface callback
	    }
	};
	
	
	/*
	 * imposta la data finale del budget (dataFineBudgetGreg) tenendo conto della data iniziale e
	 * del tipo di ripetizione
	 */
	private void impostaDataFinale() {
		if(ripetizione.equals("settimanale")) {
			findViewById(R.id.budget_aggiungi_etDataFine).setEnabled(false);
			dataFineBudgetGreg = new GregorianCalendar();
			dataFineBudgetGreg.setTimeInMillis(dataInizioBudgetGreg.getTimeInMillis());
			dataFineBudgetGreg.add(GregorianCalendar.WEEK_OF_YEAR, 1);
			dataFineBudgetGreg.add(GregorianCalendar.DATE, -1);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
			EditText etDataFine = (EditText) findViewById(R.id.budget_aggiungi_etDataFine);
			etDataFine.setText(getResources().getString(R.string.menu_termina_a) + " " + df.format(dataFineBudgetGreg.getTime()));
		}
		else if(ripetizione.equals("bisettimanale")) {
			findViewById(R.id.budget_aggiungi_etDataFine).setEnabled(false);
			dataFineBudgetGreg = new GregorianCalendar();
			dataFineBudgetGreg.setTimeInMillis(dataInizioBudgetGreg.getTimeInMillis());
			dataFineBudgetGreg.add(GregorianCalendar.WEEK_OF_YEAR, 2);
			dataFineBudgetGreg.add(GregorianCalendar.DATE, -1);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
			EditText etDataFine = (EditText) findViewById(R.id.budget_aggiungi_etDataFine);
			etDataFine.setText(getResources().getString(R.string.menu_termina_a) + " " + df.format(dataFineBudgetGreg.getTime()));
		}
		else if(ripetizione.equals("mensile")) {
			findViewById(R.id.budget_aggiungi_etDataFine).setEnabled(false);
			dataFineBudgetGreg = new GregorianCalendar();
			dataFineBudgetGreg.setTimeInMillis(dataInizioBudgetGreg.getTimeInMillis());
			dataFineBudgetGreg.add(GregorianCalendar.MONTH, 1);
			dataFineBudgetGreg.add(GregorianCalendar.DATE, -1);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
			EditText etDataFine = (EditText) findViewById(R.id.budget_aggiungi_etDataFine);
			etDataFine.setText(getResources().getString(R.string.menu_termina_a) + " " + df.format(dataFineBudgetGreg.getTime()));
		}
		else if(ripetizione.equals("annuale")) {
			findViewById(R.id.budget_aggiungi_etDataFine).setEnabled(false);
			dataFineBudgetGreg = new GregorianCalendar();
			dataFineBudgetGreg.setTimeInMillis(dataInizioBudgetGreg.getTimeInMillis());
			dataFineBudgetGreg.add(GregorianCalendar.YEAR, 1);
			dataFineBudgetGreg.add(GregorianCalendar.DATE, -1);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
			EditText etDataFine = (EditText) findViewById(R.id.budget_aggiungi_etDataFine);
			etDataFine.setText(getResources().getString(R.string.menu_termina_a) + " " + df.format(dataFineBudgetGreg.getTime()));
		}
		else if(ripetizione.equals("giornaliero")) {
			String dataIniziale = ((EditText) findViewById(R.id.budget_aggiungi_etDataInizio)).getText().toString();
			((EditText) findViewById(R.id.budget_aggiungi_etDataFine)).setText(dataIniziale);
			findViewById(R.id.budget_aggiungi_etDataFine).setEnabled(false);
			dataFineBudgetGreg.setTimeInMillis(dataInizioBudgetGreg.getTimeInMillis());
		}
		else if(ripetizione.equals("una_tantum")) {
			findViewById(R.id.budget_aggiungi_etDataFine).setEnabled(true);
		}
	}
	
	
	//listener per i bottoni delle date
	public OnClickListener etDataInizioListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putInt(ID_DATEPICKER, DATA_INIZIO);
			dataFragment.setArguments(args);
			
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	public OnClickListener etDataFineListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putInt(ID_DATEPICKER, DATA_FINE);
			dataFragment.setArguments(args);
			
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	//implementazione di DatePickerFragment.DialogFinishedListener per ricevere la data scelta
	public void onDialogFinished(int id, int year, int month, int day) {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
		
		switch(id) {
		case DATA_INIZIO:
				dataInizioBudgetGreg.set(GregorianCalendar.DATE, day);
				dataInizioBudgetGreg.set(GregorianCalendar.MONTH, month);
				dataInizioBudgetGreg.set(GregorianCalendar.YEAR, year);
				EditText etDataInizio = (EditText) findViewById(R.id.budget_aggiungi_etDataInizio);
				etDataInizio.setText(getResources().getString(R.string.menu_inizia_da) + " " + df.format(dataInizioBudgetGreg.getTime()));
				
				impostaDataFinale();
				
			break;
		case DATA_FINE:
			dataFineBudgetGreg.set(GregorianCalendar.DATE, day);
			dataFineBudgetGreg.set(GregorianCalendar.MONTH, month);
			dataFineBudgetGreg.set(GregorianCalendar.YEAR, year);
			EditText etDataFine = (EditText) findViewById(R.id.budget_aggiungi_etDataFine);
			etDataFine.setText(getResources().getString(R.string.menu_termina_a) + " " + df.format(dataFineBudgetGreg.getTime()));
			
			break;
		}
	}
	
	
	//listener per il ToggleButton
	public void onToggleClicked(View view) {
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	        aggiungereRimanenza = 1;
	    } else {
	    	aggiungereRimanenza = 0;
	    }
	    
	    etImportoBudget.clearFocus();
	}


	//AsyncTask per recuperare l'elenco dei tag delle spese
	private class RecuperaVociSpesaTask extends AsyncTask<Object, Object, Object> {
		
		protected Object doInBackground(Object... params) {
			DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(BudgetAggiungi.this);
			dbcSpeseVoci.openLettura();
			Cursor curVoci = dbcSpeseVoci.getTutteLeVoci();
			
			arrVoci = new String[curVoci.getCount()];
			int i = 0;
			while(curVoci.moveToNext()) {
				arrVoci[i] = curVoci.getString(curVoci.getColumnIndex("voce"));
				i++;
			}
			
			etTagAdapter.changeCursor(curVoci);
			
			curVoci.close();
			dbcSpeseVoci.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {

		}
	}
	
	
	//aggiunta budget
	private void conferma() {
		//date
		if(dataInizioBudgetGreg.after(dataFineBudgetGreg)) {
			new MioToast(BudgetAggiungi.this, getString(R.string.periodo_errore)).visualizza(Toast.LENGTH_SHORT);
			
			return;
		}
		
		//non posso inserire un budget gi� scaduto!
		if(FunzioniComuni.getDataAttuale() > dataFineBudgetGreg.getTimeInMillis()) {
			new MioToast(BudgetAggiungi.this, getString(R.string.budget_aggiungi_errore_budgetScaduto)).visualizza(Toast.LENGTH_SHORT);
			
			return;
		}
		
		//importo
		try {
			importoBudget = Double.parseDouble(etImportoBudget.getText().toString());
		}
		catch (NumberFormatException exc) {
			importoBudget = 0;
		}
		if(importoBudget == 0) {
			new MioToast(BudgetAggiungi.this, getString(R.string.budget_aggiungi_erroreImportoZero)).visualizza(Toast.LENGTH_SHORT);
			
			return;
		}
		importoBudgetValprin = importoBudget * tassoCambio;
		
		voce = etTags.getText().toString();
		if(voce.length() == 0) {
			new MioToast(BudgetAggiungi.this, getString(R.string.toast_budgetAggiungi_noTag)).visualizza(Toast.LENGTH_SHORT);
			
			return;
		}
		if(voce.endsWith(",")) {
			voce = voce.substring(0, voce.length() - 1);
		}
		
		new AggiungiNuovoBudgetTask().execute((Object[]) null);
		new AggiungiNuoveVociTask().execute((Object[]) null);
		
		conferma = true;
		setResult(RESULT_OK);
		finish();
	}
	
	
	//AsyncTask per inserire il nuovo budget nella tabella spese_budget
	private class AggiungiNuovoBudgetTask extends AsyncTask<Object, Object, Boolean> {
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetAggiungi.this);
		DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(BudgetAggiungi.this);
		
		protected Boolean doInBackground(Object... params) {
			try {
				//calcolo il campo spesa_sost per il budget da inserire
				double spesaSostBudget = 0.0;
				dbcSpeseSostenute.openLettura();
				
				StringTokenizer st = new StringTokenizer(voce, ",");
				while(st.hasMoreTokens()) {
					String voceSpesa = st.nextToken();
					Cursor curSpesaTotale = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXTotale(dataInizioBudgetGreg.getTimeInMillis(), dataFineBudgetGreg.getTimeInMillis(), voceSpesa);
					curSpesaTotale.moveToFirst();
					spesaSostBudget += curSpesaTotale.getDouble(curSpesaTotale.getColumnIndex("totale_spesa"));
					curSpesaTotale.close();
				}
				dbcSpeseSostenute.close();
				
				long id;
				dbcSpeseBudget.openModifica();
				id = dbcSpeseBudget.inserisciSpesaBudget(voce, ripetizione, importoBudget, valutaCorrente, importoBudgetValprin, dataInizioBudgetGreg.getTimeInMillis(), dataFineBudgetGreg.getTimeInMillis(), aggiungereRimanenza, spesaSostBudget, importoBudgetValprin - spesaSostBudget, 0, 0);
				if(!ripetizione.equals("una_tantum")) {
					//primo inserisco il budget per ottenere l'id del record, poi lo aggiorno per valorizzare il campo budget_iniziale	
					dbcSpeseBudget.aggiornaSpesaBudget(id, voce, ripetizione, importoBudget, valutaCorrente, importoBudgetValprin, dataInizioBudgetGreg.getTimeInMillis(), dataFineBudgetGreg.getTimeInMillis(), aggiungereRimanenza, spesaSostBudget, importoBudgetValprin - spesaSostBudget, id, 1);
				}
				dbcSpeseBudget.close();
			}
			catch(Exception exc) {
				return false;
			}
			
			return true;
		}
		
		protected void onPostExecute(Boolean result) {
			if(!result) {
				new MioToast(BudgetAggiungi.this, getString(R.string.toast_errore_inserimento)).visualizza(Toast.LENGTH_SHORT);
			}
			else {
				new MioToast(BudgetAggiungi.this, getString(R.string.toast_budget_aggiunto)).visualizza(Toast.LENGTH_SHORT);
				
				final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
				sendBroadcast(intAggiornaWidget);
			}
		}
	}
	
	
	//AsyncTask per inserire i nuovi tag nella tabella spese_voci
	private class AggiungiNuoveVociTask extends AsyncTask<Object, Object, Object> {
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(BudgetAggiungi.this);
		
		protected Object doInBackground(Object... params) {
			dbcSpeseVoci.openModifica();
			StringTokenizer st = new StringTokenizer(voce, ",");
			while(st.hasMoreTokens()) {
				try {
					dbcSpeseVoci.inserisciVoceSpesa(st.nextToken(), 0);
				}
				catch(Exception exc) { 
					//se la voce � gi� presente ok
				}
			}
			dbcSpeseVoci.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			
		}
	}
	
	
	//grafica schede inserimento dati: espansione e compressione
	private OnClickListener titoliSchedeListener = new OnClickListener() {
		boolean importoEspanso = true;
		boolean tagsEspanso = false;
		boolean periodoEspanso = false;
		boolean ripetizioneEspanso = false;
		boolean risparmiEspanso = false;

		Animazioni animazioni = Animazioni.getInstance();
		
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.budget_aggiungi_tlImporto_tableRowTitolo:
				if(importoEspanso) {
					findViewById(R.id.budget_aggiungi_tlImporto_tableRow0).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_tlImporto_tableRow1).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaImporto), importoEspanso);
					findViewById(R.id.budget_aggiungi_tlImporto_tableRowBordo).setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_importo).setElevation(6);
                    }
                    importoEspanso = false;
				}
				else {
					findViewById(R.id.budget_aggiungi_tlImporto_tableRow0).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_tlImporto_tableRow1).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaImporto), importoEspanso);
					findViewById(R.id.budget_aggiungi_tlImporto_tableRowBordo).setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_importo).setElevation(16);
                    }
                    importoEspanso = true;
				}
				
				break;
				
			case R.id.budget_aggiungi_tlTags_tableRowTitolo:
				if(tagsEspanso) {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaTags), tagsEspanso);
					findViewById(R.id.budget_aggiungi_tlTags_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_tlTags_tableRow0).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_llTagsBottoni).setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_tags).setElevation(6);
                    }
                    tagsEspanso = false;
				}
				else {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaTags), tagsEspanso);
					findViewById(R.id.budget_aggiungi_tlTags_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_tlTags_tableRow0).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_llTagsBottoni).setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_tags).setElevation(16);
                    }
                    tagsEspanso = true;
				}
				
				break;
				
			case R.id.budget_aggiungi_tlPeriodo_tableRowTitolo:
				if(periodoEspanso) {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaData), periodoEspanso);
					findViewById(R.id.budget_aggiungi_tlPeriodo_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_etDataInizio).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_etDataFine).setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_periodo).setElevation(6);
                    }
                    periodoEspanso = false;
				}
				else {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaData), periodoEspanso);
					findViewById(R.id.budget_aggiungi_tlPeriodo_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_etDataInizio).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_etDataFine).setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_periodo).setElevation(16);
                    }
                    periodoEspanso = true;
				}
				
				break;
				
			case R.id.budget_aggiungi_tlRisparmi_tableRowTitolo:
				if(risparmiEspanso) {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaRisparmi), risparmiEspanso);
					findViewById(R.id.budget_aggiungi_tlRisparmi_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_tvRimanenza).setVisibility(View.GONE);
					findViewById(R.id.budget_aggiungi_tbRimanenza).setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_risparmi).setElevation(6);
                    }
                    risparmiEspanso = false;
				}
				else {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaRisparmi), risparmiEspanso);
					findViewById(R.id.budget_aggiungi_tlRisparmi_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_tlRisparmi_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_tvRimanenza).setVisibility(View.VISIBLE);
					findViewById(R.id.budget_aggiungi_tbRimanenza).setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_risparmi).setElevation(16);
                    }
                    risparmiEspanso = true;
				}
				
				break;
				
			case R.id.budget_aggiungi_tlRipetizione_tableRowTitolo:
				if(ripetizioneEspanso) {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaRipetizione), ripetizioneEspanso);
					findViewById(R.id.budget_aggiungi_tlRipetizione_tableRowBordo).setVisibility(View.GONE);
                    findViewById(R.id.budget_aggiungi_spRipetizione).setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_ripetizione).setElevation(6);
                    }
                    ripetizioneEspanso = false;
				}
				else {
					animazioni.ruotaFreccia(findViewById(R.id.budget_aggiungi_ivFrecciaRipetizione), ripetizioneEspanso);
					findViewById(R.id.budget_aggiungi_tlRipetizione_tableRowBordo).setVisibility(View.VISIBLE);
                    findViewById(R.id.budget_aggiungi_spRipetizione).setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById(R.id.card_view_ripetizione).setElevation(16);
                    }
                    ripetizioneEspanso = true;
				}
				
				break;
			}
		}
	};

	
	//variabili di istanza
	//generali
	private double importoBudget;
	private double importoBudgetValprin;
	private String voce;
	private String ripetizione;
	private GregorianCalendar dataInizioBudgetGreg;
	private GregorianCalendar dataFineBudgetGreg;
	private int aggiungereRimanenza;
	private String arrVoci[];
	
	//gestione valute
	private String valutaPrincipale;
	private String valutaCorrente;
	private float tassoCambio;
	private BroadcastReceiver mLocalReceiverRecuperaCambio;
	
	//gestione suoni
	private SoundEffectsManager soundEffectsManager = SoundEffectsManager.getInstance();
	private boolean conferma;
	
	//gestione dei tags
	private DBCSpeseVoci dbcSpeseVociPerAutocomplete;
	private SimpleCursorAdapter etTagAdapter;
	
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	
	private EditText etImportoBudget;
	private AutoCompleteTextView etTags;
}
