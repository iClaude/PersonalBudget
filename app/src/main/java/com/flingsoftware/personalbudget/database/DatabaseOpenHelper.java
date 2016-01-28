/**
 * NB. Le date del database sono memorizzate come campo INTEGER, in pratica valori long che rappresentano
 * il numero di secondi dal 1/1/1970.
 * IMPORTANTE: Memorizzare tutte le date mettendo sempre 0 come ore, minuti e secondi.
 */

package com.flingsoftware.personalbudget.database;

import static com.flingsoftware.personalbudget.database.StringheSQL.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import java.io.File;

import com.flingsoftware.personalbudget.R;


public class DatabaseOpenHelper extends SQLiteOpenHelper {
	//variabili
	private Context mioContext;
	
	//costanti
	public static final String NOME_DATABASE = "BudgetPersonale";
	public static final Object[] sDataLock = new Object[0];
	public static final int VERSIONE_APP = 14;
	
	
	public DatabaseOpenHelper(Context context, String name, CursorFactory factory) {
		super(context, name, factory, VERSIONE_APP);
		
		mioContext = context;
	}
			
	
	/**
	 * Per le caratteristiche e il funzionamento delle singole tabelle vedi il file budget.xls nella
	 * cartella database dell'applicazione.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREA_TABELLA_SPESE_VOCI);
		db.execSQL(CREA_TABELLA_SPESE_SOST);
		db.execSQL(CREA_TABELLA_SPESE_RIPET);
		db.execSQL(CREA_TABELLA_SPESE_BUDGET);
		db.execSQL(CREA_TABELLA_ENTRATE_VOCI);
		db.execSQL(CREA_TABELLA_ENTRATE_INC);
		db.execSQL(CREA_TABELLA_ENTRATE_RIPET);
		db.execSQL(CREA_TABELLA_CONTI);
	}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion < 2) {
			db.execSQL("ALTER TABLE spese_voci ADD COLUMN icona INTEGER DEFAULT 0");
			db.execSQL("ALTER TABLE entrate_voci ADD COLUMN icona INTEGER DEFAULT 1");
			aggiornaVersione2(db);
		}

		if(oldVersion < 4) {
			db.execSQL(CREA_TABELLA_CONTI);
			db.execSQL("ALTER TABLE spese_sost ADD COLUMN conto TEXT");
			db.execSQL("ALTER TABLE spese_ripet ADD COLUMN conto TEXT");
			db.execSQL("ALTER TABLE entrate_inc ADD COLUMN conto TEXT");
			db.execSQL("ALTER TABLE entrate_ripet ADD COLUMN conto TEXT");
			aggiornaVersione5(db);
			cancellaBackupAutomatici();
		}

		/*
			Aggiungo colonna favorite alle spese sostenute ed entrate incassate per gestire le
			transazioni preferite.
		 */
		if(oldVersion < 10) {
			db.execSQL("ALTER TABLE spese_sost ADD COLUMN favorite INTEGER DEFAULT 0");
			db.execSQL("ALTER TABLE entrate_inc ADD COLUMN favorite INTEGER DEFAULT 0");
			cancellaBackupAutomatici();
		}
	}
	
	
	/*
	 * Passaggio alla versione 2. Aggiungo una colonna alle tabelle spese_voci ed entrate_voci.
	 * La colonna � chiamata icona e contiene un valore integer che serve per individuare il file
	 * drawable dei tag (es. tag_1). 
	 * All'inizio per default metto 0 per le spese ed 1 per le entrate.
	 */
	private void aggiornaVersione2(SQLiteDatabase db) {
		//tabella spese_voci	
		Cursor curSpese = db.query("spese_voci", new String[] {"_id",  "voce", "icona"}, null, null, null, null, "voce");
		while(curSpese.moveToNext()) {
			long id = curSpese.getLong(curSpese.getColumnIndex("_id"));
			String voce = curSpese.getString(curSpese.getColumnIndex("voce"));
			ContentValues aggiornaContact = new ContentValues();
			aggiornaContact.put("voce", voce);
			aggiornaContact.put("icona", 0);
			db.update("spese_voci", aggiornaContact, "_id=" + id, null);
		}
		curSpese.close();
		
		//tabella entrate_voci
		Cursor curEntrate = db.query("entrate_voci", new String[] {"_id",  "voce", "icona"}, null, null, null, null, "voce");
		while(curEntrate.moveToNext()) {
			long id = curEntrate.getLong(curEntrate.getColumnIndex("_id"));
			String voce = curEntrate.getString(curEntrate.getColumnIndex("voce"));
			ContentValues aggiornaContact = new ContentValues();
			aggiornaContact.put("voce", voce);
			aggiornaContact.put("icona", 1);
			db.update("entrate_voci", aggiornaContact, "_id=" + id, null);
		}
		curEntrate.close();
	}
	
	
	/*
	 * Passaggio alla versione 4. Aggiungo una colonna "conto" alle tabelle spese_sost, spese_ripe,
	 * entrate_inc e entrate_ripet. Serve per gestire diversi conti (cash, conto bancario, carte di
	 * credito, ecc.). Imposto il valore iniziale su "default".
	 */
	private void aggiornaVersione5(SQLiteDatabase db) {
		//inserimento valore di default nella tabella conti
		ContentValues nuovaVoce = new ContentValues();
		nuovaVoce.put("conto", "default");
		nuovaVoce.put("saldo", 0);
		nuovaVoce.put("data_saldo", 0);	
		db.insert("conti", null, nuovaVoce);
		
		// Tabella spese_voci: aggiungo voce per giroconti
		nuovaVoce.clear();
		nuovaVoce.put("voce", mioContext.getString(R.string.voce_giroconto));
		nuovaVoce.put("icona", 58);
		db.insert("spese_voci", null, nuovaVoce);
		db.insert("entrate_voci", null, nuovaVoce);
		
		//tabella spese_sost	
		Cursor curSpeseSost = db.query("spese_sost", null, null, null, null, null, null);
		while(curSpeseSost.moveToNext()) {
			long id = curSpeseSost.getLong(curSpeseSost.getColumnIndex("_id"));
			long data = curSpeseSost.getLong(curSpeseSost.getColumnIndex("data"));
			String voce = curSpeseSost.getString(curSpeseSost.getColumnIndex("voce"));
			double importo = curSpeseSost.getDouble(curSpeseSost.getColumnIndex("importo"));
			String valuta = curSpeseSost.getString(curSpeseSost.getColumnIndex("valuta"));
			double importoValprin = curSpeseSost.getDouble(curSpeseSost.getColumnIndex("importo_valprin"));
			String descrizione = curSpeseSost.getString(curSpeseSost.getColumnIndex("descrizione"));
			long ripetizioneId = curSpeseSost.getLong(curSpeseSost.getColumnIndex("ripetizione_id"));
			
			ContentValues aggiornaSpesaSost = new ContentValues();
			aggiornaSpesaSost.put("data", data);
			aggiornaSpesaSost.put("voce", voce);
			aggiornaSpesaSost.put("importo", importo);
			aggiornaSpesaSost.put("valuta", valuta);
			aggiornaSpesaSost.put("importo_valprin", importoValprin);
			aggiornaSpesaSost.put("descrizione", descrizione);
			aggiornaSpesaSost.put("ripetizione_id", ripetizioneId);
			aggiornaSpesaSost.put("conto", "default");

			db.update("spese_sost", aggiornaSpesaSost, "_id=" + id, null);
		}
		curSpeseSost.close();
		
		//tabella spese_ripet	
		Cursor curSpeseRipet = db.query("spese_ripet", null, null, null, null, null, null);
		while(curSpeseRipet.moveToNext()) {
			long id = curSpeseRipet.getLong(curSpeseRipet.getColumnIndex("_id"));
			String voce = curSpeseRipet.getString(curSpeseRipet.getColumnIndex("voce"));
			String ripetizione = curSpeseRipet.getString(curSpeseRipet.getColumnIndex("ripetizione"));
			double importo = curSpeseRipet.getDouble(curSpeseRipet.getColumnIndex("importo"));
			String valuta = curSpeseRipet.getString(curSpeseRipet.getColumnIndex("valuta"));
			double importoValprin = curSpeseRipet.getDouble(curSpeseRipet.getColumnIndex("importo_valprin"));
			String descrizione = curSpeseRipet.getString(curSpeseRipet.getColumnIndex("descrizione"));
			long dataInizio = curSpeseRipet.getLong(curSpeseRipet.getColumnIndex("data_inizio"));
			int flagFine = curSpeseRipet.getInt(curSpeseRipet.getColumnIndex("flag_fine"));
			long dataFine = curSpeseRipet.getLong(curSpeseRipet.getColumnIndex("data_fine"));
			long aggiornatoA = curSpeseRipet.getLong(curSpeseRipet.getColumnIndex("aggiornato_a"));
					
			ContentValues aggiornaSpesaRipet = new ContentValues();
			aggiornaSpesaRipet.put("voce", voce);
			aggiornaSpesaRipet.put("ripetizione", ripetizione);
			aggiornaSpesaRipet.put("importo", importo);
			aggiornaSpesaRipet.put("valuta", valuta);
			aggiornaSpesaRipet.put("importo_valprin", importoValprin);
			aggiornaSpesaRipet.put("descrizione", descrizione);
			aggiornaSpesaRipet.put("data_inizio", dataInizio);
			aggiornaSpesaRipet.put("flag_fine", flagFine);
			aggiornaSpesaRipet.put("data_fine", dataFine);
			aggiornaSpesaRipet.put("aggiornato_a", aggiornatoA);
			aggiornaSpesaRipet.put("conto", "default");

			db.update("spese_ripet", aggiornaSpesaRipet, "_id=" + id, null);
		}
		curSpeseRipet.close();
		
		//tabella entrate_inc	
		Cursor curEntrateInc = db.query("entrate_inc", null, null, null, null, null, null);
		while(curEntrateInc.moveToNext()) {
			long id = curEntrateInc.getLong(curEntrateInc.getColumnIndex("_id"));
			long data = curEntrateInc.getLong(curEntrateInc.getColumnIndex("data"));
			String voce = curEntrateInc.getString(curEntrateInc.getColumnIndex("voce"));
			double importo = curEntrateInc.getDouble(curEntrateInc.getColumnIndex("importo"));
			String valuta = curEntrateInc.getString(curEntrateInc.getColumnIndex("valuta"));
			double importoValprin = curEntrateInc.getDouble(curEntrateInc.getColumnIndex("importo_valprin"));
			String descrizione = curEntrateInc.getString(curEntrateInc.getColumnIndex("descrizione"));
			long ripetizioneId = curEntrateInc.getLong(curEntrateInc.getColumnIndex("ripetizione_id"));
			
			ContentValues aggiornaEntrataInc = new ContentValues();
			aggiornaEntrataInc.put("data", data);
			aggiornaEntrataInc.put("voce", voce);
			aggiornaEntrataInc.put("importo", importo);
			aggiornaEntrataInc.put("valuta", valuta);
			aggiornaEntrataInc.put("importo_valprin", importoValprin);
			aggiornaEntrataInc.put("descrizione", descrizione);
			aggiornaEntrataInc.put("ripetizione_id", ripetizioneId);
			aggiornaEntrataInc.put("conto", "default");

			db.update("entrate_inc", aggiornaEntrataInc, "_id=" + id, null);
		}
		curEntrateInc.close();
		
		//tabella entrate_ripet	
		Cursor curEntrateRipet = db.query("entrate_ripet", null, null, null, null, null, null);
		while(curEntrateRipet.moveToNext()) {
			long id = curEntrateRipet.getLong(curEntrateRipet.getColumnIndex("_id"));
			String voce = curEntrateRipet.getString(curEntrateRipet.getColumnIndex("voce"));
			String ripetizione = curEntrateRipet.getString(curEntrateRipet.getColumnIndex("ripetizione"));
			double importo = curEntrateRipet.getDouble(curEntrateRipet.getColumnIndex("importo"));
			String valuta = curEntrateRipet.getString(curEntrateRipet.getColumnIndex("valuta"));
			double importoValprin = curEntrateRipet.getDouble(curEntrateRipet.getColumnIndex("importo_valprin"));
			String descrizione = curEntrateRipet.getString(curEntrateRipet.getColumnIndex("descrizione"));
			long dataInizio = curEntrateRipet.getLong(curEntrateRipet.getColumnIndex("data_inizio"));
			int flagFine = curEntrateRipet.getInt(curEntrateRipet.getColumnIndex("flag_fine"));
			long dataFine = curEntrateRipet.getLong(curEntrateRipet.getColumnIndex("data_fine"));
			long aggiornatoA = curEntrateRipet.getLong(curEntrateRipet.getColumnIndex("aggiornato_a"));
					
			ContentValues aggiornaEntrataRipet = new ContentValues();
			aggiornaEntrataRipet.put("voce", voce);
			aggiornaEntrataRipet.put("ripetizione", ripetizione);
			aggiornaEntrataRipet.put("importo", importo);
			aggiornaEntrataRipet.put("valuta", valuta);
			aggiornaEntrataRipet.put("importo_valprin", importoValprin);
			aggiornaEntrataRipet.put("descrizione", descrizione);
			aggiornaEntrataRipet.put("data_inizio", dataInizio);
			aggiornaEntrataRipet.put("flag_fine", flagFine);
			aggiornaEntrataRipet.put("data_fine", dataFine);
			aggiornaEntrataRipet.put("aggiornato_a", aggiornatoA);
			aggiornaEntrataRipet.put("conto", "default");

			db.update("entrate_ripet", aggiornaEntrataRipet, "_id=" + id, null);
		}
		curEntrateRipet.close();
	}
	
	
	/**
	 * Cancello tutti i backup automatici contenuti nella memoria interna dell'app. Questo perch�
	 * cambio la struttura del database, quindi bisogna evitare che l'utente faccia il restore
	 * di un database vecchia versione.
	 */
	private void cancellaBackupAutomatici() {
		String fileList[] = mioContext.fileList();
		File dir = mioContext.getFilesDir();
		for(String tmp : fileList) {
			if(tmp.contains("BudgetPersonale_backup_")) {
				new File(dir, tmp).delete();
			}
		}
	}
}
