/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

/**
 * Gestione tabella entrate_ripet
 * Funzionamento del database:
 * per ogni aspetto non specificatamente commentato in questa classe fare riferimento alla guida
 * "Siluppare App per Android" - Deitel, da pag. 284.
 * @author Claudio "iClaude" Agostini
 */
package com.flingsoftware.personalbudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;


public class DBCEntrateRipetute extends DBCExpEarRepeatedAbs {
	
	public DBCEntrateRipetute(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return "entrate_inc";
	}
	
	/**
	 * Inserisce una entrata ripetuta nella tabella entrate_ripet.
	 * 
	 * @param voce voce della entrata che deve essere ripetuta
	 * @param ripetizione tipo di ripetizione (una_tantum, giornaliero, settimanale, bisettimanale, mensile, 
	 * annuale, giorni_lavorativi, weekend)
	 * @param importo importo entrata
	 * @param valuta simbolo valuta
	 * @param importo_valprin importo nella valuta di default
	 * @param descrizione descrizione della entrata ripetuta
	 * @param data_inizio data di inizio ripetizione entrata nel formato Unix Time, the number of seconds 
	 * since 1970-01-01 00:00:00 UTC
	 * @param flag_fine la ripetizione è già finita (1) o è in corso(0)
	 * @param data_fine data di fine ripetizione entrata nel formato Unix Time, the number of seconds 
	 * since 1970-01-01 00:00:00 UTC
	 * @param aggiornato_a aggiornamento tabella entrate_inc fino a questa data Unix Time, the number 
	 * of seconds since 1970-01-01 00:00:00 UTC
	 * @param conto nome del conto
	 * 
	 * @return id id della riga della tabella dove è stato inserito il nuovo record
	 */
	public long inserisciEntrataRipetuta(String voce, String ripetizione, double importo, String valuta, double importo_valprin, String descrizione, long data_inizio, int flag_fine, long data_fine, long aggiornato_a, String conto) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("voce", voce);
		nuovoContact.put("ripetizione", ripetizione);
		nuovoContact.put("importo", importo);
		nuovoContact.put("valuta", valuta);
		nuovoContact.put("importo_valprin", importo_valprin);
		nuovoContact.put("descrizione", descrizione);
		nuovoContact.put("data_inizio", data_inizio);
		nuovoContact.put("flag_fine", flag_fine);
		nuovoContact.put("data_fine", data_fine);
		nuovoContact.put("aggiornato_a", aggiornato_a);
		nuovoContact.put("conto", conto);
		
		long id;
		synchronized (sDataLock) {
			openModifica();
			id = mioSQLiteDatabase.insert("entrate_ripet", null, nuovoContact);
			close();
		}
		
		return id;
	}
	
	
	/**
	 * Aggiorna una entrata ripetuta nella tabella entrate_ripet.
	 * 
	 * @param id id della entrata ripetuta
	 * @param voce voce della entrata che deve essere ripetuta
	 * @param ripetizione tipo di ripetizione (una_tantum, giornaliero, settimanale, bisettimanale, mensile, 
	 * annuale, giorni_lavorativi, weekend)
	 * @param importo importo entrata
	 * @param valuta simbolo valuta
	 * @param importo_valprin importo nella valuta di default
	 * @param descrizione descrizione della entrata ripetuta
	 * @param data_inizio data di inizio ripetizione entrata nel formato Unix Time, the number of seconds 
	 * since 1970-01-01 00:00:00 UTC
	 * @param flag_fine la ripetizione è già finita (1) o è in corso(0)
	 * @param data_fine data di fine ripetizione entrata nel formato Unix Time, the number of seconds since 
	 * 1970-01-01 00:00:00 UTC
	 * @param aggiornato_a aggiornamento tabella entrate_inc fino a questa data nel formato Unix Time, the 
	 * number of seconds since 1970-01-01 00:00:00 UTC
	 * @param conto nome del conto
	 */
	public void aggiornaEntrataRipetuta(long id, String voce, String ripetizione, double importo, String valuta, double importo_valprin, String descrizione, long data_inizio, int flag_fine, long data_fine, long aggiornato_a, String conto) {
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("voce", voce);
		aggiornaContact.put("ripetizione", ripetizione);
		aggiornaContact.put("importo", importo);
		aggiornaContact.put("valuta", valuta);
		aggiornaContact.put("importo_valprin", importo_valprin);
		aggiornaContact.put("descrizione", descrizione);
		aggiornaContact.put("data_inizio", data_inizio);
		aggiornaContact.put("flag_fine", flag_fine);
		aggiornaContact.put("data_fine", data_fine);
		aggiornaContact.put("aggiornato_a", aggiornato_a);
		aggiornaContact.put("conto", conto);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.update("entrate_ripet", aggiornaContact, "_id=" + id, null);
			close();
		}
	}
	

	/**
	 * Ottiene un Cursor che rappresenta una entrata ripetuta dalla tabella entrate_ripet.
	 * 
	 * @param id id della entrata ripetuta di questa tabella
	 * @return un Cursor che rappresenta la entrata ripetuta selezionata
	 */
	@Override
	public Cursor getItemRepeated(long id) {
		return mioSQLiteDatabase.query("entrate_ripet", null, "_id=" + id,  null,  null,  null,  null);
	}


	/**
	 * Restituisce tutte le entrate appartenenti al conto indicato.
	 * @return Cursor con tutti i campi
	 */
	public Cursor getTutteLeEntrateContoX(String conto) {
		return mioSQLiteDatabase.query("entrate_ripet", null, "conto = ?", new String[] {conto}, null, null, null);
	}
	
	
	//metodo di debug: restituisce tutti i record (tutti i campi)
	public Cursor getTutteLeEntrate() {
		return mioSQLiteDatabase.query("entrate_ripet", null, null, null, null, null, null);
	}
}


