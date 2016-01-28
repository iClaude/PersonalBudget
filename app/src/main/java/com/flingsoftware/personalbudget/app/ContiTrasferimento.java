package com.flingsoftware.personalbudget.app;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;

import java.util.Currency;
import java.util.Locale;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;


public class ContiTrasferimento extends ActionBarActivity {

	// Variabili
	private MenuItem menuOK;
	private Spinner spDa;
	private Spinner spA;
	private EditText etImporto;
	private SimpleCursorAdapter spContiAdapter;
	private String contoDa;
	private String contoA;
	private double importoTrasf;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conti_trasferimento);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		spDa = (Spinner) findViewById(R.id.ct_spDa);
		spA = (Spinner) findViewById(R.id.ct_spA);
		etImporto = (EditText) findViewById(R.id.ct_etImporto);
		
		// Imposto Spinner dei conti (CursorAdapter)
		String[] adapterCols = new String[]{"conto"};
		int[] adapterRowViews = new int[]{android.R.id.text1};
		spContiAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, adapterCols, adapterRowViews, 0);
		spContiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spDa.setAdapter(spContiAdapter);
		spA.setAdapter(spContiAdapter);
		new RecuperaContiTask().execute((Object[]) null);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conti_trasferimento, menu);
		menuOK = menu.getItem(0);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
	        
	        return true;
		case R.id.menu_ct_OK:
			menuOK.setEnabled(false);
			conferma();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	// Recupero elenco conti per gli Spinner in un thread separato
	private class RecuperaContiTask extends AsyncTask<Object, Object, Cursor> {
		DBCConti dbcConti = new DBCConti(ContiTrasferimento.this);
		
		protected Cursor doInBackground(Object... params) {
			dbcConti.openLettura();
			Cursor curConti = dbcConti.getTuttiIConti();
			
			return curConti;
		}
		
		protected void onPostExecute(Cursor result) {
			spContiAdapter.changeCursor(result);
			spContiAdapter.notifyDataSetChanged();
			
			dbcConti.close();
		}
	}
	
	
	// Conferma operazione di trasferimento
	private void conferma() {
		// Ricavo i dati inseriti dall'utente
		Cursor curConto = (Cursor) spDa.getSelectedItem();
		if(curConto != null) {
			contoDa = curConto.getString(curConto.getColumnIndex("conto"));
		}
		
		curConto = (Cursor) spA.getSelectedItem();
		if(curConto != null) {
			contoA = curConto.getString(curConto.getColumnIndex("conto"));
		}
		
		importoTrasf = 0;
		try {
			importoTrasf = Double.parseDouble(etImporto.getText().toString());
		} 
		catch (NumberFormatException exc) {
			importoTrasf = 0;
		}
		
		if(contoDa.equals(contoA)) {
			new MioToast(ContiTrasferimento.this, getString(R.string.conti_trasferimento_contiUguali)).visualizza(Toast.LENGTH_SHORT);
			menuOK.setEnabled(true);
		}
		else if(importoTrasf == 0) {
			new MioToast(ContiTrasferimento.this, getString(R.string.conti_trasferimento_importoZero)).visualizza(Toast.LENGTH_SHORT);
			menuOK.setEnabled(true);
		}
		else {
			new TrasferimentoTask().execute((Object[]) null);
		}
	}
	
	
	// Operazione di trasferimento in un thread separato
	private class TrasferimentoTask extends AsyncTask<Object, Object, Object> {
		
		protected Object doInBackground(Object... params) {
			long oggi = FunzioniComuni.getDataAttuale();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ContiTrasferimento.this);
			String valuta = sharedPreferences.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			String voce = getString(R.string.voce_giroconto);
			
			DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(ContiTrasferimento.this);
			dbcSpeseSostenute.inserisciSpesaSostenuta(oggi, voce, importoTrasf, valuta, importoTrasf, voce + " " + contoA, 1, contoDa, 0);
			
			DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(ContiTrasferimento.this);
			dbcEntrateIncassate.inserisciEntrataIncassata(oggi, voce, importoTrasf, valuta, importoTrasf, voce + " " + contoDa, 1, contoA, 0);

			return null;
		}
		
		protected void onPostExecute(Object result) {
			new MioToast(ContiTrasferimento.this, getString(R.string.conti_trasferimento_trasferimentoCompletato)).visualizza(Toast.LENGTH_SHORT);
			
			setResult(Activity.RESULT_OK);
			finish();
		}
	}
}
