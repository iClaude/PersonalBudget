/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

/**
 * Gestione tabella entrate_inc
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
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_DATAX_ELENCO_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_ENTRATAX_DETTAGLIO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_ENTRATAX_AVG;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_ENTRATAX_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_ENTRATAX_MAX;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_ENTRATAX_MIN;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_ENTRATAX_TOTALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_MIN_MAX_PER_VOCE;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_NO_TRASF;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_TOTALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_TOTALE_NO_TRASF;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_TOTALI_PER_DATA_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO_NO_TRASF;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_INC_PREFERITE;


public class DBCEntrateIncassate extends DBCExpEarAbs {
	
	public DBCEntrateIncassate(Context context) {
		super(context);
	}

	@Override
	public String getTableName() {
		return "entrate_inc";
	}

	
	/**
	 * Aggiorna una entrata incassata nella tabella entrate_inc.
	 * 
	 * @param id id della entrata incassata
	 * @param data data di incasso della entrata nel formato Unix Time, the number of seconds since 
	 * 1970-01-01 00:00:00 UTC
	 * @param voce voce della entrata come da tabella entrate_voci
	 * @param importo importo entrata in valuta
	 * @param valuta simbolo della valuta
	 * @param importo_valprin importo entrata nella valuta di default
	 * @param descrizione descrizione entrata
	 * @param ripetizione_id id per eventuali entrate ripetute, come da tabella entrate_ripet
	 * @param conto nome del conto
	 * @param favorite 1 for favorite transactions, 0 otherwise
	 */
	public void aggiornaEntrataIncassata(long id, long data, String voce, double importo, String valuta, double importo_valprin, String descrizione, long ripetizione_id, String conto, int favorite) {
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("data", data);
		aggiornaContact.put("voce", voce);
		aggiornaContact.put("importo", importo);
		aggiornaContact.put("valuta", valuta);
		aggiornaContact.put("importo_valprin", importo_valprin);
		aggiornaContact.put("descrizione", descrizione);
		aggiornaContact.put("ripetizione_id", ripetizione_id);
		aggiornaContact.put("conto", conto);
		aggiornaContact.put("favorite", favorite);

		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.update("entrate_inc", aggiornaContact, "_id=" + id, null);
			close();
		}
	}
	

	/**
	 * Ottiene un Cursor che rappresenta una entrata incassata dalla tabella entrate_inc.
	 * 
	 * @param id id della entrata incassata di questa tabella
	 * @return un Cursor che rappresenta la entrata incassata selezionata
	 */
	public Cursor getEntrataIncassata(long id) {
		return mioSQLiteDatabase.query("entrate_inc", null, "_id=" + id,  null,  null,  null,  null);
	}

	
	/**
	 * Si fornisce in input la data iniziale e la data finale e si ottengono tutte le entrate incassate 
	 * tra le due date, ordinate per data discendente (tutti i campi).
	 * 
	 * @param dataInizio data iniziale da cui ottenere le entrate incassate 
	 * @param dataFine data finale fino alla quale ottenere le entrate incassate 
	 * @return Cursor con tutti i campi della tabella entrate_inc impostati (ordinato per data disc.)
	 */
	public Cursor getEntrateIncassateIntervallo(Long dataInizio, Long dataFine) {	
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO, new String[] {dataInizio.toString(), dataFine.toString()});
	}

	/*
	 * Metodo analogo al precedente ma escludendo dalle entrate tutti i giroconti.
	 * Passare il nome della voce, localizzata per lingua, utilizzata per identificare i giroconti.
	 */
	public Cursor getEntrateIncassateIntervalloNoTrasf(Long dataInizio, Long dataFine, String trasf) {	
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_NO_TRASF, new String[] {dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	/**
	 * Si fornisce in input la data iniziale, la data finale e la stringa di ricerca e si ottengono 
	 * tutte le entrate incassate tra le due date con il campo voce o descrizione che contengono
	 * la stringa cercata. Il risultato viene raggruppato per data, con totali per ogni data.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le entrate incassate 
	 * @param dataFine data finale fino alla quale ottenere le entrate incassate 
	 * @param str stringa da cercare
	 * @return Cursor con i campi data e totale_entrata
	 */
	public Cursor getEntrateIncassateIntervalloTotaliPerDataFiltrato(String conto, Long dataInizio, Long dataFine, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_TOTALI_PER_DATA_FILTRATO, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), "%" + str + "%", "%" + str + "%"});
	}
	
	
	/**
	 * Si fornisce in input la data iniziale e la data finale e si ottengono tutte le entrate incassate 
	 * tra le due date. Il risultato viene raggruppato per voce di entrata, con totali per ogni voce ed
	 * ordinato per totale entrata discendenti.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le entrate incassate
	 * @param dataFine data finale fino alla quale ottenere le entrate incassate
	 * @return Cursor con i campi data e totale_entrata tra le due date specificate
	 */
	public Cursor getEntrateIncassateIntervalloTotaliPerVoceOrdinatoPerImporto(String conto, Long dataInizio, Long dataFine) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO, new String[] {paramConto, dataInizio.toString(), dataFine.toString()});
	}
	
	/*
	 * Metodo analogo al precedente ma escludendo dalle entrate tutti i giroconti, che non devono entrare nelle
	 * statistiche sulle entrate, non essendo entrate.
	 * Passare il nome della voce, localizzata per lingua, utilizzata per identificare i trasferimenti.
	 */
	public Cursor getEntrateIncassateIntervalloTotaliPerVoceOrdinatoPerImportoNoTrasf(String conto, Long dataInizio, Long dataFine, String trasf) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO_NO_TRASF, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	/**
	 * Si fornisce in input la data iniziale, la data finale e la stringa di ricerca e si ottengono 
	 * tutte le entrate incassate tra le due date con il campo voce o descrizione che contengono
	 * la stringa cercata. Il risultato viene raggruppato per voce di entrata, con totali per ogni 
	 * voce.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le entrate incassate
	 * @param dataFine data finale fino alla quale ottenere le entrate incassate
	 * @param str stringa da cercare
	 * @return Cursor con i campi data e totale_entrata tra le due date specificate
	 */
	public Cursor getEntrateIncassateIntervalloTotaliPerVoceFiltrato(String conto, Long dataInizio, Long dataFine, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_TOTALI_PER_VOCE_FILTRATO, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), "%" + str + "%", "%" + str + "%"});
	}
	
	
	/**
	 * Si fornisce in input una data iniziale, una data finale, la voce di una entrata e la stringa di ricerca e 
	 * si ottiene l'elenco di tutte le entrate incassate con quella voce (stessa voce di entrata) tra le due date 
	 * indicate con i campi voce o descrizione che contengono la stringa cercata.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le entrate incassate 
	 * @param dataFine data finale fino alla quale ottenere le entrate incassate
	 * @param voce voce della entrata da ricercare
	 * @param str stringa da cercare
	 * @return Cursor con i campi data e importo_valprin
	 */
	public Cursor getEntrateIncassateIntervalloEntrataXFiltrato(String conto, Long dataInizio, Long dataFine, String voce, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_ENTRATAX_FILTRATO, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), voce, "%" + str + "%", "%" + str + "%"});
	}
	
	
	/**
	 * Si fornisce in input la data iniziale e la data finale e si ottiene il totale delle entrate
	 * incassate in quell'intervallo.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @return Cursor con il campo totale_entrata (entrata totale incassata nel periodo selezionato)
	 */
	public Cursor getEntrateIncassateIntervalloTotale(String conto, Long dataInizio, Long dataFine) {
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_TOTALE, new String[] {paramConto, dataInizio.toString(), dataFine.toString()});
	}
	
	/*
	 * Metodo analogo al precedente ma escludendo dalle entrate tutti i giroconti, che non devono entrare nelle
	 * statistiche sulle entrate, non essendo entrate.
	 * Passare il nome della voce, localizzata per lingua, utilizzata per identificare i trasferimenti.
	 */
	public Cursor getEntrateIncassateIntervalloTotaleNoTrasf(String conto, Long dataInizio, Long dataFine, String trasf) {
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_TOTALE_NO_TRASF, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	
	/**
	 * Si fornisce in input la data iniziale, la data finale e la voce di entrata e si ottiene il 
	 * totale di quella specifica voce di entrata incassata in quell'intervallo.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di entrata
	 * @return Cursor con il campo totale_entrata (entrata totale incassata nel periodo selezionato)
	 */
	public Cursor getEntrateIncassateIntervalloEntrataXTotale(String conto, Long dataInizio, Long dataFine, String voce) {
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_ENTRATAX_TOTALE, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), voce});
	}
	
	
	/**
	 * Fornisco in input la data iniziale, la data finale e la voce di entrata ed ottengo il valore minimo
	 * di quella voce di entrata nel periodo considerato.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di entrata di cui calcolare il valore minimo
	 * @return importo entrata minimo
	 */
	public double getEntrateIncassateIntervalloEntrataXMin(Long dataInizio, Long dataFine, String voce) {
		Cursor cur = mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_ENTRATAX_MIN, new String[] {dataInizio.toString(), dataFine.toString(), voce});
		double importo = 0;
		if(cur.moveToFirst()) {
			importo = cur.getDouble(cur.getColumnIndex("min_entrata"));
		}
		
		return importo;
	}
	
	
	/**
	 * Fornisco in input la data iniziale, la data finale e la voce di entrata ed ottengo il valore massimo
	 * di quella voce di entrata nel periodo considerato.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di entrata di cui calcolare il valore massimo
	 * @return importo entrata massimo
	 */
	public double getEntrataIncassateIntervalloEntrataXMax(Long dataInizio, Long dataFine, String voce) {
		Cursor cur = mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_ENTRATAX_MAX, new String[] {dataInizio.toString(), dataFine.toString(), voce});
		double importo = 0;
		if(cur.moveToFirst()) {
			importo = cur.getDouble(cur.getColumnIndex("max_entrata"));
		}
		
		return importo;
	}
	
	
	/**
	 * Fornisco in input la data iniziale, la data finale e la voce di entrata ed ottengo il valore medio
	 * di quella voce di entrata nel periodo considerato.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di entrata di cui calcolare il valore medio
	 * @return importo entrata medio
	 */
	public double getEntrateIncassateIntervalloEntrataXAvg(Long dataInizio, Long dataFine, String voce) {
		Cursor cur = mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_ENTRATAX_AVG, new String[] {dataInizio.toString(), dataFine.toString(), voce});
		double importo = 0;
		if(cur.moveToFirst()) {
			importo = cur.getDouble(cur.getColumnIndex("avg_entrata"));
		}
		
		return importo;
	}
	
	
	/**
	 * Si fornisce in input la data iniziale e quella finale e si ottiene un Cursor contenente
	 * per ogni voce di entrata l'entrata minima e quella massima incassate nel periodo.
	 * 
	 * @param dataInizio data iniziale del periodo
	 * @param dataFine data finale del periodo
	 * @param trasf nome della voce che identifica i giroconti, da escludere dalle statistiche
	 * @return Cursor con i campi _id, voce, min_entrata e max_entrata
	 */
	public Cursor getEntrateIncassateIntervalloMinMaxPerVoce(Long dataInizio, Long dataFine, String trasf) {
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_INTERVALLO_MIN_MAX_PER_VOCE, new String[] {dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	
	/**
	 * Si fornisce in input la data x e la stringa di ricerca e si ottengono tutte le entrate incassate 
	 * in quella data con il campo voce o descrizione che contengono la stringa cercata. Non viene 
	 * fatto nessun raggruppamento, quindi la stessa voce di entrata potrebbe comparire più volte
	 * 
	 * @param conto nome del conto
	 * @param dataX data in relazione alla quale elencare le entrate incassate
	 * @param str stringa da cercare
	 * @return Cursor i campi voce e importo_valprin di tutte le entrate incassate alla data X
	 */
	public Cursor getEntrateIncassateDataXElencoFiltrato(String conto, Long dataX, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_DATAX_ELENCO_FILTRATO, new String[] {paramConto, dataX.toString(), "%" + str + "%", "%" + str + "%"});
	}
	
	
		
	/**
	 * Si fornisce in input l'id della entrata e si ottengono informazioni di dettaglio di tale entrata.
	 * 
	 * @param entrataId id della entrata da estrarre dalla tabella
	 * @return Cursor con i seguenti campi del database relativi alla entrata specificata: voce, 
	 * importo_valprin, data, descrizione, ripetizione_id, ripetizione
	 */
	public Cursor getEntrataIncassataX(Long entrataId) {	
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_ENTRATAX_DETTAGLIO, new String[] {entrataId.toString()});
	}
	
	
	/**
	 * Restituisce tutte le entrate appartenenti al conto indicato.
	 * @return Cursor con tutti i campi
	 */
	public Cursor getTutteLeEntrateContoX(String conto) {
		return mioSQLiteDatabase.query("entrate_inc", null, "conto = ?", new String[]{conto}, null, null, null);
	}
	
	
	//metodo di debug: ricava tutti i record della tabella (tutti i campi)
	public Cursor getTutteLeEntrate() {
		return mioSQLiteDatabase.query("entrate_inc", null, null, null, null, null, null);
	}


	/**
	 * Restituisce le entrate incassate impostate dall'utente come preferite (campo favorite
	 * su 1).
	 * @return Cursor contenente tutti i campi delle entrate preferite
	 */
	public Cursor getEntrateIncassatePreferite() {
		return mioSQLiteDatabase.rawQuery(ENTRATE_INC_PREFERITE, new String[]{});
	}

}


