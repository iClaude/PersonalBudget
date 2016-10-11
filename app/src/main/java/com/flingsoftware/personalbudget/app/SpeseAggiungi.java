/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiSuoni;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCSpeseRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;
import com.flingsoftware.personalbudget.database.InserimentoMultiploIntentService;
import com.flingsoftware.personalbudget.oggetti.SpesaEntrata;
import com.flingsoftware.personalbudget.utilita.Animazioni;
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

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.CONTO_DEFAULT;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.TAG_SPESE_ULTIMO;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_CORRENTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.ID_DATEPICKER;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_CONTO;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_DATA;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_DESCRIZIONE;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_FAVORITE;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_ID;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_IMPORTO;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_IMPORTO_VALPRIN;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_RIPETIZIONE_ID;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_TAG;
import static com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche.VOCE_VALUTA;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_MODIFICA_SPESA;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_CODICE;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_SIMBOLO;
import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.DETTAGLIO_VALUTA_TASSO;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTADEFAULT_CODICE;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.ELENCO_VALUTE_VALUTA_CODICE;
import static com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.CostantiPubbliche.AZIONE_RECUPERA_CAMBIO;
import static com.flingsoftware.personalbudget.valute.RecuperaCambioIntentService.CostantiPubbliche.EXTRA_CAMBIO;


public class SpeseAggiungi extends ActionBarActivity implements DatePickerFragment.DialogFinishedListener {

	//costanti
	private static final int DATA_FINE_RIPETIZIONE = 0;
	private static final int DATA = 1;
	private static final int TIPO_OPERAZIONE_AGGIUNGI = 0;
	private static final int TIPO_OPERAZIONE_MODIFICA = 1;
	private static final int TIPO_OPERAZIONE_PREFERITO = 2;


	/*
	Create an Intent to start this Activity with the main variables set.
	 */
	public static Intent makeIntent(Context context, long id, String tag, double amount, String currency, double amountMainCurrency, long date, String description, long repetitionId, String account, int favorite) {
		Intent intent = new Intent(context, SpeseAggiungi.class);

		intent.putExtra(VOCE_ID, id);
		intent.putExtra(VOCE_TAG, tag);
		intent.putExtra(VOCE_IMPORTO, amount);
		intent.putExtra(VOCE_VALUTA, currency);
		intent.putExtra(VOCE_IMPORTO_VALPRIN, amountMainCurrency);
		intent.putExtra(VOCE_DATA, date);
		intent.putExtra(VOCE_DESCRIZIONE, description);
		intent.putExtra(VOCE_RIPETIZIONE_ID, repetitionId);
		intent.putExtra(VOCE_CONTO, account);
		intent.putExtra(VOCE_FAVORITE, favorite);

		return intent;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spese_entrate_aggiungi);

		// Toolbar per menu opzioni
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		//ricavo la valuta principale dal file delle preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		valutaPrincipale = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());

		//sto aggiungendo o modificando una spesa? quale?
		Bundle extras = getIntent().getExtras();
		if(extras == null) {
			tipoOperazione = TIPO_OPERAZIONE_AGGIUNGI;
			((TextView) findViewById(R.id.aggiungi_voce_tvTitolo)).setText(R.string.aggiungi_spese_aggiungi_spesa);

			ricavaValutaCorrente();
			tassoCambio = 1f;
		}
		else {
			favorite = extras.getInt(VOCE_FAVORITE);
			if(favorite == -1) {
				tipoOperazione = TIPO_OPERAZIONE_PREFERITO;
				((TextView) findViewById(R.id.aggiungi_voce_tvTitolo)).setText(R.string.aggiungi_spese_aggiungi_spesa);
			}
			else {
				tipoOperazione = TIPO_OPERAZIONE_MODIFICA;
				((TextView) findViewById(R.id.aggiungi_voce_tvTitolo)).setText(R.string.aggiungi_spese_modifica_spesa);
			}


			id = extras.getLong(VOCE_ID);
			tag = extras.getString(VOCE_TAG);
			tagVecchio = tag;

			importo = extras.getDouble(VOCE_IMPORTO);
			importoValprin = extras.getDouble(VOCE_IMPORTO_VALPRIN);
			valutaCorrente = extras.getString(VOCE_VALUTA);
			long dataIniziale = extras.getLong(VOCE_DATA);
			descrizione = extras.getString(VOCE_DESCRIZIONE);
			ripetizione_id = extras.getLong(VOCE_RIPETIZIONE_ID);
			conto = extras.getString(VOCE_CONTO);

			data = new GregorianCalendar();
			dataVecchia = new GregorianCalendar();
			data.setTimeInMillis(dataIniziale);
			dataVecchia.setTimeInMillis(dataIniziale);

			tassoCambio = (float) (importoValprin / importo);
		}

		//creo gli oggetti database connector per interagire con il database
		dbcSpeseSostenute = new DBCSpeseSostenute(this);
		dbcSpeseRipetute = new DBCSpeseRipetute(this);

		//ottengo i reference ai vari widget
		etTag = (AutoCompleteTextView) findViewById(R.id.aggiungi_voce_etTag);
		etImporto = (EditText) findViewById(R.id.aggiungi_voce_etImporto);
		etImporto.setTextColor(Color.RED);
		etDescrizione = (EditText) findViewById(R.id.aggiungi_voce_etDescrizione);
		etData = (EditText) findViewById(R.id.aggiungi_voce_etData);
		Spinner spRipetizione = (Spinner) findViewById(R.id.aggiungi_voce_spRipetizione);
		Spinner spFineRipetizione = (Spinner) findViewById(R.id.aggiungi_voce_spFineRipetizione);
		spConto = (Spinner) findViewById(R.id.aggiungi_voce_spConto);
		spTag = (Spinner) findViewById(R.id.aggiungi_voce_spTag);
		cbFavorite = (CheckBox) findViewById(R.id.cbFavorite);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			findViewById(R.id.card_view_importo).setElevation(16);
		}

		//imposto i listener per gli EditText
		etImporto.addTextChangedListener(etImportoListener);

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
		etTag.setAdapter(etTagAdapter);

		//imposto lo Spinner del conto
		String[] adapterCols=new String[]{"conto"};
		int[] adapterRowViews=new int[]{android.R.id.text1};
		spContoAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, adapterCols, adapterRowViews, 0);
		spContoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spConto.setAdapter(spContoAdapter);

		//imposto lo Spinner dei tag
		ArrayList<String> alTags = new ArrayList<String>();
		alTags.add(getResources().getString(R.string.generici_primoElemSpinner));
		spTagAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, alTags);
		spTagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dbcSpeseVoci = new DBCSpeseVoci(this);
		spTag.setAdapter(spTagAdapter);
		spTag.setOnItemSelectedListener(spTagListener);

		//imposto l'EditText della data
		df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
		oggi = new GregorianCalendar();
		int anno = oggi.get(GregorianCalendar.YEAR);
		int mese = oggi.get(GregorianCalendar.MONTH);
		int giorno = oggi.get(GregorianCalendar.DATE);
		oggi = new GregorianCalendar(anno, mese, giorno);
		ieri = new GregorianCalendar(anno, mese, giorno);
		ieri.add(GregorianCalendar.DATE, -1);
		dataFineRipetizione = new GregorianCalendar(2076, 4, 22);

		//imposto il riquadro delle ripetizioni
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ripetizioni, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spRipetizione.setAdapter(adapter);
		spRipetizione.setOnItemSelectedListener(spRipetizioneListener);
		ripetizione = "nessuna";

		ArrayAdapter<CharSequence> spFineRipetizioneAdapter = ArrayAdapter.createFromResource(this, R.array.ripetizioni_fine, android.R.layout.simple_spinner_item);
		spFineRipetizioneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spFineRipetizione.setAdapter(spFineRipetizioneAdapter);
		spFineRipetizione.setOnItemSelectedListener(spFineRipetizioneListener);

		if(tipoOperazione == TIPO_OPERAZIONE_AGGIUNGI) {
			data = new GregorianCalendar(anno, mese, giorno);
			ripetizione = "nessuna";
			Currency curValuta = Currency.getInstance(valutaCorrente);
			((Button) findViewById(R.id.aggiungi_voce_bValuta)).setText(curValuta.getSymbol());

			impostaTagDefault(pref);
			caricaSpinnerTipoRipetizione();
		}
		else if(tipoOperazione == TIPO_OPERAZIONE_MODIFICA || tipoOperazione == TIPO_OPERAZIONE_PREFERITO) {
			//imposto i widget con i valori esistenti
			if(favorite == 1) cbFavorite.setChecked(true);
			etImporto.setText(Double.valueOf(importo).toString());
			Currency curValuta = Currency.getInstance(valutaCorrente);
			((Button) findViewById(R.id.aggiungi_voce_bValuta)).setText(curValuta.getSymbol());
			etTag.setText(tag);
			etDescrizione.setText(descrizione);

			if(ripetizione_id != 1) {
				cbFavorite.setEnabled(false);
				new RecuperaRipetizioneTask().execute(ripetizione_id);
			}
			else {
				ripetizione = "nessuna";
				findViewById(R.id.aggiungi_voce_spFineRipetizione).setVisibility(View.GONE);
				findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).setVisibility(View.GONE);
			}

			if(tipoOperazione == TIPO_OPERAZIONE_PREFERITO) {
				data = new GregorianCalendar(anno, mese, giorno);
				findViewById(R.id.card_view_preferiti).setVisibility(View.GONE);
				cbFavorite.setChecked(false);
			}
		}

		etData.setText(df.format(data.getTime()));
		etData.setOnClickListener(etDataListener);

		//imposta sezione conversione valute
		impostaSezioneConversioneValute();

		etImporto.requestFocus();

		//gestione delle schede di inserimento dati
		findViewById(R.id.aggiungi_voce_tlImporto_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.aggiungi_voce_tlConto_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.aggiungi_voce_tlTags_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.aggiungi_voce_tlData_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.aggiungi_voce_tlDescrizione_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.aggiungi_voce_tlRipetizione_tableRowTitolo).setOnClickListener(titoliSchedeListener);
		findViewById(R.id.aggiungi_voce_tlPreferiti_tableRowTitolo).setOnClickListener(titoliSchedeListener);

		new CaricaSuoniTask().execute();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/*
	Verifico l'ultimo tag utilizzato nelle preferenze e uso questo per l'inserimento di una nuova
	spesa.
	 */
	private void impostaTagDefault(SharedPreferences pref) {
		String ultimoTag = pref.getString(TAG_SPESE_ULTIMO, null);
		if(ultimoTag != null) {
			etTag.setText(ultimoTag);
		}
	}

	/* Carico e visualizzo lo Spinner per selezionare il tipo di ripetizione: inserimento graduale o
   batch. Non va usato quando si modifica una spesa ripetuta (con inserimento graduale), perchè
   in questo caso bisogna prima cancellare la spesa.
 */
	private void caricaSpinnerTipoRipetizione() {
		// Inserimento multiplo o ripetizione
		Spinner spTipoRipetizione = (Spinner) findViewById(R.id.aggiungi_voce_spTipoRipetizione);
		spTipoRipetizione.setVisibility(View.VISIBLE);
		ArrayAdapter<CharSequence> adapterTipoRipet = ArrayAdapter.createFromResource(this, R.array.aggiungi_voce_tipo_ripetizione, android.R.layout.simple_spinner_item);
		adapterTipoRipet.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spTipoRipetizione.setAdapter(adapterTipoRipet);
		spTipoRipetizione.setOnItemSelectedListener(spTipoRipetizioneListener);
		inserimentoMultiplo = false;
	}

	@Override
	protected void onResume() {
		super.onResume();

		//ricarico lo Spinner dei tag ogni volta che la Activity ritorna in primo piano
		new RefreshTagsCursorTask().execute((Object[]) null); //caricamento valori dal database in un thread separato
		new RecuperaContiTask().execute((Object[]) null);
	}


	@Override
	protected void onPause() {
		super.onPause();

		//deregistro il LocalBroadcastReceiver per risparmiare risorse
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.unregisterReceiver(mLocalReceiverRecuperaCambio);

		//nascondo la progressbar_standard per la valuta e la relativa label
		findViewById(R.id.aggiungi_voce_pbAggiornamento).setVisibility(View.GONE);
		findViewById(R.id.aggiungi_voce_tvConnessioneYahoo).setVisibility(View.INVISIBLE);
		findViewById(R.id.aggiungi_voce_tvConversione).setVisibility(View.VISIBLE);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_speseentrateaggiungi, menu);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_speseEntrateAggiungi_OK:
			conferma();

			return true;
		case android.R.id.home:
			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
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
			findViewById(R.id.aggiungi_voce_pbAggiornamento).setVisibility(View.GONE);
			findViewById(R.id.aggiungi_voce_tvConnessioneYahoo).setVisibility(View.INVISIBLE);
			findViewById(R.id.aggiungi_voce_tvConversione).setVisibility(View.VISIBLE);

			Currency curValuta = Currency.getInstance(valutaCorrente);
			((TextView) findViewById(R.id.aggiungi_voce_tvConversione)).setText("1 " + curValuta.getSymbol() + " = 1 " + curValuta.getSymbol());
		}
		else {
			findViewById(R.id.aggiungi_voce_pbAggiornamento).setVisibility(View.VISIBLE);
			findViewById(R.id.aggiungi_voce_tvConnessioneYahoo).setVisibility(View.VISIBLE);
			findViewById(R.id.aggiungi_voce_tvConversione).setVisibility(View.INVISIBLE);
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
		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter(CostantiPubbliche.LOCAL_BROADCAST_RECUPERA_CAMBIO);
		mLocalReceiverRecuperaCambio = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				float result = intent.getFloatExtra(EXTRA_CAMBIO, -1);

				if(result == -1) {
					new MioToast(SpeseAggiungi.this, getString(R.string.toast_valute_dettaglioValuta_erroreRecuperoCambio)).visualizza(Toast.LENGTH_SHORT);
				}
				else {
					tassoCambio = result;
					//salvo il tasso di cambio nelle preferenze
					SharedPreferences prefCambi = getSharedPreferences("cambi", MODE_PRIVATE);
					SharedPreferences.Editor prefCambiEditor = prefCambi.edit();
					prefCambiEditor.putFloat(valutaCorrente, tassoCambio);
					prefCambiEditor.apply();

					//aggiorno la GUI
					findViewById(R.id.aggiungi_voce_pbAggiornamento).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_tvConnessioneYahoo).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_tvConversione).setVisibility(View.VISIBLE);

					aggiornaAnteprimaConversione();
				}

				localBroadcastManager.unregisterReceiver(mLocalReceiverRecuperaCambio);

			}
		};
		localBroadcastManager.registerReceiver(mLocalReceiverRecuperaCambio, intentFilter);
	}


	/*
	 * Aggiorno la TextView con l'anteprima della conversione valuta.
	 */
	private void aggiornaAnteprimaConversione() {
		double importoProvv = 0;
		try {
			importoProvv = Double.parseDouble(etImporto.getText().toString());
		}
		catch(Exception exc) {
			importoProvv = 0;
		}
		double importoConv = importoProvv * tassoCambio;

		NumberFormat nf1 = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf1.setCurrency(Currency.getInstance(valutaCorrente));
		NumberFormat nf2 = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf2.setCurrency(Currency.getInstance(valutaPrincipale));

		String conversione = nf1.format(importoProvv) + " = " + nf2.format(importoConv);
		((TextView) findViewById(R.id.aggiungi_voce_tvConversione)).setText(conversione);
	}


	/*
	 * Listener per l'EditText etImporto, per la conversione istantanea delle valute.
	 */
	private TextWatcher etImportoListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			//se l'importo � 0 metto croce rossa, altrimenti spunta
			try {
				importo = Double.parseDouble(etImporto.getText().toString());
			}
			catch (NumberFormatException exc) {
				importo = 0;
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
				new MioToast(SpeseAggiungi.this, getString(R.string.aggiungi_voce_appCalcolatriceNonTrovata)).visualizza(Toast.LENGTH_SHORT);
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
			((Button) findViewById(R.id.aggiungi_voce_bValuta)).setText(simboloValuta);

			//aggiorno la sezione conversione valute
			findViewById(R.id.aggiungi_voce_pbAggiornamento).setVisibility(View.GONE);
			findViewById(R.id.aggiungi_voce_tvConnessioneYahoo).setVisibility(View.INVISIBLE);
			findViewById(R.id.aggiungi_voce_tvConversione).setVisibility(View.VISIBLE);
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


	// Listener DialogFragment selezione data
	public OnClickListener etDataListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putInt(ID_DATEPICKER, DATA);
			dataFragment.setArguments(args);

			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};


	//Implementazione di DatePickerFragment.DialogFinishedListener per ricevere la data scelta.
	public void onDialogFinished(int id, int year, int month, int day) {
		if (id == DATA_FINE_RIPETIZIONE) {
			GregorianCalendar dataRestituita = new GregorianCalendar(year, month, day);
			if(dataRestituita.before(data)) {
				((Spinner) findViewById(R.id.aggiungi_voce_spFineRipetizione)).setSelection(0);

				new MioToast(SpeseAggiungi.this, getString(R.string.periodo_errore)).visualizza(Toast.LENGTH_SHORT);
			}
			else {
				dataFineRipetizione.set(GregorianCalendar.DATE, day);
				dataFineRipetizione.set(GregorianCalendar.MONTH, month);
				dataFineRipetizione.set(GregorianCalendar.YEAR, year);

				((EditText) findViewById(R.id.aggiungi_voce_etDataFineRipetizione)).setText(df.format(dataFineRipetizione.getTime()));
			}
		}
		else if (id == DATA) {
			data.set(year, month, day);
			etData.setText(df.format(data.getTime()));
		}
	}


	//Listener per lo Spinner dei tag.
	private AdapterView.OnItemSelectedListener spTagListener = new AdapterView.OnItemSelectedListener() {
		private int contatore = 0;

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if(contatore == 0) {
				contatore++;
				return;
			}

			if(pos == 0) {
				etTag.setText("");
			}
			else {
				tag = (String) parent.getItemAtPosition(pos);
				etTag.setText(tag);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};


	// Listener per lo Spinner del tipo di ripetizione (giorno per giorno o inserimento batch).
	private AdapterView.OnItemSelectedListener spTipoRipetizioneListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			inserimentoMultiplo = pos != 0;
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};


	//Listener per lo Spinner delle ripetizioni.
	private AdapterView.OnItemSelectedListener spRipetizioneListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			String ripetizioni[] = {"nessuna", "giornaliero", "settimanale", "bisettimanale", "mensile", "annuale", "giorni_lavorativi", "weekend"};
			ripetizione = ripetizioni[pos];

			if(pos == 0) {
				findViewById(R.id.aggiungi_voce_spFineRipetizione).setVisibility(View.GONE);
				findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).setVisibility(View.GONE);
				findViewById(R.id.aggiungi_voce_etDataFineRipetizione).setVisibility(View.GONE);
				cbFavorite.setEnabled(true);
			}
			else {
				Spinner spFineRipetizione = (Spinner) findViewById(R.id.aggiungi_voce_spFineRipetizione);
				spFineRipetizione.setVisibility(View.VISIBLE);
				if(spFineRipetizione.getSelectedItemPosition() == 1) {
					findViewById(R.id.aggiungi_voce_etDataFineRipetizione).setVisibility(View.VISIBLE);
				}
				else if(spFineRipetizione.getSelectedItemPosition() == 2) {
					findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).setVisibility(View.VISIBLE);
				}
				cbFavorite.setEnabled(false);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};


	//Listener per lo Spinner della fine delle ripetizioni
	private AdapterView.OnItemSelectedListener spFineRipetizioneListener = new AdapterView.OnItemSelectedListener() {
		int cont = 0;

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if(tipoOperazione == TIPO_OPERAZIONE_MODIFICA && cont == 0) {
				cont++;
				return;
			}

			if(pos == 0) {
				findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).setVisibility(View.GONE);
				findViewById(R.id.aggiungi_voce_etDataFineRipetizione).setVisibility(View.GONE);

				dataFineRipetizione.set(GregorianCalendar.DATE, 22);
				dataFineRipetizione.set(GregorianCalendar.MONTH, 4);
				dataFineRipetizione.set(GregorianCalendar.YEAR, 2076);
			}
			else if(pos == 1) {
				findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).setVisibility(View.GONE);
				findViewById(R.id.aggiungi_voce_etDataFineRipetizione).setVisibility(View.VISIBLE);

				DialogFragment dataFragment = new DatePickerFragment();
				Bundle args = new Bundle();
				args.putInt(ID_DATEPICKER, DATA_FINE_RIPETIZIONE);
				dataFragment.setArguments(args);
				dataFragment.show(getSupportFragmentManager(), "dataPicker");
			}
			else if(pos == 2) {
				findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).setVisibility(View.VISIBLE);
				findViewById(R.id.aggiungi_voce_etDataFineRipetizione).setVisibility(View.GONE);
				findViewById(R.id.aggiungi_voce_etNumeroRipetizioni).requestFocus();
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};


	//aggiunge la spesa
	private void conferma() {
		//ricavo le principali variabili da inserire che non sono ancora impostate
		if(cbFavorite.isEnabled() && cbFavorite.isChecked()) {
			favorite = 1;
		}
		else {
			favorite = 0;
		}
		try {
			importo = Double.parseDouble(etImporto.getText().toString());
		}
		catch (NumberFormatException exc) {
			importo = 0;
		}
		descrizione = etDescrizione.getText().toString();
		Cursor curConto = (Cursor) spConto.getSelectedItem();
		if(curConto != null) {
			conto = curConto.getString(curConto.getColumnIndex("conto"));
		}
		tag = etTag.getText().toString();

		if(tag.length() == 0) {
			new MioToast(SpeseAggiungi.this, getString(R.string.aggiungi_spese_errore)).visualizza(Toast.LENGTH_SHORT);


			return;
		}

		//tento inserimento del tag nella tabella spese_voci: se � gi� presente meglio
		try {
			dbcSpeseVoci.inserisciVoceSpesa(tag, 0);
		}
		catch (Exception exc) {
			//implementazione vuota
		}

		// Inserisco il tag utilizzato nel file delle preferenze per futuri inserimenti.
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor prefEditor = pref.edit();
		prefEditor.putString(TAG_SPESE_ULTIMO, tag);
		prefEditor.apply();

		//inserimento/aggiornamento della spesa
		if(tipoOperazione == TIPO_OPERAZIONE_AGGIUNGI  || tipoOperazione == TIPO_OPERAZIONE_PREFERITO) {
			if(inserimentoMultiplo) {
				calcolaDataFineRipetizione();
				// qua il campo id non è utilizzato
				SpesaEntrata spesaEntrata = new SpesaEntrata(SpesaEntrata.VOCE_SPESA, -1, data.getTimeInMillis(), tag, importo, valutaCorrente, importo * tassoCambio, descrizione, 1, conto);
				Intent intent = InserimentoMultiploIntentService.creaIntent(SpeseAggiungi.this, InserimentoMultiploIntentService.TIPO_VOCE_SPESA, ripetizione, dataFineRipetizione.getTimeInMillis(), spesaEntrata);
				startService(intent);
				new MioToast(SpeseAggiungi.this, getString(R.string.ricvoc_operazione_completata)).visualizza(Toast.LENGTH_SHORT);
			}
			else {
				new AggiungiSpesaSostenutaTask().execute((Object[]) null);
			}
		}
		else if(tipoOperazione == TIPO_OPERAZIONE_MODIFICA) {
			new AggiornaSpesaSostenutaTask().execute((Object[]) null);
		}

		//aggiornamento della tabella spese_budget campo spesa_sost
		new AggiornaTabellaBudgetTask().execute((Object[]) null);

		//imposto il risultato della Activity e ritorno a quella precedente
		conferma = true;
		setResult(FragmentActivity.RESULT_OK);
		finish();
	}


	//AsyncTask per caricare la HashMap con i suoni dell'app
	private class CaricaSuoniTask extends AsyncTask<Object, Object, Boolean> {

		protected Boolean doInBackground(Object... params) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SpeseAggiungi.this);
			boolean abilitazioneSuoni = pref.getBoolean(CostantiPreferenze.SUONI_ABILITATI, false);
			if(abilitazioneSuoni) {
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				mappaSuoni = new SparseIntArray(1);
				mappaSuoni.put(CostantiSuoni.SUONO_AGGIUNGI_SPESA_ENTRATA, soundPool.load(SpeseAggiungi.this, R.raw.spese_entrate_budget_aggiunta, 1));
			}

			return abilitazioneSuoni;
		}

		protected void onPostExecute(Boolean result) {
			//una volta caricati i suoni nella Map l'app � pronta ad utilizzarli, non prima
			suoniAbilitati = result;
		}
	}


	//AsyncTask per recuperare l'elenco dei conti per lo Spinner dei conti
	private class RecuperaContiTask extends AsyncTask<Object, Object, Cursor> {
		DBCConti dbcConti = new DBCConti(SpeseAggiungi.this);

		protected Cursor doInBackground(Object... params) {
			dbcConti.openLettura();
			Cursor curConti = dbcConti.getTuttiIContiNonOrdinato();

			return curConti;
		}

		protected void onPostExecute(Cursor result) {
			spContoAdapter.changeCursor(result);

			if(tipoOperazione == TIPO_OPERAZIONE_MODIFICA || tipoOperazione == TIPO_OPERAZIONE_PREFERITO) {
				int i=0;
				while(result.moveToNext()) {
					String valoreCursor = result.getString(result.getColumnIndex("conto"));
					if(valoreCursor.equals(conto)) {
						spConto.setSelection(i);
						break;
					}
					i++;
				}
			}
			else {
				// Per operazioni di aggiunta, imposto lo Spinner sul conto di default.
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SpeseAggiungi.this);
				String contoDefault = pref.getString(CONTO_DEFAULT, "default");
				if(MainPersonalBudget.conto != null && !MainPersonalBudget.conto.equals("%")) {
					contoDefault = MainPersonalBudget.conto;
				}

				int i=0;
				while(result.moveToNext()) {
					String valoreCursor = result.getString(result.getColumnIndex("conto"));
					if(valoreCursor.equals(contoDefault)) {
						spConto.setSelection(i);
						break;
					}
					i++;
				}
			}

			dbcConti.close();
		}
	}


	//AsyncTask per recuperare l'elenco dei tag delle spese per lo Spinner dei tag
	private class RefreshTagsCursorTask extends AsyncTask<Object, Object, Object> {
		String arrVoci[];

		protected Object doInBackground(Object... params) {
			dbcSpeseVoci.openLettura();
			curVoci = dbcSpeseVoci.getTutteLeVoci();

			if(curVoci.getCount() == 0) {
				return null;
			}

			arrVoci = new String[curVoci.getCount()];
			for(int i=0; curVoci.moveToNext(); i++) {
				arrVoci[i] = curVoci.getString(curVoci.getColumnIndex("voce"));
			}

			curVoci.close();
			dbcSpeseVoci.close();

			return null;
		}

		protected void onPostExecute(Object result) {
			spTagAdapter.addAll(arrVoci);
			spTagAdapter.notifyDataSetChanged();
		}
	}


	//AsyncTask per recuperare i dettagli di ripetizione di una spesa (gi� esistente, da modificare)
	private class RecuperaRipetizioneTask extends AsyncTask<Long, Object, Cursor> {

		protected Cursor doInBackground(Long... params) {
			dbcSpeseRipetute.openLettura();
			Cursor curRipetizione = dbcSpeseRipetute.getItemRepeated(params[0]);

			return curRipetizione;
		}

		protected void onPostExecute(Cursor curRipetizione) {
			curRipetizione.moveToFirst();
			ripetizione = curRipetizione.getString(curRipetizione.getColumnIndex("ripetizione"));
			long miaDataFine = curRipetizione.getLong(curRipetizione.getColumnIndex("data_fine"));
			dataFineRipetizione.setTimeInMillis(miaDataFine);

			String ripetizioni[] = {"nessuna", "giornaliero", "settimanale", "bisettimanale", "mensile", "annuale", "giorni_lavorativi", "weekend"};
			for(int i=0; i<8; i++) {
				if(ripetizione.equals(ripetizioni[i])) {
					((Spinner) findViewById(R.id.aggiungi_voce_spRipetizione)).setSelection(i);
				}
			}

			findViewById(R.id.aggiungi_voce_spFineRipetizione).setVisibility(View.VISIBLE);
			((Spinner) findViewById(R.id.aggiungi_voce_spFineRipetizione)).setSelection(1);
			findViewById(R.id.aggiungi_voce_etDataFineRipetizione).setVisibility(View.VISIBLE);
			((EditText) findViewById(R.id.aggiungi_voce_etDataFineRipetizione)).setText(df.format(dataFineRipetizione.getTime()));

			curRipetizione.close();
			dbcSpeseRipetute.close();

			etImporto.requestFocus();
		}
	}


	//AsyncTask per inserire la spesa sostenuta nella tabella spese_sost.
	private class AggiungiSpesaSostenutaTask extends AsyncTask<Object, Object, Boolean> {

		protected Boolean doInBackground(Object... params) {
			if(ripetizione.equals("nessuna")) {
				ripetizione_id = 1;
			}
			else {
				calcolaDataFineRipetizione();
				ripetizione_id = aggiungiSpesaRipetuta();
			}

			if(ripetizione_id != -1) {
				dbcSpeseSostenute.openModifica();
				try {
					dbcSpeseSostenute.insertElement(data.getTimeInMillis(), tag, importo, valutaCorrente, importo * tassoCambio, descrizione, ripetizione_id, conto, favorite);
				}
				catch (Exception exc) {
					return false;
				}
			}
			else {
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean result) {
			if(!result) {
				new MioToast(SpeseAggiungi.this, getString(R.string.toast_errore_inserimento)).visualizza(Toast.LENGTH_SHORT);
			}
			else {
				new MioToast(SpeseAggiungi.this, getString(R.string.toast_spesa_aggiunta)).visualizza(Toast.LENGTH_SHORT);
			}

			dbcSpeseSostenute.close();
		}
	}


	//AsyncTask per aggiornare la spesa sostenuta nella tabella spese_sost (modifica).
	private class AggiornaSpesaSostenutaTask extends AsyncTask<Object, Object, Boolean> {

		protected Boolean doInBackground(Object... params) {
			//tabella spese_ripet
			if(ripetizione.equals("nessuna")) {
				if(ripetizione_id != 1) {
					ripetizione_id = eliminaRipetizione();
				}
			}
			else {
				calcolaDataFineRipetizione();
				if(ripetizione_id != 1) {
					ripetizione_id = aggiornaSpesaRipetuta();
				}
				else {
					ripetizione_id = aggiungiSpesaRipetuta();
				}
			}

			//tabella spese_sost
			if(ripetizione_id != -1) {
				dbcSpeseSostenute.openModifica();
				try {
					dbcSpeseSostenute.aggiornaSpesaSostenuta(id, data.getTimeInMillis(), tag, importo, valutaCorrente, importo * tassoCambio, descrizione, ripetizione_id, conto, favorite);
				}
				catch (Exception exc) {
					return false;
				}
			}
			else {
				return false;
			}

			return true;
		}

		protected void onPostExecute(Boolean params) {
			if(!params) {
				new MioToast(SpeseAggiungi.this, getString(R.string.toast_errore_inserimento)).visualizza(Toast.LENGTH_SHORT);
			}
			else {
				new MioToast(SpeseAggiungi.this, getString(R.string.toast_spesa_modificata)).visualizza(Toast.LENGTH_SHORT);
			}

			dbcSpeseSostenute.close();
		}
	}


	/*
	 *  Inserisce la spesa ripetuta nella tabella spese_ripet. Restituisce -1 se l'inserimento non ha avuto
	 * successo, altrimenti restituisce l'id del record inserito nella tabella.
	 */
	private long aggiungiSpesaRipetuta() {
			dbcSpeseRipetute.openModifica();
			long mioId;
			try {
				mioId = dbcSpeseRipetute.inserisciSpesaRipetuta(tag, ripetizione, importo, valutaCorrente, importo * tassoCambio, descrizione, data.getTimeInMillis(), 0, dataFineRipetizione.getTimeInMillis(), data.getTimeInMillis(), conto);
			}
			catch (Exception exc) {
				mioId = -1;
			}

			dbcSpeseRipetute.close();

			return mioId;
	}


	/*
	 * Aggiorna un record della tabella spese_ripet per l'operazione di modifica di spese gi�
	 * esistenti con ripetizione.
	 * Restituisce ripetizione_id se l'operazione ha avuto successo, -1 in caso di insuccesso
	 */
	private long aggiornaSpesaRipetuta() {
		dbcSpeseRipetute.openModifica();
		long risul = ripetizione_id;
		try {
			dbcSpeseRipetute.aggiornaSpesaRipetuta(ripetizione_id, tag, ripetizione, importo, valutaCorrente, importo * tassoCambio, descrizione, data.getTimeInMillis(), 0, dataFineRipetizione.getTimeInMillis(), oggi.getTimeInMillis(), conto);
		}
		catch (Exception exc) {
			risul = -1;
		}

		dbcSpeseRipetute.close();

		return risul;
	}


	/*
	 * Elimino il record dalla tabella spese_ripet: restituisce 1 se l'operazione ha avuto successo,
	 * -1 in caso di insuccesso.
	 */

	private long eliminaRipetizione() {
		dbcSpeseRipetute.openModifica();
		long risul = 1;
		try {
			dbcSpeseRipetute.eliminaSpesaRipetuta(ripetizione_id);
		}
		catch (Exception exc) {
			risul = -1;
		}

		dbcSpeseRipetute.close();

		return risul;
	}


	/*
	 * Calcola la data finale delle spese ripetute a seconda del tipo di ripetizione impostata.
	 */
	private void calcolaDataFineRipetizione() {
		Spinner spFineRipetizione = (Spinner) findViewById(R.id.aggiungi_voce_spFineRipetizione);
		if(spFineRipetizione.getSelectedItemPosition() == 2) {
			EditText etNumeroRipetizioni = (EditText) findViewById(R.id.aggiungi_voce_etNumeroRipetizioni);
			int numRipet = Integer.parseInt(etNumeroRipetizioni.getText().toString());

			//calcolo la data finale
			dataFineRipetizione.set(GregorianCalendar.DATE, data.get(GregorianCalendar.DATE));
			dataFineRipetizione.set(GregorianCalendar.MONTH, data.get(GregorianCalendar.MONTH));
			dataFineRipetizione.set(GregorianCalendar.YEAR, data.get(GregorianCalendar.YEAR));

			if(ripetizione.equals("giornaliero")) {
				dataFineRipetizione.add(GregorianCalendar.DATE, numRipet);
			}
			else if(ripetizione.equals("mensile")) {
				dataFineRipetizione.add(GregorianCalendar.MONTH, numRipet);
			}
			else if(ripetizione.equals("annuale")) {
				dataFineRipetizione.add(GregorianCalendar.YEAR, numRipet);
			}
			else if(ripetizione.equals("settimanale")) {
				dataFineRipetizione.add(GregorianCalendar.WEEK_OF_YEAR, numRipet);
			}
			else if(ripetizione.equals("bisettimanale")) {
				dataFineRipetizione.add(GregorianCalendar.WEEK_OF_YEAR, numRipet * 2);
			}
			else if(ripetizione.equals("giorni_lavorativi")) {
				int i = numRipet;
				while(i > 0) {
					dataFineRipetizione.add(GregorianCalendar.DATE, 1);
					if((dataFineRipetizione.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY) || (dataFineRipetizione.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)) {
						continue;
					}
					i--;
				}
			}
			else if(ripetizione.equals("weekend")) {
				int i = numRipet;
				while(i > 0) {
					dataFineRipetizione.add(GregorianCalendar.DATE, 1);
					if(!((dataFineRipetizione.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY) || (dataFineRipetizione.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY))) {
						continue;
					}
					i--;
				}
			}
		}
	}


	//AsyncTask per aggiornare la tabella spese_budget -a seguito dell'inserimento/modifica della spesa
	private class AggiornaTabellaBudgetTask extends AsyncTask<Object, Object, Boolean> {

		protected Boolean doInBackground(Object... params) {
			int budgetAggiornati = 0;

			FunzioniAggiornamento aggBudget = new FunzioniAggiornamento(SpeseAggiungi.this);
			if(tipoOperazione == TIPO_OPERAZIONE_AGGIUNGI || tipoOperazione == TIPO_OPERAZIONE_PREFERITO) {
				budgetAggiornati = aggBudget.aggiornaTabBudgetSpeseSost(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + tag + "%", Long.valueOf(data.getTimeInMillis()).toString(), Long.valueOf(data.getTimeInMillis()).toString());
			}
			else if (tipoOperazione == TIPO_OPERAZIONE_MODIFICA) {
				budgetAggiornati = aggBudget.aggiornaTabBudgetSpeseSost(ESTRAI_BUDGET_PER_MODIFICA_SPESA, "%" + tag + "%", Long.valueOf(data.getTimeInMillis()).toString(), Long.valueOf(data.getTimeInMillis()).toString(), "%" + tagVecchio + "%", Long.valueOf(dataVecchia.getTimeInMillis()).toString(), Long.valueOf(dataVecchia.getTimeInMillis()).toString());
			}

			return budgetAggiornati != -1;
		}

		protected void onPostExecute(Boolean result) {
			if(!result) {
				new MioToast(SpeseAggiungi.this, getString(R.string.toast_aggiornamentoDatabase_errore)).visualizza(Toast.LENGTH_SHORT);
			}

			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
		}
	}


	@Override
	protected void onStop() {
		super.onStop();

		spTagAdapter.clear();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		if(curVoci != null) {
			curVoci.close();
		}
		if(curVociPerAutocomplete != null) {
			curVociPerAutocomplete.close();
		}
		dbcSpeseVociPerAutocomplete.close();

		if(suoniAbilitati && conferma) {
			soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_AGGIUNGI_SPESA_ENTRATA), 1, 1, 1, 0, 1f);
		}
	}


	//grafica schede inserimento dati: espansione e compressione
	private OnClickListener titoliSchedeListener = new OnClickListener() {
		boolean importoEspanso = true;
		boolean contoEspanso = false;
		boolean tagsEspanso = false;
		boolean dataEspanso = false;
		boolean descrizioneEspanso = false;
		boolean ripetizioneEspanso = false;
		boolean preferitiEspanso = false;

		Animazioni animazioni = Animazioni.getInstance();

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.aggiungi_voce_tlImporto_tableRowTitolo:
				if(importoEspanso) {
					findViewById(R.id.aggiungi_voce_tlImporto_tableRow0).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_tlImporto_tableRow1).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaImporto), importoEspanso);
					findViewById(R.id.aggiungi_voce_tlImporto_tableRowBordo).setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_importo).setElevation(6);
					}
					importoEspanso = false;
				}
				else {
					findViewById(R.id.aggiungi_voce_tlImporto_tableRow0).setVisibility(View.VISIBLE);
					findViewById(R.id.aggiungi_voce_tlImporto_tableRow1).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaImporto), importoEspanso);
					findViewById(R.id.aggiungi_voce_tlImporto_tableRowBordo).setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_importo).setElevation(16);
					}
					importoEspanso = true;
				}

				break;

			case R.id.aggiungi_voce_tlConto_tableRowTitolo:
				if(contoEspanso) {
					findViewById(R.id.aggiungi_voce_tlConto_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_spConto).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaConto), contoEspanso);
					findViewById(R.id.aggiungi_voce_llContoControlli).setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_conto).setElevation(6);
					}
					contoEspanso = false;
				}
				else {
					findViewById(R.id.aggiungi_voce_tlConto_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.aggiungi_voce_spConto).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaConto), contoEspanso);
					findViewById(R.id.aggiungi_voce_llContoControlli).setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_conto).setElevation(16);
					}
					contoEspanso = true;
				}

				break;

			case R.id.aggiungi_voce_tlTags_tableRowTitolo:
				if(tagsEspanso) {
					findViewById(R.id.aggiungi_voce_tlTags_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_spTag).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_etTag).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaTags), tagsEspanso);
					findViewById(R.id.aggiungi_voce_tlTags_tableRowBordo).setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_tags).setElevation(6);
					}
					tagsEspanso = false;
				}
				else {
					findViewById(R.id.aggiungi_voce_tlTags_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.aggiungi_voce_spTag).setVisibility(View.VISIBLE);
					findViewById(R.id.aggiungi_voce_etTag).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaTags), tagsEspanso);
					findViewById(R.id.aggiungi_voce_tlTags_tableRowBordo).setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_tags).setElevation(16);
					}
					tagsEspanso = true;
				}

				break;

			case R.id.aggiungi_voce_tlData_tableRowTitolo:
				if(dataEspanso) {
					findViewById(R.id.aggiungi_voce_tlData_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_etData).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaData), dataEspanso);
					findViewById(R.id.aggiungi_voce_tlData_tableRowBordo).setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_data).setElevation(6);
					}
					dataEspanso = false;
				}
				else {
					findViewById(R.id.aggiungi_voce_tlData_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.aggiungi_voce_etData).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaData), dataEspanso);
					findViewById(R.id.aggiungi_voce_tlData_tableRowBordo).setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_data).setElevation(16);
					}
					dataEspanso = true;
				}

				break;

			case R.id.aggiungi_voce_tlDescrizione_tableRowTitolo:
				if(descrizioneEspanso) {
					findViewById(R.id.aggiungi_voce_tlDescrizione_tableRow0).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaDescrizione), descrizioneEspanso);
					findViewById(R.id.aggiungi_voce_tlDescrizione_tableRowBordo).setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_descrizione).setElevation(6);
					}
					descrizioneEspanso = false;
				}
				else {
					findViewById(R.id.aggiungi_voce_tlDescrizione_tableRow0).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaDescrizione), descrizioneEspanso);
					findViewById(R.id.aggiungi_voce_tlDescrizione_tableRowBordo).setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_descrizione).setElevation(16);
					}
					descrizioneEspanso = true;
				}

				break;

			case R.id.aggiungi_voce_tlRipetizione_tableRowTitolo:
				if(ripetizioneEspanso) {
					findViewById(R.id.aggiungi_voce_tlRipetizione_tableRowBordo).setVisibility(View.GONE);
					findViewById(R.id.aggiungi_voce_llRipetizioneControlli).setVisibility(View.GONE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaRipetizione), ripetizioneEspanso);
					findViewById(R.id.aggiungi_voce_tlRipetizione_tableRowBordo).setVisibility(View.GONE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_ripetizione).setElevation(6);
					}
					ripetizioneEspanso = false;
				}
				else {
					findViewById(R.id.aggiungi_voce_tlRipetizione_tableRowBordo).setVisibility(View.VISIBLE);
					findViewById(R.id.aggiungi_voce_llRipetizioneControlli).setVisibility(View.VISIBLE);
					animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaRipetizione), ripetizioneEspanso);
					findViewById(R.id.aggiungi_voce_tlRipetizione_tableRowBordo).setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						findViewById(R.id.card_view_ripetizione).setElevation(16);
					}
					ripetizioneEspanso = true;
				}

				break;

				case R.id.aggiungi_voce_tlPreferiti_tableRowTitolo:
					if(preferitiEspanso) {
						findViewById(R.id.aggiungi_voce_tlPreferiti_tableRowBordo).setVisibility(View.GONE);
						animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaPreferiti), preferitiEspanso);
						findViewById(R.id.aggiungi_voce_llPreferitiControlli).setVisibility(View.GONE);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							findViewById(R.id.card_view_preferiti).setElevation(6);
						}
						preferitiEspanso = false;
					}
					else {
						findViewById(R.id.aggiungi_voce_tlPreferiti_tableRowBordo).setVisibility(View.VISIBLE);
						animazioni.ruotaFreccia(findViewById(R.id.aggiungi_voce_ivFrecciaPreferiti), preferitiEspanso);
						findViewById(R.id.aggiungi_voce_llPreferitiControlli).setVisibility(View.VISIBLE);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							findViewById(R.id.card_view_preferiti).setElevation(16);
						}
						preferitiEspanso = true;
					}

					break;
			}
		}
	};


	//variabili di istanza
	private EditText etImporto;
	private EditText etData;
	private AutoCompleteTextView etTag;
	private EditText etDescrizione;
	private Spinner spConto;
	private Spinner spTag;
	private CheckBox cbFavorite;

	private int tipoOperazione; //aggiunta o modifica spesa?
	private long id;
	private DateFormat df;
	private GregorianCalendar data;
	private GregorianCalendar oggi;
	private GregorianCalendar ieri;
	private GregorianCalendar dataFineRipetizione;
	private boolean inserimentoMultiplo;
	private String ripetizione;
	private String conto;
	private String tag;
	private String descrizione;
	private double importoValprin;
	private double importo;
	private long ripetizione_id;
	private String valutaPrincipale;
	private String valutaCorrente;
	private float tassoCambio;
	private int favorite;

	//gestione suoni
	private SoundPool soundPool;
	private SparseIntArray mappaSuoni;
	private boolean suoniAbilitati;
	private boolean conferma;

	//valori iniziali: servono per modificare correttamente la tabella dei budget
	private GregorianCalendar dataVecchia;
	private String tagVecchio;

	//gestione del database
	private DBCSpeseVoci dbcSpeseVoci;
	private DBCSpeseVoci dbcSpeseVociPerAutocomplete;
	private DBCSpeseSostenute dbcSpeseSostenute;
	private DBCSpeseRipetute dbcSpeseRipetute;
	private SimpleCursorAdapter spContoAdapter;
	private ArrayAdapter<String> spTagAdapter;
	private SimpleCursorAdapter etTagAdapter;
	private Cursor curVoci;
	private Cursor curVociPerAutocomplete;

	private BroadcastReceiver mLocalReceiverRecuperaCambio;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
}
