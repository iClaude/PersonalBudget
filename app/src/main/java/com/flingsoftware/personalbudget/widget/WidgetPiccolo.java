/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.EntrateAggiungi;
import com.flingsoftware.personalbudget.app.FunzioniComuni;
import com.flingsoftware.personalbudget.app.MainPersonalBudget;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.app.Preferiti;
import com.flingsoftware.personalbudget.app.SpeseAggiungi;
import com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;
import com.flingsoftware.personalbudget.utility.NumberFormatter;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.LOCAL_BROADCAST_UPDATE_VOCI_RIPETUTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;


public class WidgetPiccolo extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		this.mioContext = context;
		
		aggiornaDatabase();
		aggiornaDati();	
		double saldo = entrate - spese + entrateFuture - speseFuture;
		for(int i=0; i<appWidgetIds.length; i++) {
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_small);
            rv.setViewVisibility(R.id.tvLoading, View.INVISIBLE);
            rv.setViewVisibility(R.id.llContent, View.VISIBLE);

            //icone cliccabili sul titolo del widget
            Intent intMain = new Intent(context, MainPersonalBudget.class);
            PendingIntent pintMain = PendingIntent.getActivity(context, 0, intMain, 0);
			rv.setOnClickPendingIntent(R.id.ivLogo, pintMain);
            rv.setOnClickPendingIntent(R.id.llContent, pintMain);

			Intent intSpese = new Intent(context, SpeseAggiungi.class);
            PendingIntent pintSpese = PendingIntent.getActivity(context, 0, intSpese, 0);
			rv.setOnClickPendingIntent(R.id.ivAddExpense, pintSpese);

			Intent intEntrate = new Intent(context, EntrateAggiungi.class);
            PendingIntent pintEntrate = PendingIntent.getActivity(context, 0, intEntrate, 0);
			rv.setOnClickPendingIntent(R.id.ivAddEarning, pintEntrate);

			Intent intPreferiti = new Intent(context, Preferiti.class);
            PendingIntent pintPreferiti = PendingIntent.getActivity(context, 0, intPreferiti, 0);
			rv.setOnClickPendingIntent(R.id.ivFavorite, pintPreferiti);


            // Display data on the widget.
            // Timespan.
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
            rv.setTextViewText(R.id.tvTime, df.format(dataInizio.getTime()) + " - " + df.format(dataFine.getTime()));
            // Earnings and expenses.
            rv.setTextViewText(R.id.tvEarnings, NumberFormatter.formatAmountMainCurrency(earnings, mioContext));
            rv.setTextViewText(R.id.tvExpenses, NumberFormatter.formatAmountMainCurrency(expenses, mioContext));
            // Percentages.
            double percEar = (earnings == 0 && expenses == 0) ? 0.0 : (earnings / (earnings + expenses));
            double percExp = (earnings == 0 && expenses == 0) ? 0.0 : (expenses / (earnings + expenses));
            NumberFormat percForm = NumberFormat.getPercentInstance();
            rv.setTextViewText(R.id.tvPercEar, percForm.format(percEar));
            rv.setTextViewText(R.id.tvPercExp, percForm.format(percExp));
			// Icons for percentages.
			rv.setImageViewResource(R.id.ivImg1, R.drawable.ic_action_news);
			rv.setImageViewResource(R.id.ivImg2, R.drawable.ic_action_news);
			// Balance.
			rv.setTextViewText(R.id.tvBalance, NumberFormatter.formatAmountColorCurrencySuperscript(earnings - expenses, mioContext));


            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	
	@Override
	public void onReceive(Context context, Intent intent) {				
		String action = intent.getAction();
	    if (action.equals(WIDGET_AGGIORNA) || action.equals(WIDGET_PICCOLO_AGGIORNA) || action.equals(Intent.ACTION_DATE_CHANGED)) 
	    {
	        AppWidgetManager gm = AppWidgetManager.getInstance(context);
	        int[] ids = gm.getAppWidgetIds(new ComponentName(context, WidgetPiccolo.class));
	        onUpdate(context, gm, ids);
	        
	    }
	    else 
	    {
	        super.onReceive(context, intent);
	    }
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
			
			//lancio un broadcast di avvenuto aggiornamento del database (utile se l'app � in esecuzione)
			Intent broadcastIntent = new Intent(LOCAL_BROADCAST_UPDATE_VOCI_RIPETUTE);
			LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mioContext);
			localBroadcastManager.sendBroadcast(broadcastIntent);	
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
	
	
	private void aggiornaDati() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mioContext);
		
		//periodo di riferimento come da preferenze
		int tipoDataAutomatica = pref.getInt(CostantiPreferenze.DATA_AUTOMATICA, -1);
		int offset = pref.getInt(CostantiPreferenze.DATA_OFFSET, 1);

		if(tipoDataAutomatica == -1) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);
		
			long dataInizioLong = pref.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
			dataInizio.setTimeInMillis(dataInizioLong);
			
			dataComodo.add(GregorianCalendar.MONTH, 1);
			dataComodo.add(GregorianCalendar.DATE, -1);
			long dataFineLong = pref.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
			dataFine.setTimeInMillis(dataFineLong);
		}
		else {
			FunzioniComuni.impostaPeriodoAutomatico(tipoDataAutomatica, dataInizio, dataFine, offset);
		}
		
		//valuta
		String valuta = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(valuta);
		
		//entrate e spese da database
		String trasf = mioContext.getString(R.string.voce_giroconto);
		DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(mioContext);
		DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(mioContext);
		dbcEntrateIncassate.openLettura();
		dbcSpeseSostenute.openLettura();
		Cursor cursor = null;
		long adesso = System.currentTimeMillis();

		// Entrate e spese passate.
		entrate = 0.0;
		spese = 0.0;
		if(adesso > dataInizio.getTimeInMillis()) {
			long tempoFineMillis = dataFine.getTimeInMillis() > adesso ? adesso : dataFine.getTimeInMillis();

			// Totale entrate.
			cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataInizio.getTimeInMillis(), tempoFineMillis, trasf);
			cursor.moveToFirst();
			entrate = cursor.getDouble(cursor.getColumnIndex("totale_entrata"));

			// Totale Spese.
			cursor = null;
			cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataInizio.getTimeInMillis(), tempoFineMillis, trasf);
			cursor.moveToFirst();
			spese = cursor.getDouble(cursor.getColumnIndex("totale_spesa"));
		}

		// Entrate e spese future.
		entrateFuture = 0.0;
		speseFuture = 0.0;
		if(adesso < dataFine.getTimeInMillis()) {
			long tempoInizioMillis = (adesso < dataFine.getTimeInMillis()) && (adesso > dataInizio.getTimeInMillis()) ? adesso : dataInizio.getTimeInMillis();

			// Entrate future.
			cursor = null;
			cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", tempoInizioMillis, dataFine.getTimeInMillis(), trasf);
			cursor.moveToFirst();
			entrateFuture = cursor.getDouble(cursor.getColumnIndex("totale_entrata"));

			// Spese future.
			cursor = null;
			dbcSpeseSostenute.openLettura();
			cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", tempoInizioMillis, dataFine.getTimeInMillis(), trasf);
			cursor.moveToFirst();
			speseFuture = cursor.getDouble(cursor.getColumnIndex("totale_spesa"));
		}

		dbcEntrateIncassate.close();
		dbcSpeseSostenute.close();

		earnings = entrate + entrateFuture;
		expenses = spese + speseFuture;
	}
	
	
	@Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }
	

    @Override
    public void onDisabled(Context context) 
    {
        super.onDisabled(context);
    }

    
    @Override
    public void onEnabled(Context context) 
    {
        super.onEnabled(context);
    }
    
    
    //variabili di istanza
    private Context mioContext;
  	private GregorianCalendar dataInizio = new GregorianCalendar();
  	private GregorianCalendar dataFine = new GregorianCalendar();
  	private Currency currValuta;
  	private double entrate;
  	private double spese;
	private double entrateFuture;
	private double speseFuture;
	private double earnings;
	private double expenses;
  	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
}

