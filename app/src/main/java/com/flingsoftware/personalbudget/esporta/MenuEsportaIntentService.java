package com.flingsoftware.personalbudget.esporta;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.*;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;


/**
 * Exports the database in the chosen format (pdf, csv, xls) in the background using an
 * IntentService.
 * Plays the role of Context in the Strategy Pattern.
 */
public class MenuEsportaIntentService extends IntentService {
	
	// Constants.
	private static final String NOME = "MenuEsportaIntentService";

	// Public interface with constants needed to use this class.
	public interface CostantiIntentService {
		String AZIONE_CSV = "com.flingsoftware.personalbudget.CSV";
		String AZIONE_PDF = "com.flingsoftware.personalbudget.PDF";
		String AZIONE_XLS = "com.flingsoftware.personalbudget.XLS";
		String EXTRA_TABELLA_SPESE = "tabellaSpese";
		String EXTRA_TABELLA_ENTRATE = "tabellaEntrate";
		String EXTRA_INDIRIZZO_EMAIL = "indirizzoEmail";
		String EXTRA_DATA_INIZIO = "dataInizio";
		String EXTRA_DATA_FINE = "dataFine";
		String EXTRA_RISULT_INTENT = "risultIntent";
		String EXTRA_FORMATO_OUTPUT = "formatoOutput";
	}

	/*
		Factory method that creates an Intent to start this IntentService with the proper
		parameters set.
	 */
	public static Intent makeIntent(Context context, String exportFormat, boolean exportExpenses, boolean exportEarnings, long dateStart, long dateEnd, String eMail) {
		Intent intent = new Intent(context, MenuEsportaIntentService.class);
		intent.setAction(exportFormat);
		intent.putExtra(CostantiIntentService.EXTRA_TABELLA_SPESE, exportExpenses);
		intent.putExtra(CostantiIntentService.EXTRA_TABELLA_ENTRATE, exportEarnings);
		intent.putExtra(CostantiIntentService.EXTRA_DATA_INIZIO, dateStart);
		intent.putExtra(CostantiIntentService.EXTRA_DATA_FINE, dateEnd);
		intent.putExtra(CostantiIntentService.EXTRA_INDIRIZZO_EMAIL, eMail);

		return intent;
	}


	public MenuEsportaIntentService() {
		super(NOME);
		setIntentRedelivery(false);
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		boolean risult = false; // success or failure?
		String action = intent.getAction();

		// Details of export operation retrieved from Intent.
		ExportDetails exportDetails = new ExportDetails();
		exportDetails.setExportExpenses(intent.getBooleanExtra(CostantiIntentService.EXTRA_TABELLA_SPESE, true));
		exportDetails.setExportEarnings(intent.getBooleanExtra(CostantiIntentService.EXTRA_TABELLA_ENTRATE, true));
		exportDetails.seteMail(intent.getStringExtra(CostantiIntentService.EXTRA_INDIRIZZO_EMAIL));
		exportDetails.setDateStart(intent.getLongExtra(CostantiIntentService.EXTRA_DATA_INIZIO, 0));
		exportDetails.setDateEnd(intent.getLongExtra(CostantiIntentService.EXTRA_DATA_FINE, 0));

		// Export the database using the chosen ConcreteStrategy.
		ExportStrategy exportStrategy = StrategyFactory.getStrategy(this, action);
		risult = exportStrategy.exportDatabase(exportDetails);

		// Pass the result to the BroadcastReceiver registered in the main Activity.
		Intent broadcastIntent = new Intent(LOCAL_BROADCAST_ACTION);
		broadcastIntent.putExtra(CostantiIntentService.EXTRA_RISULT_INTENT, risult);
		broadcastIntent.putExtra(CostantiIntentService.EXTRA_INDIRIZZO_EMAIL, exportDetails.geteMail());
		broadcastIntent.putExtra(CostantiIntentService.EXTRA_FORMATO_OUTPUT, exportStrategy.getOutputFormat());
		LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.sendBroadcast(broadcastIntent);		
	}
}
