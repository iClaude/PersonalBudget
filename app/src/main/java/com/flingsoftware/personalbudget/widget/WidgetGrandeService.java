package com.flingsoftware.personalbudget.widget;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;

import com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.FunzioniComuni;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class WidgetGrandeService extends RemoteViewsService {
	@Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetGrandeRemoteViewsFactory(this.getApplicationContext(), intent);
    }
	
	
	class WidgetGrandeRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

		public WidgetGrandeRemoteViewsFactory(Context context, Intent intent) {
		    this.mioContext = context;
		    lstBudgetAperti = new ArrayList<BudgetAperto>();
		}
		
		
		@Override
		public void onCreate() {
			
		}
		

		private void aggiornaDatabase() {
			if(!AggiornamentoDatabaseIntentService.serviceAttivo) { //controllo che l'aggiornamento non sia gi� in corso (es. quando lancio l'app, la chiudo subito e la riapro)
				//impostazioni iniziali del database
				if(!databaseImpostato()) {
					return;
				}
				
				FunzioniAggiornamento funzioniAggiornamento = new FunzioniAggiornamento(mioContext);
				funzioniAggiornamento.aggiornaSpeseRipetute();
				funzioniAggiornamento.aggiornaEntrateRipetute();
				funzioniAggiornamento.aggiornaTabBudgetNuoviBudget();
			}
		}
		
		
		/*
		 * Restituisce true se il database � impostato con i valori iniziali (l'app � gi� stata avviata
		 * una volta), altrimenti false.
		 */
		private boolean databaseImpostato() {
			boolean risultato = false;
			
			DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(mioContext);
			dbcSpeseVoci.openLettura();
			Cursor cVoce = dbcSpeseVoci.getVoceSpesa(1);
			if(cVoce.getCount() > 0) {
				risultato = true;
			}
			cVoce.close();
			dbcSpeseVoci.close();
			
			return risultato;
		}
		
		
		private void updateWidgetListView()
		{
			//recupero info su icone voci
	    	DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(mioContext);
	    	dbcSpeseVoci.openLettura();
	    	Cursor curVoci = dbcSpeseVoci.getTutteLeVoci();
	    	while(curVoci.moveToNext()) {
	    		String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
	    		int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
	    		hmIcone.put(voce, icona);
	    	}
	    	curVoci.close();
	    	dbcSpeseVoci.close();
	    	
	    	//aggiornamento info sui budget
			lstBudgetAperti.clear();
			tipiBudget = mioContext.getResources().getStringArray(R.array.ripetizioni_budget);
			
			//dati ricavati dal database
			DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(mioContext);
			dbcSpeseBudget.openLettura();
			Cursor curBudgetAperti = dbcSpeseBudget.getSpeseBudgetElencoNonScaduti(FunzioniComuni.getDataAttuale());
			while(curBudgetAperti.moveToNext()) {
				BudgetAperto budgetAperto = new BudgetAperto();
				
				String voceGrezza = curBudgetAperti.getString(curBudgetAperti.getColumnIndex("voce_budget"));
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
				
				budgetAperto.setBudgetId(curBudgetAperti.getLong(curBudgetAperti.getColumnIndex("_id")));
				budgetAperto.setRipetizione(tipoBudget);
				budgetAperto.setVoce(vociInteressate);
				budgetAperto.setImportoValprin(curBudgetAperti.getDouble(curBudgetAperti.getColumnIndex("importo_valprin")));
				budgetAperto.setSpesaSost(curBudgetAperti.getDouble(curBudgetAperti.getColumnIndex("spesa_sost")));
				lstBudgetAperti.add(budgetAperto);
			}
			curBudgetAperti.close();
			dbcSpeseBudget.close();
			
			//altri dati
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mioContext);
			String valuta = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			Currency currValuta = Currency.getInstance(valuta);
			nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
			nf.setCurrency(currValuta);	
		}

		
		@Override
		public int getCount()
		{
		    return lstBudgetAperti.size();
		}
		

		@Override
		public long getItemId(int position)
		{
		    return position;
		}

		
		@Override
		public RemoteViews getLoadingView()
		{
			return null;
		}

		
		@Override
		public RemoteViews getViewAt(int position)
		{
			String ripetizione = lstBudgetAperti.get(position).getRipetizione();
			String voce = lstBudgetAperti.get(position).getVoce();
			double importoValPrin = lstBudgetAperti.get(position).getImportoValprin();
			double spesaSost = lstBudgetAperti.get(position).getSpesaSost();

			RemoteViews remoteView = new RemoteViews(mioContext.getPackageName(), R.layout.fragment_budget_listview_item2);
		    remoteView.setTextViewText(R.id.fragment_budget_listview_item_tvTipoBudget2, ripetizione);
		    remoteView.setTextViewText(R.id.fragment_budget_listview_item_tvVoci2, voce);
		    remoteView.setBoolean(R.id.fragment_budget_listview_item_tvVoci2, "setSingleLine", true);
		    if(spesaSost<importoValPrin) {
		    	remoteView.setViewVisibility(R.id.fragment_budget_listview_item_pbProgressoFull2, View.INVISIBLE);
                remoteView.setViewVisibility(R.id.fragment_budget_listview_item_pbProgresso2, View.VISIBLE);
                remoteView.setProgressBar(R.id.fragment_budget_listview_item_pbProgresso2, (int) importoValPrin, (int) spesaSost, false);
		    }
		    else {
                remoteView.setViewVisibility(R.id.fragment_budget_listview_item_pbProgressoFull2, View.VISIBLE);
                remoteView.setViewVisibility(R.id.fragment_budget_listview_item_pbProgresso2, View.INVISIBLE);
		    }
		    remoteView.setTextViewText(R.id.fragment_budget_listview_item_tvImportoBudget2, nf.format(importoValPrin));

		   
		    Integer icona = hmIcone.get(voce);
		    Bitmap miaBitmap;
		    if(icona != null) {
		    	miaBitmap = decodeSampledBitmapFromResource(mioContext.getResources(), arrIconeId[icona], 50 , 50);
		    }
		    else {
		    	miaBitmap = decodeSampledBitmapFromResource(mioContext.getResources(), R.drawable.img_budget, 50 , 50);
		    }
		    remoteView.setImageViewBitmap(R.id.menu_esporta_ivFormato2, miaBitmap);
		    
		    
		    //singoli elementi della ListView cliccabili
		    Bundle extras = new Bundle();
		    extras.putLong(WidgetGrande.EXTRA_ITEM, lstBudgetAperti.get(position).getBudgetId());
		    Intent fillInIntent = new Intent();
		    fillInIntent.putExtras(extras);
		    remoteView.setOnClickFillInIntent(R.id.fragment_budget_listview_item_rlPadre2, fillInIntent);
		    
		    return remoteView;
		}
		
		
		public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeResource(res, resId, options);

		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeResource(res, resId, options);
		}
		
		
		private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		    // Raw height and width of image
		    final int height = options.outHeight;
		    final int width = options.outWidth;
		    int inSampleSize = 1;
		
		    if (height > reqHeight || width > reqWidth) {
		
		        final int halfHeight = height / 2;
		        final int halfWidth = width / 2;
		
		        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
		        // height and width larger than the requested height and width.
		        while ((halfHeight / inSampleSize) > reqHeight
		                && (halfWidth / inSampleSize) > reqWidth) {
		            inSampleSize *= 2;
		        }
		    }
		
		    return inSampleSize;
		}
		

		@Override
		public int getViewTypeCount()
		{
		    return 1;
		}

		
		@Override
		public boolean hasStableIds()
		{
		    return false;
		}

		
		@Override
		public void onDataSetChanged()
		{
			aggiornaDatabase();
		    updateWidgetListView();
		}

		
		@Override
		public void onDestroy()
		{
		    lstBudgetAperti.clear();
		}
		
		
		class BudgetAperto {
			
			BudgetAperto() {
				
			}
			
			
			BudgetAperto(long budgetId, String ripetizione, String voce, double importoValprin, double spesaSost) {
				this.budgetId = budgetId;
				this.ripetizione = ripetizione;
				this.voce = voce;
				this.importoValprin = importoValprin;
				this.spesaSost = spesaSost;
			}
			
			
			public void setBudgetId(long budgetId) {
				this.budgetId = budgetId;
			}
			
			
			public long getBudgetId() {
				return this.budgetId;
			}
			
			
			public void setRipetizione(String ripetizione) {
				this.ripetizione = ripetizione;		
			}
			
			
			public String getRipetizione() {
				return this.ripetizione;
			}
			
			
			public void setVoce(String voce) {
				this.voce = voce;
			}
			
			
			public String getVoce() {
				return this.voce;
			}
			
			
			public void setImportoValprin(double importoValprin) {
				this.importoValprin = importoValprin;
			}
			
			
			public double getImportoValprin() {
				return this.importoValprin;
			}
			
			
			public void setSpesaSost(double spesaSost) {
				this.spesaSost = spesaSost;
			}
			
			
			public double getSpesaSost() {
				return this.spesaSost;
			}
			
			//variabili di istanza
			private long budgetId;
			private String ripetizione;
			private String voce;
			private double importoValprin;
			private double spesaSost;
		}
			
		
		//variabili di istanza
		private Context mioContext;
		private List<BudgetAperto> lstBudgetAperti;
		private NumberFormat nf;
		private String[] tipiBudget;
		
		private HashMap<String,Integer> hmIcone = new HashMap<String,Integer>();
		private final Integer[] arrIconeId = new Integer[] {R.drawable.tag_0, R.drawable.tag_1, R.drawable.tag_2, R.drawable.tag_3, R.drawable.tag_4, R.drawable.tag_5, R.drawable.tag_6, R.drawable.tag_7, R.drawable.tag_8, R.drawable.tag_9,
			R.drawable.tag_10, R.drawable.tag_11, R.drawable.tag_12, R.drawable.tag_13, R.drawable.tag_14, R.drawable.tag_15, R.drawable.tag_16, R.drawable.tag_17, R.drawable.tag_18, R.drawable.tag_19,
			R.drawable.tag_20, R.drawable.tag_21, R.drawable.tag_22, R.drawable.tag_23, R.drawable.tag_24, R.drawable.tag_25, R.drawable.tag_26, R.drawable.tag_27, R.drawable.tag_28, R.drawable.tag_29,
			R.drawable.tag_30, R.drawable.tag_31, R.drawable.tag_32, R.drawable.tag_33, R.drawable.tag_34, R.drawable.tag_35, R.drawable.tag_36, R.drawable.tag_37, R.drawable.tag_38, R.drawable.tag_39,
			R.drawable.tag_40, R.drawable.tag_41, R.drawable.tag_42, R.drawable.tag_43, R.drawable.tag_44, R.drawable.tag_45, R.drawable.tag_46, R.drawable.tag_47, R.drawable.tag_48, R.drawable.tag_49,
			R.drawable.tag_50, R.drawable.tag_51, R.drawable.tag_52, R.drawable.tag_53, R.drawable.tag_54, R.drawable.tag_55, R.drawable.tag_56, R.drawable.tag_57};
	}
}