/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import static com.flingsoftware.personalbudget.app.BudgetDettaglio.CostantiPubbliche.BUDGET_DATA_FINE;
import static com.flingsoftware.personalbudget.app.BudgetDettaglio.CostantiPubbliche.BUDGET_DATA_INIZIO;
import static com.flingsoftware.personalbudget.app.BudgetDettaglio.CostantiPubbliche.BUDGET_VOCE;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_ACCOUNT;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_AMOUNT;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_AMOUNT_CURR;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_CURRENCY;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_DATE;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_DESC;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_FAVORITE;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_ID;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_REP_ID;
import static com.flingsoftware.personalbudget.app.ExpenseEarningDetails.KEY_TAG;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.ACTIVITY_SPESE_DETTAGLIOVOCE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VISUALIZZA_PER_DATA;


public class BudgetSpeseIncluse extends ActionBarActivity implements OnQueryTextListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.budget_speseincluse);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		//gestione efficiente icone
		iconeVeloci = new ListViewIconeVeloce(this);
		new PlaceHolderWorkerTask().execute(R.drawable.tag_0);
		new CaricaHashMapIconeTask().execute((Object[]) null);
		
		ricavaValuta();
		
		//recupero i valori passati dall'Activity chiamante
		Bundle extras = getIntent().getExtras();
		voceBudget = extras.getString(BUDGET_VOCE);
		dataInizioBudgetLong = extras.getLong(BUDGET_DATA_INIZIO);
		dataFineBudgetLong = extras.getLong(BUDGET_DATA_FINE);
		
		//aggiorno la TextView del periodo
		DateFormat df = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
		((TextView) findViewById(R.id.budget_speseincluse_tvPeriodo)).setText(df.format(new Date(dataInizioBudgetLong)) + " - " + df.format(new Date(dataFineBudgetLong)));
		
		//ricavo le varie voci di spesa del budget e le metto in un ArrayList
		vociBudget = new ArrayList<String>();
		ricavaVociBudget();
		impostaQuery();
		
		dbcSpeseSostenute = new DBCSpeseSostenute(this);
		tipoVisualizzazione = VISUALIZZA_PER_DATA;
		
		//popolo la listview con i dati contenuti nel database in un thread separato
		new RefreshGroupsCursorTask().execute((Object[]) null);
		
		elv = (ExpandableListView) findViewById(R.id.budget_speseincluse_elvSpese);
		
		//adapter per personalizzare il formato dei dati visualizzati sulla elv. NB: adapter per la visualizzazione per data
		mAdapter = new MyExpandableListAdapter(null, this, R.layout.fragment_spese_entrate_group_item, R.layout.fragment_spese_entrate_child_item, new String[] {"data", "totale_spesa"}, new int[] {R.id.tvVoceGruppo, R.id.fragment_spese_tvImportoGruppo}, new String[] {"voce", "ripetizione_id", "importo_valprin"}, new int[] {R.id.tvVoceChild, R.id.fragment_spese_entrate_child_item_tvRipetizione, R.id.tvImportoChild});
		mAdapter.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue (View view, final Cursor cursor, int columnIndex) {
				int viewId = view.getId();
				
				NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
				DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
				
				switch(viewId) {
				case R.id.tvVoceGruppo:
					long dataDatabaseMillis = cursor.getLong(columnIndex);
					GregorianCalendar oggi = new GregorianCalendar();
					int giorno = oggi.get(GregorianCalendar.DATE);
					int mese = oggi.get(GregorianCalendar.MONTH);
					int anno = oggi.get(GregorianCalendar.YEAR);
					oggi = new GregorianCalendar(anno, mese, giorno);
					
					//imposto la label con la data
					SimpleDateFormat mioSdf = new SimpleDateFormat("dd", miaLocale);
                    TextView tvGiorno = (TextView) ((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_tvGiorno);
					tvGiorno.setText(mioSdf.format(new Date(dataDatabaseMillis)));

                    // Date future con label grigia.
                    if(dataDatabaseMillis > oggi.getTimeInMillis()) {
                        tvGiorno.setBackgroundResource(R.drawable.background_tondo_grigio);
                    }
                    else {
                        tvGiorno.setBackgroundResource(R.drawable.background_tondo_primary);
                    }

					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_oggi));
						return true;
					}
					
					oggi.add(GregorianCalendar.DATE, -1);
					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_ieri));
						return true;
					}
					
					mioSdf = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
					((TextView) view).setText(mioSdf.format(new Date(dataDatabaseMillis)));
					//testo scorrevole
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					return true;
				case R.id.fragment_spese_tvImportoGruppo:
					double importoGruppo = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoGruppoFormattato = nf.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
                        ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.GONE);
					}
					else {
						String importoGruppoFormattato = nfRidotto.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
                        ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.VISIBLE);
						((TextView) ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta)).setText("(" + currValutaPrinc.getSymbol() + ")");
					}
					
					return true;
				case R.id.fragment_spese_entrate_child_item_tvRipetizione:
					long ripetizioneId = cursor.getLong(cursor.getColumnIndex("ripetizione_id"));
					if(ripetizioneId != 1) {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.VISIBLE);
					}
					else {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.GONE);
					}
					
					return true;
				case R.id.tvImportoChild:
					double importoChild = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoChildFormattato = nf.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					else {
						String importoChildFormattato = nfRidotto.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					
					return true;	
				case R.id.tvVoceChild:
					String voceChild = cursor.getString(columnIndex);
					
					//testo scorrevole su voci child	
					((TextView) view).setText(voceChild);
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					//icona
					ImageView ivIcona = (ImageView) (((View) (view.getParent())).findViewById(R.id.menu_esporta_ivFormato));
					Integer icona = hmIcone.get(voceChild);
					if(icona == null) {
						ivIcona.setImageBitmap(mPlaceHolderBitmapSpese);
					}
					else {
						iconeVeloci.loadBitmap(icona, ivIcona, mPlaceHolderBitmapSpese, 40, 40);
					}
					
					return true;
				}
				return false;
			}
		});
		elv.setAdapter(mAdapter);
		
		//adapter per personalizzare il formato dei dati visualizzati sulla elv. NB: adapter per la visualizzazione per voci. Lo creo e poi lo associo alla elv alla bisogna
		mAdapterVoci = new MyExpandableListAdapter(null, this, R.layout.fragment_spese_entrate_group_item, R.layout.fragment_spese_entrate_child_item, new String[] {"voce", "totale_spesa"}, new int[] {R.id.tvVoceGruppo, R.id.fragment_spese_tvImportoGruppo}, new String[] {"data", "ripetizione_id", "importo_valprin", "voce"}, new int[] {R.id.tvVoceChild, R.id.fragment_spese_entrate_child_item_tvRipetizione, R.id.tvImportoChild, R.id.fragment_spese_entrate_child_item_tvVoce});
		mAdapterVoci.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue (View view, final Cursor cursor, int columnIndex) {
				int viewId = view.getId();
				
				NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
				DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
				
				switch(viewId) {
				case R.id.tvVoceChild:
					long dataDatabaseMillis = cursor.getLong(columnIndex);
					GregorianCalendar oggi = new GregorianCalendar();
					int giorno = oggi.get(GregorianCalendar.DATE);
					int mese = oggi.get(GregorianCalendar.MONTH);
					int anno = oggi.get(GregorianCalendar.YEAR);
					oggi = new GregorianCalendar(anno, mese, giorno);
					
					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_oggi));
						return true;
					}
					
					oggi.add(GregorianCalendar.DATE, -1);
					if(oggi.getTimeInMillis() == dataDatabaseMillis) {
						((TextView) view).setText(getResources().getString(R.string.generale_ieri));
						return true;
					}
					
					SimpleDateFormat mioSdf = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
					((TextView) view).setText(mioSdf.format(new Date(dataDatabaseMillis)));
					//testo scorrevole
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					return true;
				case R.id.fragment_spese_tvImportoGruppo:
					double importoGruppo = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoGruppoFormattato = nf.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
                        ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.GONE);
					}
					else {
						String importoGruppoFormattato = nfRidotto.format(importoGruppo);
						((TextView) view).setText(importoGruppoFormattato);
                        ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta).setVisibility(View.VISIBLE);
						((TextView) ((LinearLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_group_item_tvValuta)).setText("(" + currValutaPrinc.getSymbol() + ")");
					}
					
					return true;
				case R.id.fragment_spese_entrate_child_item_tvRipetizione:
					long ripetizioneId = cursor.getLong(cursor.getColumnIndex("ripetizione_id"));
					if(ripetizioneId != 1) {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.VISIBLE);
					}
					else {
						((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_entrate_child_item_ivRipetizione).setVisibility(View.GONE);
					}
					
					return true;
				case R.id.tvImportoChild:
					double importoChild = cursor.getDouble(columnIndex);
					if(!valutaAlternativa) {
						String importoChildFormattato = nf.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					else {
						String importoChildFormattato = nfRidotto.format(importoChild);
						((TextView) view).setText(importoChildFormattato);
					}
					
					return true;	
				case R.id.tvVoceGruppo:
					//imposto la label
					String voceItem = cursor.getString(columnIndex);
					
					//testo scorrevole
					((TextView) view).setText(voceItem);
					((TextView) view).setEllipsize(TextUtils.TruncateAt.MARQUEE);
					((TextView) view).setSingleLine(true);
					((TextView) view).setMarqueeRepeatLimit(5);
					view.setSelected(true);
					
					((TextView) ((RelativeLayout) view.getParent()).findViewById(R.id.fragment_spese_tvGiorno)).setText(voceItem.substring(0,1).toUpperCase(Locale.getDefault()));
				
					return true;
				case R.id.fragment_spese_entrate_child_item_tvVoce:
					//icona
					String voce = cursor.getString(columnIndex);
					ImageView ivIcona = (ImageView) (((View) (view.getParent())).findViewById(R.id.menu_esporta_ivFormato));
					Integer icona = hmIcone.get(voce);
					if(icona == null) {
						ivIcona.setImageBitmap(mPlaceHolderBitmapSpese);
					}
					else {
						iconeVeloci.loadBitmap(icona, ivIcona, mPlaceHolderBitmapSpese, 40, 40);
					}
					
					return true;
				}
				return false;
			}
		});

		//listener per i click sugli elementi (visualizzazione delle voci cliccate)
		elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {		
				dbcSpeseSostenute.openLettura();
				Cursor cDettaglioVoce = dbcSpeseSostenute.getSpesaSostenutaX(id);
				cDettaglioVoce.moveToFirst();
				
				String valuta = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("valuta"));
				double importoValprin = cDettaglioVoce.getDouble(cDettaglioVoce.getColumnIndex("importo_valprin"));
				double importo = cDettaglioVoce.getDouble(cDettaglioVoce.getColumnIndex("importo"));
				String voce = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("voce"));
				long data = cDettaglioVoce.getLong(cDettaglioVoce.getColumnIndex("data"));
				String descrizione = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("descrizione"));
				long ripetizione_id = cDettaglioVoce.getLong(cDettaglioVoce.getColumnIndex("ripetizione_id"));
				String conto = cDettaglioVoce.getString(cDettaglioVoce.getColumnIndex("conto"));
				int preferito = cDettaglioVoce.getInt(cDettaglioVoce.getColumnIndex("favorite"));
				
				cDettaglioVoce.close();
				dbcSpeseSostenute.close();
				Intent visualizzaVoceIntent = new Intent(BudgetSpeseIncluse.this, SpeseDettaglioVoce.class);
				visualizzaVoceIntent.putExtra(KEY_ID, id);
				visualizzaVoceIntent.putExtra(KEY_AMOUNT, importo);
				visualizzaVoceIntent.putExtra(KEY_TAG, voce);
				visualizzaVoceIntent.putExtra(KEY_DATE, data);
				visualizzaVoceIntent.putExtra(KEY_DESC, descrizione);
				visualizzaVoceIntent.putExtra(KEY_REP_ID, ripetizione_id);
				visualizzaVoceIntent.putExtra(KEY_CURRENCY, valuta);
				visualizzaVoceIntent.putExtra(KEY_AMOUNT_CURR, importoValprin);
				visualizzaVoceIntent.putExtra(KEY_ACCOUNT, conto);
				visualizzaVoceIntent.putExtra(KEY_FAVORITE, preferito);
				
				startActivityForResult(visualizzaVoceIntent, ACTIVITY_SPESE_DETTAGLIOVOCE);
				
				return true;
			}
		});
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	// ricavo la valuta principale dal file delle preferenze
	private void ricavaValuta() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String valuta = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValutaPrinc = Currency.getInstance(valuta);
		valutaAlternativa = !currValutaPrinc.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}
	
	
	//ricavo le voci di spesa comprese in questo budget e le metto nell'ArrayList vociBudget
	private void ricavaVociBudget() {
		StringTokenizer st = new StringTokenizer(voceBudget, ",");
		while(st.hasMoreTokens()) {
			vociBudget.add(st.nextToken());
		}
	}
	
	//costruisco la query di ricerca per i gruppi della ExpandableListview
	private void impostaQuery() {
		//creo le query in SQL
		queryData = new StringBuilder("SELECT _id, data, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND (");
		queryVoce = new StringBuilder("SELECT _id, voce, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= ? AND data <= ? AND (");
		queryDataChild = new StringBuilder("SELECT _id, data, voce, importo_valprin, ripetizione_id FROM spese_sost WHERE data >= ? AND data <= ? AND (");
		int size = vociBudget.size();
		if(size == 1) {
			queryData.append("voce = ?) AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY data ORDER BY data DESC");
			queryDataChild.append("voce = ?) AND (voce LIKE ? OR descrizione LIKE ?) ORDER BY data DESC");
			queryVoce.append("voce = ?) AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY voce ORDER BY voce ASC");
		}
		else {
			queryData.append("voce = ?");
			queryDataChild.append("voce = ?");
			queryVoce.append("voce = ?");
			for(int i=0; i<(size-1); i++) {
				queryData.append(" OR voce = ?");
				queryDataChild.append(" OR voce = ?");
				queryVoce.append(" OR voce = ?");
			}
			queryData.append(") AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY data ORDER BY data DESC");
			queryDataChild.append(") AND (voce LIKE ? OR descrizione LIKE ?) ORDER BY data DESC");
			queryVoce.append(") AND (voce LIKE ? OR descrizione LIKE ?) GROUP BY voce ORDER BY voce ASC");
		}
		
		//creo l'array di String di argomenti delle query precedente
		int numArgs = 2 + size + 2;
		args = new String[numArgs];
		argsChild = new String[numArgs];
		args[0] = Long.valueOf(dataInizioBudgetLong).toString();
		args[1] = Long.valueOf(dataFineBudgetLong).toString();
		for(int i=2; i<(numArgs-2); i++) {
			args[i] = vociBudget.get(i-2);
			argsChild[i] = vociBudget.get(i-2);
		}
		//aggiungo argomenti per funzioni di ricerca
		args[numArgs-2] = "%" + ricerca.toString() + "%";
		args[numArgs-1] = "%" + ricerca.toString() + "%";
		argsChild[numArgs-2] = "%" + ricerca.toString() + "%";
		argsChild[numArgs-1] = "%" + ricerca.toString() + "%";		
	}

	
	//adapter del database per l'expandablelistview
	public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {
		
		public MyExpandableListAdapter(Cursor cursor, Context context, int groupLayout, int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom, int[] childrenTo) {
			super (context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom, childrenTo);
		}
		
		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
				long data = groupCursor.getLong(groupCursor.getColumnIndex("data"));
				new RefreshChildrenCursorTask(groupCursor.getPosition(), null).execute(data);
			}
			else {
				String mVoce = groupCursor.getString(groupCursor.getColumnIndex("voce"));
				new RefreshChildrenCursorTask(groupCursor.getPosition(), mVoce).execute(0L);
			}
			
			return null;
		}
	}
	
	//AsyncTask per caricare i totali per data (group items) in un thread separato
	private class RefreshGroupsCursorTask extends AsyncTask<Object, Void, Cursor> {
		
		protected Cursor doInBackground(Object... params) {	
			dbcSpeseSostenute.openLettura();
			
			if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
				groupCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpeseXYZTotaliPerDataOVoce(queryData.toString(), args);
			}
			else {
				groupCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpeseXYZTotaliPerDataOVoce(queryVoce.toString(), args);
			}
			groupCursor.moveToFirst();
			
			return groupCursor;
		}
		
		protected void onPostExecute(Cursor groupCursor) {
			try {
				if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
					mAdapter.changeCursor(groupCursor);
				}
				else {
					mAdapterVoci.changeCursor(groupCursor);
				}
				elv.expandGroup(0);
			}
			catch(NullPointerException exc) {
				return;
			}
		}
	}
	
	//AsyncTask per caricare il dettaglio delle spese (child items) in un thread separato
	private class RefreshChildrenCursorTask extends AsyncTask<Long, Void, Cursor> {
		private int mGroupPosition;
		private String mVoce;
		
		public RefreshChildrenCursorTask(int groupPosition, String mVoce) {
			this.mGroupPosition = groupPosition;
			this.mVoce = mVoce;
		}
		
		@Override
		protected Cursor doInBackground(Long... params) {
			dbcSpeseSostenute.openLettura();
			
			if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
				argsChild[0] = params[0].toString();
				argsChild[1] = params[0].toString();
				childCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpeseXYZ(queryDataChild.toString(), argsChild);
			}
			else {
				childCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXFiltrato("%", dataInizioBudgetLong, dataFineBudgetLong, mVoce, ricerca.toString()); //utilizzo tutti i conti
			}
			childCursor.moveToFirst();
			
			return childCursor;
		}
		
		@Override
		protected void onPostExecute(Cursor childrenCursor) {
			try {
				if(tipoVisualizzazione == VISUALIZZA_PER_DATA) {
					mAdapter.setChildrenCursor(mGroupPosition, childrenCursor);
				}
				else {
					mAdapterVoci.setChildrenCursor(mGroupPosition, childrenCursor);
				}
				//dbcSpeseSostenute.close();
			}
			catch(NullPointerException exc) {
				return;
			}
		}
	}
	
	
	//recupero icona placeholder della listview
	private class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {   
	    // Decode image in background.
	    @Override
	    protected Object doInBackground(Integer... params) {
	    	mPlaceHolderBitmapSpese = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 40, 40);
	    	return null;
	    }
	}

	
	//recupero hashmap icone
	private class CaricaHashMapIconeTask extends AsyncTask<Object, Void, Object> {   
	    @Override
	    protected Object doInBackground(Object... params) {
			//recupero info su icone voci
	    	DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(BudgetSpeseIncluse.this);
	    	dbcSpeseVoci.openLettura();
	    	Cursor curVoci = dbcSpeseVoci.getTutteLeVoci();
	    	while(curVoci.moveToNext()) {
	    		String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
	    		int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
	    		hmIcone.put(voce, icona);
	    	}
	    	curVoci.close();
	    	dbcSpeseVoci.close();
	    	return null;
	    }
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,  resultCode, data);
		
		switch(requestCode) {

		case ACTIVITY_SPESE_DETTAGLIOVOCE:
			if(resultCode == Activity.RESULT_OK) {
				modifiche = true;
				aggiornaCursor();
			}
			break;
		}
	}
	
	private void aggiornaCursor() {
		new CaricaHashMapIconeTask().execute((Object[]) null);
		new RefreshGroupsCursorTask().execute((Object[]) null);
	}
	
	
	/* Metodo chiamato quando imposto una stringa di ricerca per filtrare l'elenco delle spese.
	 */
	private void impostaRicerca(String str) {
		ricerca.delete(0, ricerca.length());
		ricerca.append(str);
		
		//aggiungo argomenti per funzioni di ricerca
		int lung = args.length;
		args[lung-2] = "%" + ricerca.toString() + "%";
		args[lung-1] = "%" + ricerca.toString() + "%";	
		argsChild[lung-2] = "%" + ricerca.toString() + "%";
		argsChild[lung-1] = "%" + ricerca.toString() + "%";		
		
		aggiornaCursor();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_budgetspeseincluse, menu);
		
		//SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_ricerca));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.menu_ricerca));
        searchView.setOnQueryTextListener(this);

		return true;
	}


    /*
   Implementazione interfaccia android.support.v7.widget.SearchView.OnQueryTextListener per intercettare
   eventi riguardanti la SearchView.
    */
    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        impostaRicerca(newText);
        // da fare

        return false;
    }


    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_budgetSpeseIncluse_visualizzaPerVoce:
			if(tipoVisualizzazione == CostantiPreferenze.VISUALIZZA_PER_DATA) {
				tipoVisualizzazione = CostantiPreferenze.VISUALIZZA_PER_VOCE;
				elv.setAdapter(mAdapterVoci);
			}
			else {
				tipoVisualizzazione = CostantiPreferenze.VISUALIZZA_PER_DATA;
				elv.setAdapter(mAdapter);
			}
			
			aggiornaCursor();

			return true;	
			
		case android.R.id.home:
			finish();
	        
	        return true;
			
		default:
			return super.onOptionsItemSelected(item);
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
	protected void onDestroy() {
		super.onDestroy();
	}



	//variabili di istanza
	private int tipoVisualizzazione;
	private long dataInizioBudgetLong;
	private long dataFineBudgetLong;
	private String voceBudget;
	private ArrayList<String> vociBudget;
	private DBCSpeseSostenute dbcSpeseSostenute;
	private Cursor groupCursor;
	private Cursor childCursor;
	private StringBuilder queryData; //query di ricerca per i gruppi (totali per data)
	private StringBuilder queryVoce; //query di ricerca per i gruppi (totali per voce)
	private String args[]; //argomenti da usare nella query prec.
	private StringBuilder queryDataChild; //query di ricerca per i gruppi
	private String argsChild[]; //argomenti da usare nella query precedente
	private MyExpandableListAdapter mAdapter;
	private MyExpandableListAdapter mAdapterVoci;
	private boolean modifiche; // ho modificato qualche spesa?
	private Currency currValutaPrinc;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private boolean valutaAlternativa;
	
	//gestione suoni
	private SoundEffectsManager soundEffectsManager = SoundEffectsManager.getInstance();
	
	//gestione ricerca
	private StringBuilder ricerca = new StringBuilder(); //filtro per le ricerche
	
	//gestione efficiente icone
	private Bitmap mPlaceHolderBitmapSpese;
	private ListViewIconeVeloce iconeVeloci;
	private HashMap<String,Integer> hmIcone = new HashMap<String,Integer>();
	
	private ExpandableListView elv;
}
