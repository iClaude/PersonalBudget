package com.flingsoftware.personalbudget.widget;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.FunzioniComuni;
import com.flingsoftware.personalbudget.app.MainPersonalBudget;
import com.flingsoftware.personalbudget.app.Preferiti;
import com.flingsoftware.personalbudget.app.SpeseAggiungi;
import com.flingsoftware.personalbudget.app.EntrateAggiungi;
import com.flingsoftware.personalbudget.app.BudgetAggiungi;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.database.AggiornamentoDatabaseIntentService;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.LOCAL_BROADCAST_UPDATE_VOCI_RIPETUTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;
import android.content.ComponentName;
import android.database.Cursor;
import android.graphics.Color;


public class WidgetPiccolo extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {	
		this.mioContext = context;
		
		aggiornaDatabase();
		aggiornaDati();	
		double saldo = entrate - spese + entrateFuture - speseFuture;
		for(int i=0; i<appWidgetIds.length; i++) {		
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_piccolo);
            
            //icone cliccabili sul titolo del widget
            Intent intMain = new Intent(context, MainPersonalBudget.class);
            PendingIntent pintMain = PendingIntent.getActivity(context, 0, intMain, 0);
            rv.setOnClickPendingIntent(R.id.widget_piccolo_ivLogo, pintMain);
            rv.setOnClickPendingIntent(R.id.widget_piccolo_rlContenuto, pintMain);
            
            Intent intSpese = new Intent(context, SpeseAggiungi.class);
            PendingIntent pintSpese = PendingIntent.getActivity(context, 0, intSpese, 0);
            rv.setOnClickPendingIntent(R.id.widget_piccolo_ivAggiungiSpesa, pintSpese);
            
            Intent intEntrate = new Intent(context, EntrateAggiungi.class);
            PendingIntent pintEntrate = PendingIntent.getActivity(context, 0, intEntrate, 0);
            rv.setOnClickPendingIntent(R.id.widget_piccolo_ivAggiungiEntrata, pintEntrate);
            
            Intent intPreferiti = new Intent(context, Preferiti.class);
            PendingIntent pintPreferiti = PendingIntent.getActivity(context, 0, intPreferiti, 0);
            rv.setOnClickPendingIntent(R.id.ivPreferiti, pintPreferiti);
            
            //scrivo i dati sul widget
            rv.setViewVisibility(R.id.widget_piccolo_tvCaricamento, View.INVISIBLE);
            rv.setViewVisibility(R.id.widget_piccolo_rlContenuto, View.VISIBLE);
            if(saldo <= 0) {
            	rv.setImageViewResource(R.id.widget_piccolo_iv_Maialino, R.drawable.img_maiale_rosso);
            	rv.setTextColor(R.id.widget_piccolo_tvSaldo, Color.RED);
            }
            else {
            	rv.setImageViewResource(R.id.widget_piccolo_iv_Maialino, R.drawable.img_maiale_verde);
            	rv.setTextColor(R.id.widget_piccolo_tvSaldo, Color.argb(255, 0, 128, 0));
            }
            DateFormat df =  new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
            rv.setTextViewText(R.id.widget_piccolo_tvPeriodo, df.format(dataInizio.getTime()) + " - " + df.format(dataFine.getTime()));
			NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
            NumberFormat nfSaldo = NumberFormat.getInstance(Locale.getDefault());
			nf.setCurrency(currValuta);	
			rv.setTextViewText(R.id.widget_piccolo_tvEntrate, mioContext.getResources().getString(R.string.preferenze_voci_EAR) + " " + nf.format(entrate));
			rv.setTextViewText(R.id.widget_piccolo_tvSpese, mioContext.getResources().getString(R.string.preferenze_voci_EXP) + " " + nf.format(spese));
			rv.setTextViewText(R.id.widget_piccolo_tvEntrateFuture, mioContext.getResources().getString(R.string.widget_piccolo_stimato) + " " + nf.format(entrateFuture));
			rv.setTextViewText(R.id.widget_piccolo_tvSpeseFuture, mioContext.getResources().getString(R.string.widget_piccolo_stimato) + " " + nf.format(speseFuture));
			rv.setTextViewText(R.id.widget_piccolo_tvSaldo, nfSaldo.format(saldo));
            
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
  	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
}

