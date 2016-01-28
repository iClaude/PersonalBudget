package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import static com.flingsoftware.personalbudget.app.BudgetDettaglio.CostantiPubbliche.*;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.*;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.support.v7.app.ActionBarActivity;


public class BudgetBudgetAnaloghi extends ActionBarActivity {

	//costanti
	private static final String ROW_ID = "row_id";
	private static final String CHIAMANTE = "chiamante";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.budget_budgetanaloghi);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		ricavaValuta();
	
		//recupero i valori di budget_iniziale e icona passati dall'Activity chiamante
		Bundle extras = getIntent().getExtras();
		budgetIniziale = extras.getLong(BUDGET_BUDGET_INIZIALE);
		int iconaId = extras.getInt(BUDGET_ICONAID);
		new PlaceHolderWorkerTask().execute(iconaId);
		
		//imposto la ListView dei budget analoghi
		lvBudget = (ListView) findViewById(R.id.budget_budgetanaloghi_lvBudget);
		lvBudget.setOnItemClickListener(lvBudgetListener);
		
		//collegamento ListView - database
		String from[] = new String[] {"data_inizio", "data_fine", "importo_valprin", "spesa_sost"};
		int to[] = {R.id.budget_budgetanaloghi_listviewitem_tvDataInizio, R.id.budget_budgetanaloghi_listviewitem_tvDataFine, R.id.budget_budgetanaloghi_listviewitem_tvImportoBudget, R.id.budget_budgetanaloghi_listviewitem_tvSpesaSostComodo};
		budgetAdapter = new SimpleCursorAdapter(this, R.layout.budget_budgetanaloghi_listviewitem, null, from, to);
		
		//ViewBinder associato all'Adapter per personalizzare la vista di ogni elemento
		df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
		nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		final DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
		((SimpleCursorAdapter) budgetAdapter).setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				
				switch(view.getId()) {
				case R.id.budget_budgetanaloghi_listviewitem_tvImportoBudget:
					double budget = cursor.getDouble(columnIndex);
					
					if(!valutaAlternativa) {
						String budgetFormattato = nf.format(budget);
						((TextView) view).setText(budgetFormattato);
						((ProgressBar) ((View) (view.getParent())).findViewById(R.id.budget_budgetanaloghi_listviewitem_pbProgresso)).setMax((int) budget);
						((TextView) ((View) (view.getParent())).findViewById(R.id.budget_budgetanaloghi_listviewitem_tvValuta)).setText("");
					}
					else {
						String budgetFormattato = nfRidotto.format(budget);
						((TextView) view).setText(budgetFormattato);
						((ProgressBar) ((View) (view.getParent())).findViewById(R.id.budget_budgetanaloghi_listviewitem_pbProgresso)).setMax((int) budget);
						((TextView) ((View) (view.getParent())).findViewById(R.id.budget_budgetanaloghi_listviewitem_tvValuta)).setText("(" + currValutaPrinc.getSymbol() + ")");
					}
					
					return true;	
					
				case R.id.budget_budgetanaloghi_listviewitem_tvDataInizio:
					long dataInizio = cursor.getLong(columnIndex);
					((TextView) view).setText(df.format(new Date(dataInizio)));
					
					return true;
					
				case R.id.budget_budgetanaloghi_listviewitem_tvDataFine:
					long dataFine = cursor.getLong(columnIndex);
					((TextView) view).setText(df.format(new Date(dataFine)));
					
					return true;
				
				case R.id.budget_budgetanaloghi_listviewitem_tvSpesaSostComodo:
					double spesaSost = cursor.getDouble(columnIndex);
					double importoBudget = cursor.getDouble(cursor.getColumnIndex("importo_valprin"));
					double risparmiato = importoBudget - spesaSost;

					ProgressBar mProgressBar = (ProgressBar) ((View) (view.getParent())).findViewById(R.id.budget_budgetanaloghi_listviewitem_pbProgresso);

					if(spesaSost>importoBudget) {
                        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_accent));
					}
					else {
                        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_standard));
					}
                    mProgressBar.setProgress((int) spesaSost);
					
					String str2 = risparmiato >= 0 ? getString(R.string.budget_budgetanaloghi_risparmiato) : getString(R.string.budget_budgetanaloghi_ecceduto);				
					((TextView) ((View) (view.getParent())).findViewById(R.id.budget_budgetanaloghi_listviewitem_tvScostamento)).setText(nfRidotto.format(risparmiato) + " " + str2);
					
					//icona budget
					((ImageView) ((View) (view.getParent())).findViewById(R.id.fs_ivMaiale)).setImageBitmap(mPlaceHolderBitmap);
					return true;
				}
			
				return false;
			}
		});
		
		lvBudget.setAdapter(budgetAdapter);
		aggiornaCursor();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		new RecuperaBudgetAnaloghiTask().execute((Object[]) null);
	}
	
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	// ricavo la valuta principale dal file delle preferenze
	private void ricavaValuta() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String valuta = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValutaPrinc = Currency.getInstance(valuta);
		valutaAlternativa = !currValutaPrinc.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}
	
	
	//aggiorna la ListView recuperando i dati dal database
	public void aggiornaCursor() {
		new RecuperaBudgetAnaloghiTask().execute((Object[]) null);
	}
	
	
	//AsyncTask per recuperare l'elenco completo dei budget da visualizzare nella ListView
	private class RecuperaBudgetAnaloghiTask extends AsyncTask<Object, Void, Cursor> {
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetBudgetAnaloghi.this);
		
		protected Cursor doInBackground(Object... params) {
			dbcSpeseBudget.openLettura();
			
			return dbcSpeseBudget.getSpeseBudgetElencoBudgetAnaloghi(budgetIniziale);
		}
		
		protected void onPostExecute(Cursor curBudget) {
			budgetAdapter.changeCursor(curBudget);
			dbcSpeseBudget.close();
		}
	}
	
	
	//recupero icona placeholder della listview
	private class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {   
	    @Override
	    protected Object doInBackground(Integer... params) {
	    	mPlaceHolderBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 50, 50);
	    	return null;
	    }
	}
	
	
	//gestione click su elementi della ListView
	OnItemClickListener lvBudgetListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent dettaglioBudget = new Intent(BudgetBudgetAnaloghi.this, BudgetDettaglio.class);
			dettaglioBudget.putExtra(ROW_ID, arg3);
			dettaglioBudget.putExtra(CHIAMANTE, ACTIVITY_BUDGET_BUDGETANALOGHI);

            startActivityForResult(dettaglioBudget, ACTIVITY_BUDGET_DETTAGLIOVOCE);
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			modifiche = true;
			
			aggiornaCursor();
		}
	}
	

	@Override
	public void onBackPressed() {
		if(modifiche) {
			setResult(Activity.RESULT_OK);
		}
		
		super.onBackPressed();
	}
	

	@Override
	public void onStop() {
		Cursor cursor = budgetAdapter.getCursor();
		if(cursor != null) {
			cursor.close();
		}
		budgetAdapter.changeCursor(null);
		
		super.onStop();
	}
	
	
	//variabili di istanza
	private CursorAdapter budgetAdapter;
	private DateFormat df;
	private NumberFormat nf;
	private long budgetIniziale;
	private boolean modifiche; // ho fatto qualche modifica alle spese incluse?
	private Currency currValutaPrinc;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private boolean valutaAlternativa;
	private Bitmap mPlaceHolderBitmap;
	
	private ListView lvBudget;

}
