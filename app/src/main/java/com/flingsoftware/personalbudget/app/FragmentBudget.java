package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.*;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;

import com.flingsoftware.personalbudget.database.*;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.HashMap;

import android.os.Build;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class FragmentBudget extends ListFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	//costanti
	public static final String ROW_ID = "row_id";
	public static final String CHIAMANTE = "chiamante";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		iconeVeloci = new ListViewIconeVeloce(getActivity());
		new PlaceHolderWorkerTask().execute(R.drawable.img_budget);
		tipiBudget = getResources().getStringArray(R.array.ripetizioni_budget);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_budget, container, false);
		setHasOptionsMenu(true);
		
		ivWallet = (ImageView) rootView.findViewById(R.id.fragment_budget_ivWallet);
		tvToccaPiu = (TextView) rootView.findViewById(R.id.fragment_budget_tvToccaPiu);
		
		//registro l'ascoltatore per il cambio preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.registerOnSharedPreferenceChangeListener(this);
		ricavaPeriodo(pref);
		ricavaValuta(pref);
				
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//imposto la ListView dei budget
		lvBudget = getListView();
		lvBudget.setOnItemClickListener(lvBudgetListener);

		// Per avere l'effetto della Toolbar a scomparsa (funziona solo con Lollipop).
		// TODO: 19/07/2016 cercare soluzione per versioni precedenti
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			lvBudget.setNestedScrollingEnabled(true);
		}
		
		//collegamento ListView - database
		String from[] = new String[] {"voce_budget", "importo_valprin", "spesa_sost", "data_fine"};
		int to[] = {R.id.fragment_budget_listview_item_tvTipoBudget, R.id.fragment_budget_listview_item_tvImportoBudget, R.id.fragment_budget_listview_item_tvComodoSpesaSost, R.id.fragment_budget_listview_item_tvComodoDataFine};
		budgetAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_budget_listview_item, null, from, to);
		final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		final DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
		
		//ViewBinder associato all'Adapter per personalizzare la vista di ogni elemento
		((SimpleCursorAdapter) budgetAdapter).setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				
				switch(view.getId()) {
				case R.id.fragment_budget_listview_item_tvImportoBudget:
					double budget = cursor.getDouble(columnIndex);
					
					if(!valutaAlternativa) {
						String budgetFormattato = nf.format(budget);
						((TextView) view).setText(budgetFormattato);
						((ProgressBar) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_pbProgresso)).setMax((int) budget);
						((TextView) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvValuta)).setText("");
					}
					else {
						String budgetFormattato = nfRidotto.format(budget);
						((TextView) view).setText(budgetFormattato);
						((ProgressBar) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_pbProgresso)).setMax((int) budget);
						((TextView) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvValuta)).setText("(" + currValuta.getSymbol() + ")");
					}
					
					return true;	
					
				case R.id.fragment_budget_listview_item_tvComodoSpesaSost:
					double spesaSost = cursor.getDouble(columnIndex);
					double importoBudget = cursor.getDouble(cursor.getColumnIndex("importo_valprin"));

                    ProgressBar mProgressBar = (ProgressBar) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_pbProgresso);
                    if(spesaSost>=importoBudget) {
                        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_accent));
                    }
                    else {
                        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_standard));
                    }
					mProgressBar.setProgress((int) spesaSost);

					return true;
					
				case R.id.fragment_budget_listview_item_tvComodoDataFine:
					//long dataFineBudget = cursor.getLong(columnIndex);
					
					return true;

				case R.id.fragment_budget_listview_item_tvTipoBudget:
					String voceGrezza = cursor.getString(columnIndex);
					if(voceGrezza.endsWith(",")) {
						voceGrezza = voceGrezza.substring(0, voceGrezza.length() - 1);
					}
					
					String tipoBudget = voceGrezza.substring(0, voceGrezza.indexOf(' '));
					String vociInteressate = voceGrezza.substring(voceGrezza.indexOf(' ') + 1);
					if(tipoBudget.equals("una_tantum")) {
						tipoBudget = tipiBudget[0];
					}
					else if(tipoBudget.equals("giornaliero")) {
						tipoBudget = tipiBudget[1];
					}
					else if(tipoBudget.equals("settimanale")) {
						tipoBudget = tipiBudget[2];
					}
					else if(tipoBudget.equals("bisettimanale")) {
						tipoBudget = tipiBudget[3];
					}
					else if(tipoBudget.equals("mensile")) {
						tipoBudget = tipiBudget[4];
					}
					else if(tipoBudget.equals("annuale")) {
						tipoBudget = tipiBudget[5];
					}
					((TextView) view).setText(tipoBudget);
										
					//testo scorrevole sulle voci del budget
					((TextView) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvVoci)).setText(vociInteressate);
					((TextView) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvVoci)).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvVoci)).setSingleLine(true);
					((TextView) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvVoci)).setMarqueeRepeatLimit(5);
					((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_tvVoci).setSelected(true);
					
					//icona
					ImageView ivIcona = (ImageView) (((View) (view.getParent())).findViewById(R.id.menu_esporta_ivFormato));
					Integer icona = hmIcone.get(vociInteressate);
					if(icona == null) {
						ivIcona.setImageBitmap(mPlaceHolderBitmap);
					}
					else {
						iconeVeloci.loadBitmap(icona, ivIcona, mPlaceHolderBitmap, 50, 50);
					}
					
					return true;
				}
				
				return false;
			}
		});
		
		setListAdapter(budgetAdapter);
		aggiornaCursor();
	}

	
	//Nascondo la voce di menu visualizza per voce e favoriti.
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem item = menu.findItem(R.id.menu_visualizzaPerVoce);
		item.setVisible(false);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_ricerca));
		searchView.setIconified(true);
	}
	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		//deregistro l'ascoltatore per il cambio preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.unregisterOnSharedPreferenceChangeListener(this);
	}

	/*
	 * Ricavo il periodo di riferimento dal file delle preferenze.
	 */
	private void ricavaPeriodo(SharedPreferences prefTempo) {
		GregorianCalendar dataComodo = new GregorianCalendar();
		int mese = dataComodo.get(GregorianCalendar.MONTH);
		int anno = dataComodo.get(GregorianCalendar.YEAR);
		dataComodo = new GregorianCalendar(anno, mese, 1);
	
		long dataInizioLong = prefTempo.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
		dataInizio.setTimeInMillis(dataInizioLong);
		
		dataComodo.add(GregorianCalendar.MONTH, 1);
		dataComodo.add(GregorianCalendar.DATE, -1);
		long dataFineLong = prefTempo.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
		dataFine.setTimeInMillis(dataFineLong);
	}
	
	/*
	 * Ricavo il codice e il simbolo della valuta principale.
	 */
	private void ricavaValuta(SharedPreferences prefValuta) {
		String valuta = prefValuta.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(valuta);
		valutaAlternativa = !currValuta.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}

	
	/*
	 * Listener per il cambio delle preferenze.
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(CostantiPreferenze.DATA_INIZIO) || key.equals(CostantiPreferenze.DATA_FINE)) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);
			
			if(key.equals(CostantiPreferenze.DATA_INIZIO)) {
				long dataInizioLong = sharedPreferences.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
				dataInizio.setTimeInMillis(dataInizioLong);
			}
			else if(key.equals(CostantiPreferenze.DATA_FINE)) {
				dataComodo.add(GregorianCalendar.MONTH, 1);
				dataComodo.add(GregorianCalendar.DATE, -1);
				long dataFineLong = sharedPreferences.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
				dataFine.setTimeInMillis(dataFineLong);
			}
		}
		else if(key.equals(CostantiPreferenze.VALUTA_PRINCIPALE)) {
				String valuta = sharedPreferences.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
				currValuta = Currency.getInstance(valuta);
			valutaAlternativa = !currValuta.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
				lvBudget.invalidateViews();
		}
	}
	
	//aggiorna la ListView recuperando i dati dal database
	public void aggiornaCursor() {
		new RecuperaElencoCompletoBudgetTask().execute((Object[]) null);
	}
	
	/* Metodo chiamato da MainPersonalBudget quando imposto una stringa di ricerca per filtrare l'elenco
	 * delle spese.
	 */
	public void impostaRicerca(String str) {
		ricerca.delete(0, ricerca.length());
		ricerca.append(str);
		
		aggiornaCursor();
	}
	
	//gestione click su elementi della ListView
	OnItemClickListener lvBudgetListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent dettaglioBudget = new Intent(getActivity(), BudgetDettaglio.class);
			dettaglioBudget.putExtra(ROW_ID, arg3);
			dettaglioBudget.putExtra(CHIAMANTE, ACTIVITY_MAIN);

            getActivity().startActivityForResult(dettaglioBudget, ACTIVITY_BUDGET_DETTAGLIOVOCE);
		}
	};
	
	//AsyncTask per recuperare l'elenco completo dei budget da visualizzare nella ListView
	private class RecuperaElencoCompletoBudgetTask extends AsyncTask<Object, Void, Cursor> {
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(getActivity());
		
		protected Cursor doInBackground(Object... params) {
			//recupero info su icone voci
	    	DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(getActivity());
	    	dbcSpeseVoci.openLettura();
	    	Cursor curVoci = dbcSpeseVoci.getTutteLeVoci();
	    	while(curVoci.moveToNext()) {
	    		String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
	    		int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
	    		hmIcone.put(voce, icona);
	    	}
	    	curVoci.close();
	    	dbcSpeseVoci.close();
	    	
	    	//recupero budget
			dbcSpeseBudget.openLettura();
			
			return dbcSpeseBudget.getSpeseBudgetElencoNonScadutiFiltrato(FunzioniComuni.getDataAttuale(), ricerca.toString());
		}
		
		protected void onPostExecute(Cursor curBudget) {
			try {
				budgetAdapter.changeCursor(curBudget);
				dbcSpeseBudget.close();
				
				if(curBudget.getCount() > 0) {
					ivWallet.setVisibility(View.INVISIBLE);
					tvToccaPiu.setVisibility(View.INVISIBLE);
				}
				else {
					ivWallet.setVisibility(View.VISIBLE);
					tvToccaPiu.setVisibility(View.VISIBLE);
				}
			}
			catch(NullPointerException exc) {
				return;
			}
		}
	}
	
	//metodi pubblici per recuperare le date iniziali e finali di periodo
	public GregorianCalendar getDataInizio() {
		return dataInizio;
	}
	
	public GregorianCalendar getDataFine() {
		return dataFine;
	}
	
	
	//aggiungere un budget
	public void aggiungi() {
		Intent aggiungiBudget = new Intent(getActivity(), BudgetAggiungi.class);
		getActivity().startActivityForResult(aggiungiBudget, ACTIVITY_BUDGET_AGGIUNGI);
	}

	
	@Override
	public void onDestroy() {
		Cursor cursor = budgetAdapter.getCursor();
		if(cursor != null) {
			cursor.close();
		}
		budgetAdapter.changeCursor(null);
		
		super.onDestroy();
	}
	
	
	class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {   
	    // Decode image in background.
	    @Override
	    protected Object doInBackground(Integer... params) {
	    	mPlaceHolderBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 50, 50);
	    	return null;
	    }
	}

	
	//variabili di istanza
	private StringBuilder ricerca = new StringBuilder(); //filtro per le ricerche
	private String[] tipiBudget;
	private GregorianCalendar dataInizio = new GregorianCalendar();
	private GregorianCalendar dataFine = new GregorianCalendar();
	private CursorAdapter budgetAdapter;
	private Currency currValuta;
	private boolean valutaAlternativa;
	
	private ListView lvBudget;
	private ImageView ivWallet;
	private TextView tvToccaPiu;
	
	private ListViewIconeVeloce iconeVeloci;
	private Bitmap mPlaceHolderBitmap;
	private HashMap<String,Integer> hmIcone = new HashMap<String,Integer>();
}
