/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MenuPeriodoTimeline;
import com.flingsoftware.personalbudget.customviews.MioToast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA_30GG_PROSSIMI;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA_30GG_ULTIMI;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_AUTOMATICA_MESE_CORRENTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_FINE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_INIZIO;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.DATA_OFFSET;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.ID_DATEPICKER;


public class MenuPeriodo extends ActionBarActivity implements DatePickerFragment.DialogFinishedListener {
	
	// Costanti.
	private static final int DATA_INIZIALE = 0;
	private static final int DATA_FINALE = 1;

	// Costanti scelte rapide.
	private static final int NESSUNA = 0;
	private static final int SEMPRE = 1;
	private static final int ULTIMI_30 = 2;
	private static final int PROSSIMI_30 = 3;
	private static final int MESE_CORRENTE = 4;
	private static final int MESE_ATTUALE = 5;
	private static final int MESE_PRECEDENTE = 6;
	private static final int MESE_SUCCESSIVO = 7;

	public static final String TIPO_DATA_AUTOMATICA = "tipo_data_automatica";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_periodo);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
		
		//ricavo la data iniziale e finale passate dalla Activity principale
		dataInizio = new GregorianCalendar();
		dataFine = new GregorianCalendar();
		Bundle extras = getIntent().getExtras();
		dataInizio.setTimeInMillis(extras.getLong(DATA_INIZIO));
		dataFine.setTimeInMillis(extras.getLong(DATA_FINE));
		
		etInizio = (EditText) findViewById(R.id.etInizio);
		etInizio.setOnClickListener(etInizioOnClickListener);
		etFine = (EditText) findViewById(R.id.etFine);
		etFine.setOnClickListener(etFineOnClickListener);
		etOffset = (EditText) findViewById(R.id.menu_periodo_etOffset);
		
		spScelteRapide = (Spinner) findViewById(R.id.menu_periodo_spScelteRapide);
		impostaAdapterSpinner();
		spScelteRapide.setOnItemSelectedListener(spScelteRapideOnItemSelectedListener);
		
		//impostazione timeline con le date
		timeLine = (MenuPeriodoTimeline) findViewById(R.id.menu_periodo_timeLine);
		timeLine.setDataInizio(dataInizio.getTimeInMillis());
		timeLine.setDataFine(dataFine.getTimeInMillis());
		timeLine.setDataOggi(FunzioniComuni.getDataAttuale());
		
		aggiornaCampiDate();
		impostaCampoOffset();
		
		etOffset.addTextChangedListener(etOffsetTextChangedListener);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_menuperiodo, menu);
		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_menuPeriodo_OK:
			conferma();

			return true;	
		case android.R.id.home:
	        finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void aggiornaCampiDate() {
		etInizio.setText(Html.fromHtml(getResources().getString(R.string.menu_inizia_da) + "<b>" + " " + df.format(dataInizio.getTime())));
		etFine.setText(Html.fromHtml(getResources().getString(R.string.menu_termina_a) + "<b>" + " " + df.format(dataFine.getTime())));
	}
	
	
	/*
	 * Imposta i campi dello Spinner per le scelte rapide del periodo.
	 */
	private void impostaAdapterSpinner() {
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
		adapter.add(getResources().getString(R.string.menu_periodo_nessuna));
		adapter.add(getResources().getString(R.string.menu_periodo_sempre));
		adapter.add(getResources().getString(R.string.menu_periodo_ultimi_30_gg));
		adapter.add(getResources().getString(R.string.menu_periodo_prossimi_30_gg));
		adapter.add(getResources().getString(R.string.menu_periodo_mese_corrente));
		
		GregorianCalendar data = new GregorianCalendar();
		int meseCorrente = data.get(Calendar.MONTH);
		String[] mesi = getResources().getStringArray(R.array.mesi);
		adapter.add(mesi[meseCorrente]);
		int mesePrec = (meseCorrente - 1) >= 0 ? (meseCorrente - 1) : 11; 
		adapter.add(mesi[mesePrec]);
		int meseSucc = (meseCorrente + 1) <= 11 ? (meseCorrente + 1) : 0;
		adapter.add(mesi[meseSucc]);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spScelteRapide.setAdapter(adapter);
	}
	
	
	//ricavo dalle preferenze l'offset del mese
	private void impostaCampoOffset() {
		SharedPreferences prefTempo = PreferenceManager.getDefaultSharedPreferences(this);
		offset = prefTempo.getInt(DATA_OFFSET, 1);
		etOffset.setText("" + offset);
	}
	
	
	public OnClickListener etInizioOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putInt(ID_DATEPICKER, DATA_INIZIALE);
			dataFragment.setArguments(args);
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	public OnClickListener etFineOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			args.putInt(ID_DATEPICKER, DATA_FINALE);
			dataFragment.setArguments(args);
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	public void onDialogFinished(int id, int year, int month, int day) {
		if(id == DATA_INIZIALE) {
			dataInizio.set(GregorianCalendar.DATE, day);
			dataInizio.set(GregorianCalendar.MONTH, month);
			dataInizio.set(GregorianCalendar.YEAR, year);
			timeLine.setDataInizio(dataInizio.getTimeInMillis());
		}
		else if(id == DATA_FINALE) {
			dataFine.set(GregorianCalendar.DATE, day);
			dataFine.set(GregorianCalendar.MONTH, month);
			dataFine.set(GregorianCalendar.YEAR, year);
			timeLine.setDataFine(dataFine.getTimeInMillis());
		}
		
		timeLine.ridisegna();
		aggiornaCampiDate();
	}
	
	
	//listener per lo Spinner delle scelte rapide
	OnItemSelectedListener spScelteRapideOnItemSelectedListener = new OnItemSelectedListener() {    
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	        switch(pos) {
			case NESSUNA: // nessuna
		    	dataAutomatica = false;
				tipoDataAutomatica = -1;
		    	break;
		    	
	        case SEMPRE: // sempre
            	dataInizio.set(GregorianCalendar.DATE, 22);
            	dataInizio.set(GregorianCalendar.MONTH, 4);
            	dataInizio.set(GregorianCalendar.YEAR, 1976);
            	dataFine.set(GregorianCalendar.DATE, 22);
            	dataFine.set(GregorianCalendar.MONTH, 4);
            	dataFine.set(GregorianCalendar.YEAR, 2076);
            	dataAutomatica = false;
				tipoDataAutomatica = -1;
	            break;
	            
	        case ULTIMI_30: // ultimi 30 gg
	    		impostaDataAttuale();
				dataInizio.add(GregorianCalendar.DATE, -30);
	    		dataAutomatica = true;
				tipoDataAutomatica = DATA_AUTOMATICA_30GG_ULTIMI;
		    	break;

			case PROSSIMI_30: // prossimi 30 gg
				impostaDataAttuale();
				dataFine.add(GregorianCalendar.DATE, 30);
				dataAutomatica = true;
				tipoDataAutomatica = DATA_AUTOMATICA_30GG_PROSSIMI;
				break;

			case MESE_CORRENTE: // mese corrente (aggiornamento automatico)
				impostaDataAttuale();
				dataInizio.set(GregorianCalendar.DATE, 1);
				dataFine.add(GregorianCalendar.MONTH, 1);
				dataFine.set(GregorianCalendar.DATE, 1);
				dataFine.add(GregorianCalendar.DATE, -1);
				dataAutomatica = true;
				tipoDataAutomatica = DATA_AUTOMATICA_MESE_CORRENTE;
				break;
		    	
	        case MESE_ATTUALE: // mese attuale (scelta statica)
	    		impostaDataAttuale();
	    		dataInizio.set(GregorianCalendar.DATE, offset);
	    		dataFine.set(GregorianCalendar.DATE, offset);
	    		dataFine.add(GregorianCalendar.MONTH, 1);
	    		dataFine.add(GregorianCalendar.DATE, -1);
	    		dataAutomatica = false;
				tipoDataAutomatica = -1;
	    		break;
	    		
	        case MESE_PRECEDENTE: // mese precedente
	    		impostaDataAttuale();
	    		dataInizio.add(GregorianCalendar.MONTH, -1);
	    		dataInizio.set(GregorianCalendar.DATE, offset);
	    		dataFine.set(GregorianCalendar.DATE, offset);
	     		dataFine.add(GregorianCalendar.DATE, -1);
	     		dataAutomatica = false;
				tipoDataAutomatica = -1;
	     		break;
	     		
	        case MESE_SUCCESSIVO: // mese successivo
	    		impostaDataAttuale();
	    		dataInizio.add(GregorianCalendar.MONTH, 1);
	    		dataInizio.set(GregorianCalendar.DATE, offset);
	    		dataFine.add(GregorianCalendar.MONTH, 2);
	    		dataFine.set(GregorianCalendar.DATE, offset);
	     		dataFine.add(GregorianCalendar.DATE, -1);
	     		dataAutomatica = false;
				tipoDataAutomatica = -1;
	     		break;
	        }
	        
	        timeLine.setDataInizio(dataInizio.getTimeInMillis());
	        timeLine.setDataFine(dataFine.getTimeInMillis());
	        timeLine.ridisegna();
	        aggiornaCampiDate();
	    }

	    public void onNothingSelected(AdapterView<?> parent) {
	        // Another interface callback
	    }
	};
	
	
	//listener per variazioni testo campo offset
	private TextWatcher etOffsetTextChangedListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			try {
				offset = Integer.parseInt(s.toString());
				if(offset < 1) {
					offset = 1;
					etOffset.setText("" + offset);
				}
				if(offset > 25) {
					offset = 25;
					etOffset.setText("" + offset);
				}
				aggiornaDateConNuovoOffset(spScelteRapide.getSelectedItemPosition());
			}
			catch(NumberFormatException e) {
				offset = 1;
			}
			
		}	
	};
	
	
	/*
	 * imposta dataInizio e dataFine entrambe sulla data attuale
	 */
	private void impostaDataAttuale() {
		dataInizio = new GregorianCalendar();
		int giorno = dataInizio.get(GregorianCalendar.DATE);
		int mese = dataInizio.get(GregorianCalendar.MONTH);
		int anno = dataInizio.get(GregorianCalendar.YEAR);
		dataInizio = new GregorianCalendar(anno, mese, giorno);
		dataFine.set(GregorianCalendar.DATE, dataInizio.get(GregorianCalendar.DATE));
		dataFine.set(GregorianCalendar.MONTH, dataInizio.get(GregorianCalendar.MONTH));
		dataFine.set(GregorianCalendar.YEAR, dataInizio.get(GregorianCalendar.YEAR));
	}
	
	
	/*Quando cambio l'offset del mese e ho impostato un mese specifico, aggiorno le date.
	 */
	private void aggiornaDateConNuovoOffset(int pos) {
        switch(pos) {

		case MESE_CORRENTE: // mese attuale (scelta statica) con offset
			impostaDataAttuale();
			dataInizio.set(GregorianCalendar.DATE, offset);
			dataFine.set(GregorianCalendar.DATE, offset);
			dataFine.add(GregorianCalendar.MONTH, 1);
			dataFine.add(GregorianCalendar.DATE, -1);
			dataAutomatica = true;
			break;

        case MESE_ATTUALE: // mese attuale (scelta statica) con offset
    		impostaDataAttuale();
    		dataInizio.set(GregorianCalendar.DATE, offset);
    		dataFine.set(GregorianCalendar.DATE, offset);
    		dataFine.add(GregorianCalendar.MONTH, 1);
    		dataFine.add(GregorianCalendar.DATE, -1);
    		dataAutomatica = false;
    		break;
    		
        case MESE_PRECEDENTE: // mese precedente
    		impostaDataAttuale();
    		dataInizio.add(GregorianCalendar.MONTH, -1);
    		dataInizio.set(GregorianCalendar.DATE, offset);
    		dataFine.set(GregorianCalendar.DATE, offset);
     		dataFine.add(GregorianCalendar.DATE, -1);
     		dataAutomatica = false;
     		break;
     		
        case MESE_SUCCESSIVO: // mese successivo
    		impostaDataAttuale();
    		dataInizio.add(GregorianCalendar.MONTH, 1);
    		dataInizio.set(GregorianCalendar.DATE, offset);
    		dataFine.add(GregorianCalendar.MONTH, 2);
    		dataFine.set(GregorianCalendar.DATE, offset);
     		dataFine.add(GregorianCalendar.DATE, -1);
     		dataAutomatica = false;
     		break;
        }
        
        timeLine.setDataInizio(dataInizio.getTimeInMillis());
        timeLine.setDataFine(dataFine.getTimeInMillis());
        timeLine.ridisegna();
        aggiornaCampiDate();
	}
		
	
	private void conferma() {
		if(dataInizio.after(dataFine)) {
			new MioToast(MenuPeriodo.this, getString(R.string.periodo_errore)).visualizza(Toast.LENGTH_SHORT);
		}
		else {
			Intent returnIntent = new Intent();
			returnIntent.putExtra(DATA_INIZIO, dataInizio.getTimeInMillis());
			returnIntent.putExtra(DATA_FINE, dataFine.getTimeInMillis());
			returnIntent.putExtra(DATA_AUTOMATICA, dataAutomatica);
			returnIntent.putExtra(TIPO_DATA_AUTOMATICA, tipoDataAutomatica);
			setResult(FragmentActivity.RESULT_OK, returnIntent);
			
			//registro nelle preferenze il nuovo valore offset
			SharedPreferences prefTempo = PreferenceManager.getDefaultSharedPreferences(MenuPeriodo.this);
			SharedPreferences.Editor preferencesEditor = prefTempo.edit();
			preferencesEditor.putInt(DATA_OFFSET, offset);
			preferencesEditor.apply();
			
			finish();
			}
	}


	//variabili di istanza
	private boolean dataAutomatica = false;
	private int tipoDataAutomatica = -1;
	private int offset = 0; //il mese finanziario inizia dal giorno...
	private EditText etInizio;
	private EditText etFine;
	private EditText etOffset;
	private Spinner spScelteRapide;
	private MenuPeriodoTimeline timeLine;
	private GregorianCalendar dataInizio;
	private GregorianCalendar dataFine;
	private DateFormat df;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);

}