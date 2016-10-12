/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCEntrateRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.ContiElenco.ContoDettaglio;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_CONTO;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_CONTO_DEFAULT;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_DATA_SALDO;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_ENTRATE_INCASSATE;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_ID;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_SALDO;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_SALDO_FINALE;
import static com.flingsoftware.personalbudget.app.ContiElenco.EXTRA_SPESE_SOSTENUTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.CONTO_DEFAULT;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;


public class ContiVisualizza extends ActionBarActivity {
	
	// Costanti
	private static final int OP_MODIFICA = 0;
	
	// Variabili
	private ContoDettaglio contoDettaglio = new ContoDettaglio();
	private Currency currValuta;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private MenuItem menuElimina;
	private MenuItem menuModifica;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);
		setContentView(R.layout.conti_visualizza);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		// Recupero valori passati dall'Activity ContiElenco
		Bundle extras = getIntent().getExtras();
		contoDettaglio.setId(extras.getLong(EXTRA_ID));
		contoDettaglio.setConto(extras.getString(EXTRA_CONTO));
		contoDettaglio.setSaldo(extras.getDouble(EXTRA_SALDO));
		contoDettaglio.setDataSaldo(extras.getLong(EXTRA_DATA_SALDO));
		contoDettaglio.setEntrateIncassate(extras.getDouble(EXTRA_ENTRATE_INCASSATE));
		contoDettaglio.setSpeseSostenute(extras.getDouble(EXTRA_SPESE_SOSTENUTE));
		contoDettaglio.setSaldoFinale(extras.getDouble(EXTRA_SALDO_FINALE));
		contoDettaglio.setContoDefault(extras.getBoolean(EXTRA_CONTO_DEFAULT));
		
		// Impostazione componenti
		TextView tvNome = (TextView) findViewById(R.id.cv_tvNome);
		TextView tvSaldo = (TextView) findViewById(R.id.cv_tvSaldo);
		TextView tvDataSaldo = (TextView) findViewById(R.id.cv_tvDataSaldo);
		TextView tvEntrate = (TextView) findViewById(R.id.cv_tvEntrate);
		TextView tvSpese = (TextView) findViewById(R.id.cv_tvSpese);
		TextView tvSaldoFinale = (TextView) findViewById(R.id.cv_tvSaldoFinale);
		CheckBox cbContoDefault = (CheckBox) findViewById(R.id.cv_cbContoDefault);
		
		ricavaValuta();
		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf.setCurrency(currValuta);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
		tvNome.setText(contoDettaglio.getConto());
		tvSaldo.setText(nf.format(contoDettaglio.getSaldo()));
		tvDataSaldo.setText(df.format(new Date(contoDettaglio.getDataSaldo())));
		tvEntrate.setText(nf.format(contoDettaglio.getEntrateIncassate()));
		tvSpese.setText(nf.format(contoDettaglio.getSpeseSostenute()));
		double saldoFinale = contoDettaglio.getSaldoFinale();
		if(saldoFinale >= 0) {
			tvSaldoFinale.setTextColor(Color.argb(255, 0, 128, 0));
		}
		else {
			tvSaldoFinale.setTextColor(Color.RED);
		}
		tvSaldoFinale.setText(nf.format(saldoFinale));

		if(contoDettaglio.getContoDefault()) {
			cbContoDefault.setChecked(true);
		}
		cbContoDefault.setEnabled(false);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	
	// Ricavo la valuta principale salvata nelle preferenze.
	private void ricavaValuta() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String codiceValutaDefault = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(codiceValutaDefault);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conti_visualizza, menu);
		menuElimina = menu.getItem(0);
		if(contoDettaglio.getId() == 1) {
			menuElimina.setEnabled(false);
			menuElimina.setVisible(false);
		}
		menuModifica = menu.getItem(1);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            }
            else {
                finish();
            }
	        
	        return true;
		case R.id.menu_conti_visualizza_elimina:
			eliminaConto();
	        
	        return true;
		case R.id.menu_conti_visualizza_modifica:
			Intent intent = new Intent(this, ContiModifica.class);
			intent.putExtra(EXTRA_ID, contoDettaglio.getId());
			intent.putExtra(EXTRA_CONTO, contoDettaglio.getConto());
			intent.putExtra(EXTRA_SALDO, contoDettaglio.getSaldo());
			intent.putExtra(EXTRA_DATA_SALDO, contoDettaglio.getDataSaldo());
			intent.putExtra(EXTRA_CONTO_DEFAULT, contoDettaglio.getContoDefault());
			startActivityForResult(intent, OP_MODIFICA);

			return true;	
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	private void eliminaConto() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ContiVisualizza.this);
		builder.setTitle(R.string.conti_visualizza_eliminaTitolo);
		builder.setMessage(R.string.conti_visualizza_eliminaTesto);
		builder.setNegativeButton(R.string.generici_annulla, null);
		builder.setPositiveButton(R.string.generici_OK, new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				menuElimina.setEnabled(false);
				menuModifica.setEnabled(false);
				new EliminaContoTask().execute((Object[]) null);
			}
		});
		builder.setCancelable(true);
		AlertDialog confirmDialog = builder.create();
		confirmDialog.show();
	}
	
	
	// Elimino il conto dalla tabella conti e aggiorno tutte le voci di quel conto a default
	private class EliminaContoTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			// Se questo è il conto di default, ripristino "default" come conto predefinito
			if(contoDettaglio.getContoDefault()) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ContiVisualizza.this);
				SharedPreferences.Editor prefEditor = pref.edit();
				prefEditor.putString(CONTO_DEFAULT, "default");
				prefEditor.apply();
			}

			// Elimino il conto dalla tabella conti
			DBCConti dbcConti = new DBCConti(ContiVisualizza.this);
			dbcConti.eliminaConto(contoDettaglio.getId());
			
			// Tabella spese sostenute: le spese con il conto eliminato passano al conto default
			DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(ContiVisualizza.this);
			dbcSpeseSostenute.openModifica();
			Cursor curSpeseSost = dbcSpeseSostenute.getTutteLeSpeseContoX(contoDettaglio.getConto());
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

				dbcSpeseSostenute.updateElement(id, data, voce, importo, valuta, importoValprin, descrizione, ripetizioneId, "default", favorite);
			}			
			curSpeseSost.close();
			dbcSpeseSostenute.close();
			
			// Tabella spese ripetute: le spese con il conto eliminato passano al conto default
			DBCSpeseRipetute dbcSpeseRipetute = new DBCSpeseRipetute(ContiVisualizza.this);
			dbcSpeseRipetute.openModifica();
			Cursor curSpeseRip = dbcSpeseRipetute.getTutteLeSpeseContoX(contoDettaglio.getConto());
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

				dbcSpeseRipetute.updateElement(id, voce, ripetizione, importo, valuta, importoValprin, descrizione, dataInizio, flagFine, dataFine, aggiornatoA, "default");
			}			
			curSpeseRip.close();
			dbcSpeseRipetute.close();
			
			// Tabella entrate incassate: le entrate con il conto eliminato passano al conto default
			DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(ContiVisualizza.this);
			dbcEntrateIncassate.openModifica();
			Cursor curEntrateInc = dbcEntrateIncassate.getTutteLeEntrateContoX(contoDettaglio.getConto());
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

				dbcEntrateIncassate.updateElement(id, data, voce, importo, valuta, importoValprin, descrizione, ripetizioneId, "default", favorite);
			}			
			curEntrateInc.close();
			dbcEntrateIncassate.close();
			
			// Tabella entrate ripetute: le entrate con il conto eliminato passano al conto default
			DBCEntrateRipetute dbcEntrateRipetute = new DBCEntrateRipetute(ContiVisualizza.this);
			dbcEntrateRipetute.openModifica();
			Cursor curEntrateRip = dbcEntrateRipetute.getTutteLeEntrateContoX(contoDettaglio.getConto());
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

				dbcEntrateRipetute.updateElement(id, voce, ripetizione, importo, valuta, importoValprin, descrizione, dataInizio, flagFine, dataFine, aggiornatoA, "default");
			}			
			curEntrateRip.close();
			dbcEntrateRipetute.close();
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			new MioToast(ContiVisualizza.this, getString(R.string.conti_visualizza_contoEliminato)).visualizza(Toast.LENGTH_SHORT);
			finish();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == OP_MODIFICA && resultCode == Activity.RESULT_OK) {
			finish();
		}
	}


}
