package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.ID_DATEPICKER;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.CONTO_DEFAULT;
import static com.flingsoftware.personalbudget.app.ContiElenco.*;

import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.Conto;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCEntrateRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;

import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;


public class ContiModifica extends ActionBarActivity implements DatePickerFragment.DialogFinishedListener {
	
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
	private String nomeIniziale;
	private boolean eraDefault;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conti_inserimento);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		// Recupero dati del conto passati dall'Activity chiamante
		conto = new Conto();
		Bundle extras = getIntent().getExtras();
		conto.setId(extras.getLong(EXTRA_ID));
		conto.setConto(extras.getString(EXTRA_CONTO));
		nomeIniziale = conto.getConto();
		conto.setSaldo(extras.getDouble(EXTRA_SALDO));
		conto.setDataSaldo(extras.getLong(EXTRA_DATA_SALDO));
		eraDefault = extras.getBoolean(EXTRA_CONTO_DEFAULT);
		
		// Imposto il layout con i dati esistenti
		etNome = (EditText) findViewById(R.id.ci_etNome);
		etSaldo = (EditText) findViewById(R.id.ci_etSaldo);
		etData = (EditText) findViewById(R.id.ci_etData);
		etData.setOnClickListener(etDataOnClickListener);
		findViewById(R.id.ci_tlConto_tableRowTitolo).setOnClickListener(schedaListener);
		df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
		dataSaldo = new GregorianCalendar();
		dataSaldo.setTimeInMillis(conto.getDataSaldo());		
		etNome.setText(conto.getConto());
		etSaldo.setText("" + conto.getSaldo());
		etData.setText(df.format(new Date(conto.getDataSaldo())));
		if(conto.getConto().equals("default")) {
			etNome.setEnabled(false);
			etNome.setFocusable(false);
			etNome.setLongClickable(false);
			etNome.setCursorVisible(false);
		}
		cbContoDefault = (CheckBox) findViewById(R.id.ci_cbContoDefault);
		cbContoDefault.setChecked(eraDefault);
		
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
		
		@Override
		public void onClick(View v) {
			if(schedaEspansa) {
				findViewById(R.id.ci_llContoControlli).setVisibility(View.GONE);
				((ImageView) findViewById(R.id.ci_ivFrecciaConto)).setImageDrawable(getResources().getDrawable(R.drawable.ic_navigation_expand));	
				findViewById(R.id.ci_tlConto_tableRowBordo).setVisibility(View.GONE);
				schedaEspansa = false;
			}
			else {
				findViewById(R.id.ci_llContoControlli).setVisibility(View.VISIBLE);
				((ImageView) findViewById(R.id.ci_ivFrecciaConto)).setImageDrawable(getResources().getDrawable(R.drawable.ic_navigation_collapse));
				findViewById(R.id.ci_tlConto_tableRowBordo).setVisibility(View.VISIBLE);
				schedaEspansa = true;
			}
		}
	};
	
	
	// Conferma aggiunta nuovo conto
	private void conferma() {
		if(etNome.getText() == null || etNome.getText().toString().length() == 0) {
			new MioToast(ContiModifica.this, getString(R.string.conti_inserimento_nomeContoMancante)).visualizza(Toast.LENGTH_SHORT);
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
			impostaContoDefault(cbContoDefault.isChecked());

			new ModificaContoTask().execute((Object[]) null);
		}		
	}


	// Se questo è il conto di default lo salvo nelle preferenze.
	private void impostaContoDefault(boolean isContoDefault) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor prefEditor = pref.edit();

		if(isContoDefault && !eraDefault) {
			prefEditor.putString(CONTO_DEFAULT, conto.getConto());
			prefEditor.apply();
		}
		else if (!isContoDefault && eraDefault) {
			if(!conto.getConto().equals("default")) {
				prefEditor.putString(CONTO_DEFAULT, "default");
				prefEditor.apply();
			}
		}
	}


	// Aggiunta nuova conto in un thread separato
	private class ModificaContoTask extends AsyncTask<Object, Object, Object> {
		DBCConti dbcConti = new DBCConti(ContiModifica.this);
		
		protected Object doInBackground(Object... params) {
			dbcConti.openModifica();
			dbcConti.aggiornaConto(conto);
			dbcConti.close();
			
			if(!conto.getConto().equals(nomeIniziale)) {
				aggiornaNomeConto();
			}
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			new MioToast(ContiModifica.this, getString(R.string.conti_modifica_contoModificato)).visualizza(Toast.LENGTH_SHORT);
			setResult(Activity.RESULT_OK);
			finish();
		}
	}
	
	
	// Cambio il nome del conto in tutte le tabelle interessate
	private void aggiornaNomeConto() {
		// Tabella spese sostenute: le spese con il conto eliminato passano al conto default
		DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(ContiModifica.this);
		dbcSpeseSostenute.openModifica();
		Cursor curSpeseSost = dbcSpeseSostenute.getTutteLeSpeseContoX(nomeIniziale);
		while(curSpeseSost.moveToNext()) {
			long id = curSpeseSost.getLong(curSpeseSost.getColumnIndex("_id"));
			long data = curSpeseSost.getLong(curSpeseSost.getColumnIndex("data"));
			String voce = curSpeseSost.getString(curSpeseSost.getColumnIndex("voce"));
			double importo = curSpeseSost.getDouble(curSpeseSost.getColumnIndex("importo"));
			String valuta = curSpeseSost.getString(curSpeseSost.getColumnIndex("valuta"));
			double importoValprin = curSpeseSost.getDouble(curSpeseSost.getColumnIndex("importo_valprin"));
			String descrizione = curSpeseSost.getString(curSpeseSost.getColumnIndex("descrizione"));
			long ripetizioneId = curSpeseSost.getLong(curSpeseSost.getColumnIndex("ripetizione_id"));
			int favorite = curSpeseSost.getInt(curSpeseSost.getColumnIndex("favorite"));
			
			dbcSpeseSostenute.aggiornaSpesaSostenuta(id, data, voce, importo, valuta, importoValprin, descrizione, ripetizioneId, conto.getConto(), favorite);
		}			
		curSpeseSost.close();
		dbcSpeseSostenute.close();
		
		// Tabella spese ripetute: le spese con il conto eliminato passano al conto default
		DBCSpeseRipetute dbcSpeseRipetute = new DBCSpeseRipetute(ContiModifica.this);
		dbcSpeseRipetute.openModifica();
		Cursor curSpeseRip = dbcSpeseRipetute.getTutteLeSpeseContoX(nomeIniziale);
		while(curSpeseRip.moveToNext()) {
			long id = curSpeseRip.getLong(curSpeseRip.getColumnIndex("_id"));
			String voce = curSpeseRip.getString(curSpeseRip.getColumnIndex("voce"));
			String ripetizione = curSpeseRip.getString(curSpeseRip.getColumnIndex("ripetizione"));			
			double importo = curSpeseRip.getDouble(curSpeseRip.getColumnIndex("importo"));
			String valuta = curSpeseRip.getString(curSpeseRip.getColumnIndex("valuta"));
			double importoValprin = curSpeseRip.getDouble(curSpeseRip.getColumnIndex("importo_valprin"));
			String descrizione = curSpeseRip.getString(curSpeseRip.getColumnIndex("descrizione"));
			long dataInizio = curSpeseRip.getLong(curSpeseRip.getColumnIndex("data_inizio"));
			int flagFine = curSpeseRip.getInt(curSpeseRip.getColumnIndex("flag_fine"));
			long dataFine = curSpeseRip.getLong(curSpeseRip.getColumnIndex("data_fine"));
			long aggiornatoA = curSpeseRip.getLong(curSpeseRip.getColumnIndex("aggiornato_a"));
			
			dbcSpeseRipetute.aggiornaSpesaRipetuta(id, voce, ripetizione, importo, valuta, importoValprin, descrizione, dataInizio, flagFine, dataFine, aggiornatoA, conto.getConto());
		}			
		curSpeseRip.close();
		dbcSpeseRipetute.close();
		
		// Tabella entrate incassate: le entrate con il conto eliminato passano al conto default
		DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(ContiModifica.this);
		dbcEntrateIncassate.openModifica();
		Cursor curEntrateInc = dbcEntrateIncassate.getTutteLeEntrateContoX(nomeIniziale);
		while(curEntrateInc.moveToNext()) {
			long id = curEntrateInc.getLong(curEntrateInc.getColumnIndex("_id"));
			long data = curEntrateInc.getLong(curEntrateInc.getColumnIndex("data"));
			String voce = curEntrateInc.getString(curEntrateInc.getColumnIndex("voce"));
			double importo = curSpeseSost.getDouble(curSpeseSost.getColumnIndex("importo"));
			String valuta = curEntrateInc.getString(curEntrateInc.getColumnIndex("valuta"));
			double importoValprin = curEntrateInc.getDouble(curEntrateInc.getColumnIndex("importo_valprin"));
			String descrizione = curEntrateInc.getString(curEntrateInc.getColumnIndex("descrizione"));
			long ripetizioneId = curEntrateInc.getLong(curEntrateInc.getColumnIndex("ripetizione_id"));
			int favorite = curEntrateInc.getInt(curEntrateInc.getColumnIndex("favorite"));
			
			dbcEntrateIncassate.aggiornaEntrataIncassata(id, data, voce, importo, valuta, importoValprin, descrizione, ripetizioneId, conto.getConto(), favorite);
		}			
		curEntrateInc.close();
		dbcEntrateIncassate.close();
		
		// Tabella entrate ripetute: le entrate con il conto eliminato passano al conto default
		DBCEntrateRipetute dbcEntrateRipetute = new DBCEntrateRipetute(ContiModifica.this);
		dbcEntrateRipetute.openModifica();
		Cursor curEntrateRip = dbcEntrateRipetute.getTutteLeEntrateContoX(nomeIniziale);
		while(curEntrateRip.moveToNext()) {
			long id = curEntrateRip.getLong(curEntrateRip.getColumnIndex("_id"));
			String voce = curEntrateRip.getString(curEntrateRip.getColumnIndex("voce"));
			String ripetizione = curEntrateRip.getString(curEntrateRip.getColumnIndex("ripetizione"));			
			double importo = curEntrateRip.getDouble(curEntrateRip.getColumnIndex("importo"));
			String valuta = curEntrateRip.getString(curEntrateRip.getColumnIndex("valuta"));
			double importoValprin = curEntrateRip.getDouble(curEntrateRip.getColumnIndex("importo_valprin"));
			String descrizione = curEntrateRip.getString(curEntrateRip.getColumnIndex("descrizione"));
			long dataInizio = curEntrateRip.getLong(curEntrateRip.getColumnIndex("data_inizio"));
			int flagFine = curEntrateRip.getInt(curEntrateRip.getColumnIndex("flag_fine"));
			long dataFine = curEntrateRip.getLong(curEntrateRip.getColumnIndex("data_fine"));
			long aggiornatoA = curEntrateRip.getLong(curEntrateRip.getColumnIndex("aggiornato_a"));
			
			dbcEntrateRipetute.aggiornaEntrataRipetuta(id, voce, ripetizione, importo, valuta, importoValprin, descrizione, dataInizio, flagFine, dataFine, aggiornatoA, conto.getConto());
		}			
		curEntrateRip.close();
		dbcEntrateRipetute.close();
	}
}
