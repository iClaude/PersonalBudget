package com.flingsoftware.personalbudget.database;

import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.*;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


public class AggiornamentoDatabaseIntentService extends IntentService {
	//costanti private
	private static final String NAME = "AggiornamentoDatabaseIntentService";
	
	//costanti pubbliche
	public interface CostantiPubbliche {	
		String ACTION_UPDATE_DATABASE = "com.flingsoftware.personalbudget.UPDATE_DATABASE";
		String EXTRA_RESULT = "result";
	}
	
	
	public AggiornamentoDatabaseIntentService() {
		super(NAME);
		setIntentRedelivery(false);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		
		if(action.equals(CostantiPubbliche.ACTION_UPDATE_DATABASE)) {
			updateDatabase();
		}
	}
	
	/*
	 * Ad ogni lancio dell'app aggiorno il database eseguendo queste operazioni:
	 * - inserimento spese ripetute non ancora inserite dall'ultima volta che è stata lanciata l'app
	 * - inserimento entrate ripetute non ancora inserite dall'ultima volta
	 * - inserimento budget periodici scaduti e cancellazione budget una tantum scaduti	
	 */
	private void updateDatabase() {
		serviceAttivo = true;
		
		FunzioniAggiornamento funzioniAggiornamento = new FunzioniAggiornamento(this);
		int speseAggiunte = funzioniAggiornamento.aggiornaSpeseRipetute();
		int entrateAggiunte = funzioniAggiornamento.aggiornaEntrateRipetute();
		int budgetAggiunti = funzioniAggiornamento.aggiornaTabBudgetNuoviBudget();
		
		serviceAttivo = false;
		
		boolean result = true;
		if(speseAggiunte == -1 || entrateAggiunte == -1 || budgetAggiunti == -1) {
			result = false;
		}
		
		//comunico il risultato alla main activity
		Intent broadcastIntent = new Intent(LOCAL_BROADCAST_UPDATE_DATABASE);
		broadcastIntent.putExtra(CostantiPubbliche.EXTRA_RESULT, result);
		LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.sendBroadcast(broadcastIntent);	
	}
	
	
	//variabili di istanza
	public static boolean serviceAttivo;
}
