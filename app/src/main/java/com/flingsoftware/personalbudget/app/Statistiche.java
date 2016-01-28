package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.ID_DATEPICKER;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.customviews.MioToast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.app.ActivityOptions;


public class Statistiche extends ActionBarActivity implements DatePickerFragment.DialogFinishedListener {

	//costanti date per TimePicker
	private static final int DATA_INIZIALE = 0;
	private static final int DATA_FINALE = 1;
	private static final int DATA_INIZIALE2 = 2;
	private static final int DATA_FINALE2 = 3;
	
	//costanti tipo grafici da usare nell'Activity che visualizza il grafico
	public interface CostantiGrafici {
		int GRAFICO_TORTA = 0;
		int GRAFICO_BARRE = 1;
		int GRAFICO_TEMPORALE = 2;
		int GRAFICO_TEMPORALE_TAG = 3;
		int GRAFICO_DIAL = 4;
		int GRAFICO_RANGE = 5;
		int GRAFICO_DOUGHNUT = 6;
	}
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }

		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistiche);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		spese = true;
		
		//periodo di default: mese corrente
		dataInizio.setTimeInMillis(FunzioniComuni.getDataAttuale());
		dataFine.setTimeInMillis(FunzioniComuni.getDataAttuale());
		dataInizio.set(Calendar.DAY_OF_MONTH, 1);
		dataFine.set(Calendar.DAY_OF_MONTH, dataInizio.getActualMaximum(Calendar.DAY_OF_MONTH));
		dataInizio2.setTimeInMillis(dataInizio.getTimeInMillis());
		dataFine2.setTimeInMillis(dataFine.getTimeInMillis());
		
		df= DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
		etDataInizio = (EditText) findViewById(R.id.statistiche_etInizio);
		etDataFine = (EditText) findViewById(R.id.statistiche_etFine);
		etDataInizio.setText(Html.fromHtml(getResources().getString(R.string.statistiche_iniziaDa) + "<b>" + " " + df.format(dataInizio.getTime())));
		etDataFine.setText(Html.fromHtml(getResources().getString(R.string.statistiche_terminaA) + "<b>" + " " + df.format(dataFine.getTime())));
		etDataInizio2 = (EditText) findViewById(R.id.statistiche_etInizio2);
		etDataFine2 = (EditText) findViewById(R.id.statistiche_etFine2);
		etDataInizio2.setText(Html.fromHtml(getResources().getString(R.string.statistiche_iniziaDa) + "<b>" + " " + df.format(dataInizio2.getTime())));
		etDataFine2.setText(Html.fromHtml(getResources().getString(R.string.statistiche_terminaA) + "<b>" + " " + df.format(dataFine2.getTime())));
		
		etDataInizio.setOnClickListener(etDataInizioOnClickListener);
		etDataFine.setOnClickListener(etDataFineOnClickListener);
		etDataInizio2.setOnClickListener(etDataInizioOnClickListener);
		etDataFine2.setOnClickListener(etDataFineOnClickListener);
		
		//tipo di grafico
		spGrafico = (Spinner) findViewById(R.id.statistiche_spGrafico);
		spVoci = (Spinner) findViewById(R.id.statistiche_spVoci);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.statistiche_grafici, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spGrafico.setAdapter(adapter);
		spGrafico.setOnItemSelectedListener(spGraficoListener);
		
		new RecuperaVociTask().execute((Object[]) null);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_statistiche, menu);
		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_statistiche_OK:
			conferma();

			return true;	
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	public void onRadioButtonClicked(View view) {
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    switch(view.getId()) {
	        case R.id.statistiche_rbSpese:
	            if (checked) {
	                spese = true;
					if(spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_TEMPORALE_TAG || spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DIAL) {
						spVoci.setAdapter(adapterVociSpese);
					}
	            }
	            break;
	        case R.id.statistiche_rbEntrate:
	            if (checked) {
	                spese = false;
					if(spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_TEMPORALE_TAG || spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DIAL) {
						spVoci.setAdapter(adapterVociEntrate);
					}
	            }
	            break;
	    }
	}
	
	
	public OnClickListener etDataInizioOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			if(v.getId() == R.id.statistiche_etInizio) {
				args.putInt(ID_DATEPICKER, DATA_INIZIALE);
			}
			else {
				args.putInt(ID_DATEPICKER, DATA_INIZIALE2);
			}
			dataFragment.setArguments(args);
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	public OnClickListener etDataFineOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			DialogFragment dataFragment = new DatePickerFragment();
			Bundle args = new Bundle();
			if(v.getId() == R.id.statistiche_etFine) {
				args.putInt(ID_DATEPICKER, DATA_FINALE);
			}
			else {
				args.putInt(ID_DATEPICKER, DATA_FINALE2);
			}
			dataFragment.setArguments(args);
			dataFragment.show(getSupportFragmentManager(), "dataPicker");
		}
	};
	
	
	public void onDialogFinished(int id, int year, int month, int day) {
		if(id == DATA_INIZIALE) {
			dataInizio.set(GregorianCalendar.DATE, day);
			dataInizio.set(GregorianCalendar.MONTH, month);
			dataInizio.set(GregorianCalendar.YEAR, year);
			
			etDataInizio.setText(Html.fromHtml(getResources().getString(R.string.statistiche_iniziaDa) + "<b>" + " " + df.format(dataInizio.getTime())));
		}
		else if(id == DATA_FINALE) {
			dataFine.set(GregorianCalendar.DATE, day);
			dataFine.set(GregorianCalendar.MONTH, month);
			dataFine.set(GregorianCalendar.YEAR, year);
			
			etDataFine.setText(Html.fromHtml(getResources().getString(R.string.statistiche_terminaA) + "<b>" + " " + df.format(dataFine.getTime())));
		}
		else if(id == DATA_INIZIALE2) {
			dataInizio2.set(GregorianCalendar.DATE, day);
			dataInizio2.set(GregorianCalendar.MONTH, month);
			dataInizio2.set(GregorianCalendar.YEAR, year);
			
			etDataInizio2.setText(Html.fromHtml(getResources().getString(R.string.statistiche_iniziaDa) + "<b>" + " " + df.format(dataInizio2.getTime())));
		}
		else if(id == DATA_FINALE2) {
			dataFine2.set(GregorianCalendar.DATE, day);
			dataFine2.set(GregorianCalendar.MONTH, month);
			dataFine2.set(GregorianCalendar.YEAR, year);
			
			etDataFine2.setText(Html.fromHtml(getResources().getString(R.string.statistiche_terminaA) + "<b>" + " " + df.format(dataFine2.getTime())));
		}	
	}
	
	
	private AdapterView.OnItemSelectedListener spGraficoListener = new AdapterView.OnItemSelectedListener() {		
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if((spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_TEMPORALE_TAG || spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DIAL) && spese) {
				spVoci.setVisibility(View.VISIBLE);
				spVoci.setAdapter(adapterVociSpese);
			}
			else if((spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_TEMPORALE_TAG || spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DIAL) && !spese) {
				spVoci.setVisibility(View.VISIBLE);
				spVoci.setAdapter(adapterVociEntrate);
			}
			else {
				spVoci.setVisibility(View.INVISIBLE);
			}
			
			if(spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DOUGHNUT) {
				findViewById(R.id.card_view_date2).setVisibility(View.VISIBLE);
			}
			else {
                findViewById(R.id.card_view_date2).setVisibility(View.GONE);
			}
		}
		
	    public void onNothingSelected(AdapterView<?> parent) {
	        // Another interface callback
	    }
	};
	
	
	private void conferma() {
		if(dataInizio.after(dataFine)) {
			new MioToast(Statistiche.this, getString(R.string.statistiche_periodoSbagliato)).visualizza(Toast.LENGTH_SHORT);
			
			return;
		}
		
		Intent intGrafico = new Intent(Statistiche.this, StatisticheGrafico.class);
		intGrafico.putExtra("spese", spese);
		intGrafico.putExtra("grafico", spGrafico.getSelectedItemPosition());
		if((spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_TEMPORALE_TAG || spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DIAL) && spese) {
			intGrafico.putExtra("voce", arrVociSpese[spVoci.getSelectedItemPosition()]);
		}
		else if ((spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_TEMPORALE_TAG || spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DIAL) && !spese) {
			intGrafico.putExtra("voce", arrVociEntrate[spVoci.getSelectedItemPosition()]);
		}		
		intGrafico.putExtra("data_inizio", dataInizio.getTimeInMillis());
		intGrafico.putExtra("data_fine", dataFine.getTimeInMillis());
		if(spGrafico.getSelectedItemPosition() == CostantiGrafici.GRAFICO_DOUGHNUT) {
			intGrafico.putExtra("data_inizio2", dataInizio2.getTimeInMillis());
			intGrafico.putExtra("data_fine2", dataFine2.getTimeInMillis());
		}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intGrafico, ActivityOptions.makeSceneTransitionAnimation(Statistiche.this).toBundle());
        }
        else {
            startActivity(intGrafico);
        }
	}
	
	
	private class RecuperaVociTask extends AsyncTask<Object, Object, Object> {
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(Statistiche.this);
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(Statistiche.this);
		
		
		protected Object doInBackground(Object... params) {
			Cursor cur;
			
			dbcSpeseVoci.openLettura();
			cur = dbcSpeseVoci.getTutteLeVoci();
			arrVociSpese = new String[cur.getCount()];
			for(int i=0; cur.moveToNext(); i++) {
				arrVociSpese[i] = cur.getString(cur.getColumnIndex("voce"));
			}
			
			dbcEntrateVoci.openLettura();
			cur = dbcEntrateVoci.getTutteLeVoci();
			arrVociEntrate = new String[cur.getCount()];
			for(int i=0; cur.moveToNext(); i++) {
				arrVociEntrate[i] = cur.getString(cur.getColumnIndex("voce"));
			}
			
			cur.close();
			dbcSpeseVoci.close();
			dbcEntrateVoci.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			adapterVociSpese = new ArrayAdapter<String>(Statistiche.this, android.R.layout.simple_spinner_item, arrVociSpese);
			adapterVociEntrate = new ArrayAdapter<String>(Statistiche.this, android.R.layout.simple_spinner_item, arrVociEntrate);
			adapterVociSpese.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			adapterVociEntrate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spVoci.setAdapter(adapterVociSpese);
		}
	}
	
	
	//variabili di istanza
	
	private boolean spese;
	private GregorianCalendar dataInizio = new GregorianCalendar();
	private GregorianCalendar dataFine = new GregorianCalendar();
	private GregorianCalendar dataInizio2 = new GregorianCalendar();
	private GregorianCalendar dataFine2 = new GregorianCalendar();
	private EditText etDataInizio;
	private EditText etDataFine;
	private EditText etDataInizio2;
	private EditText etDataFine2;
	private DateFormat df;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	
	private Spinner spGrafico;
	private Spinner spVoci;
	
	private String arrVociSpese[];
	private String arrVociEntrate[];
	private ArrayAdapter<String> adapterVociSpese;
	private ArrayAdapter<String> adapterVociEntrate;
}
