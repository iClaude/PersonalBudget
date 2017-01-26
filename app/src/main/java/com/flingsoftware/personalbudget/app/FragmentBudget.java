/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.app.budgets.BudgetDetails;
import com.flingsoftware.personalbudget.app.utility.TagsColors;
import com.flingsoftware.personalbudget.customviews.TextViewWithBackground;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.ACTIVITY_BUDGET_AGGIUNGI;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.ACTIVITY_BUDGET_DETTAGLIOVOCE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;


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
        String from[] = new String[]{"ripetizione", "voce", "risparmio", "spesa_sost"};
        int to[] = {R.id.tvBudgetType, R.id.tvTag, R.id.tvSaved, R.id.tvPerc};
        budgetAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_budget_listview_item, null, from, to);
        final NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		final DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
		
		//ViewBinder associato all'Adapter per personalizzare la vista di ogni elemento
		((SimpleCursorAdapter) budgetAdapter).setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				
				switch(view.getId()) {
                    // Repetition (budget type).
                    case R.id.tvBudgetType:
                        String tipoBudget = cursor.getString(columnIndex);

                        if (tipoBudget.equals("una_tantum")) {
                            tipoBudget = tipiBudget[0];
                        } else if (tipoBudget.equals("giornaliero")) {
                            tipoBudget = tipiBudget[1];
                        } else if (tipoBudget.equals("settimanale")) {
                            tipoBudget = tipiBudget[2];
                        } else if (tipoBudget.equals("bisettimanale")) {
                            tipoBudget = tipiBudget[3];
                        } else if (tipoBudget.equals("mensile")) {
                            tipoBudget = tipiBudget[4];
                        } else if (tipoBudget.equals("annuale")) {
                            tipoBudget = tipiBudget[5];
                        }
                        ((TextView) view).setText(tipoBudget);

                        return true;

                    // Tags and icon.
                    case R.id.tvTag:
						TextViewWithBackground tvTag = (TextViewWithBackground) view;

                        String voceGrezza = cursor.getString(columnIndex);
                        if (voceGrezza.endsWith(",")) {
                            voceGrezza = voceGrezza.substring(0, voceGrezza.length() - 1);
                        }

                        // Change the color of the tag's background.
                        int tagColor = TagsColors.getInstance().getRandomColor(cursor.getPosition());
						/*GradientDrawable bgShape = (GradientDrawable) tvTag.getBackground();
                        bgShape.setColor(ContextCompat.getColor(getActivity(), tagColor));*/
						tvTag.setBackgroundColorPreserveBackground(ContextCompat.getColor(getActivity(), tagColor));

                        tvTag.setText(voceGrezza);
                        // Running text.
                        if (voceGrezza.length() > 25) {
                            tvTag.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            tvTag.setSingleLine(true);
                            tvTag.setMarqueeRepeatLimit(5);
                            tvTag.setSelected(true);
                        }

                        //icona
                        ImageView ivIcona = (ImageView) (((View) (view.getParent())).findViewById(R.id.ivIcon));
                        Integer icona = hmIcone.get(voceGrezza);
                        if (icona == null) {
                            ivIcona.setImageBitmap(mPlaceHolderBitmap);
                        } else {
                            iconeVeloci.loadBitmap(icona, ivIcona, mPlaceHolderBitmap, 56, 56);
                        }

                        return true;

                    // Amount saved.
                    case R.id.tvSaved:
						double saved = cursor.getDouble(columnIndex);
						((TextView) view).setText(UtilityVarious.getFormattedAmountBudgetSavings(saved, getActivity()), TextView.BufferType.SPANNABLE);
						if (saved >= 0) {
							((TextView) view).setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_green_dark));
                        } else {
                            ((TextView) view).setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));
                        }

                        //((ProgressBar) ((View) (view.getParent())).findViewById(R.id.fragment_budget_listview_item_pbProgresso)).setMax((int) amount);

                        return true;

                    // Percentage of budget spent and ProgressBar.
                    case R.id.tvPerc:
                        double spent = cursor.getDouble(columnIndex);
                        double budgetAmount = cursor.getDouble(cursor.getColumnIndex("importo_valprin"));
                        int perc = Math.min(((int) ((spent * 100) / budgetAmount)), 100);

                        // Percentage spent.
                        ((TextView) view).setText(getString(R.string.budget_dettaglio_speso) + ": " + perc + "%");

                        // ProgressBar.
                        ProgressBar mProgressBar = (ProgressBar) ((View) (view.getParent())).findViewById(R.id.pbBudget);
                        if (spent >= budgetAmount) {
                            mProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_accent));
                        } else {
                            mProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.progressbar_standard));
                        }
                        mProgressBar.setProgress(perc);

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
			Intent intent = BudgetDetails.makeIntent(getActivity(), arg3);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getActivity().startActivityForResult(intent, ACTIVITY_BUDGET_DETTAGLIOVOCE, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
			} else {
				getActivity().startActivityForResult(intent, ACTIVITY_BUDGET_DETTAGLIOVOCE);
			}
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

	private ListView lvBudget;
	private ImageView ivWallet;
	private TextView tvToccaPiu;
	
	private ListViewIconeVeloce iconeVeloci;
	private Bitmap mPlaceHolderBitmap;
	private HashMap<String,Integer> hmIcone = new HashMap<String,Integer>();
}
