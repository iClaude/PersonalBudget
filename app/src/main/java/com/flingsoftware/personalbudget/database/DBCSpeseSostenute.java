/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

/**
 * Gestione tabella spese_sost
 * Funzionamento del database:
 * per ogni aspetto non specificatamente commentato in questa classe fare riferimento alla guida
 * "Siluppare App per Android" - Deitel, da pag. 284.
 * @author Claudio "iClaude" Agostini
 */
package com.flingsoftware.personalbudget.database;

import android.content.Context;
import android.database.Cursor;

import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_DATAX_ELENCO_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_MIN_MAX_PER_VOCE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_NO_TRASF;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_SPESAX_AVG;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_SPESAX_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_SPESAX_MAX;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_SPESAX_MIN;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_SPESAX_TOTALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_SPESAX_X_BUDGET_SPESE_INCLUSE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_TOTALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_TOTALE_NO_TRASF;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_TOTALI_PER_DATA_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO_NO_TRASF;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_PREFERITE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_SOST_SPESAX_DETTAGLIO;

// stringhe SQL usate in questa classe


public class DBCSpeseSostenute extends DBCExpEarAbs {
	
	public DBCSpeseSostenute(Context context) {
		super(context);
	}


	@Override
	public String getTableName() {
		return "spese_sost";
	}


	/**
	 * Ottiene un Cursor che rappresenta una spesa sostenuta dalla tabella spese_sost.
	 * 
	 * @param id id della spesa sostenuta di questa tabella
	 * @return un Cursor che rappresenta la spesa sostenuta selezionata
	 */
	public Cursor getSpesaSostenuta(long id) {
		return mioSQLiteDatabase.query("spese_sost", null, "_id=" + id,  null,  null,  null,  null);
	}


	/**
	 * Si fornisce in input la data iniziale e la data finale e si ottengono tutte le spese sostenute tra le 
	 * due date, ordinate per data discendente (tutti i campi).
	 * 
	 * @param dataInizio data iniziale da cui ottenere le spese sostenute 
	 * @param dataFine data finale fino alla quale ottenere le spese sostenute 
	 * @return Cursor con tutti i campi della tabella spese_sost impostati (ordinato per data disc.)
	 */
	public Cursor getSpeseSostenuteIntervallo(Long dataInizio, Long dataFine) {	
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO, new String[] {dataInizio.toString(), dataFine.toString()});
	}
	
	/*
	 * Metodo analogo al precedente ma escludendo dalle spese tutti i giroconti.
	 * Passare il nome della voce, localizzata per lingua, utilizzata per identificare i giroconti.
	 */
	public Cursor getSpeseSostenuteIntervalloNoTrasf(Long dataInizio, Long dataFine, String trasf) {	
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_NO_TRASF, new String[] {dataInizio.toString(), dataFine.toString(), trasf});
	}

	
	/**
	 * Si fornisce in input la data iniziale, la data finale e la stringa di ricerca e si ottengono tutte 
	 * le spese sostenute tra le due date e con i campi voce o descrizione che contengono la stringa
	 * di ricerca. Il risultato viene raggruppato per data, con totali per ogni data.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le spese sostenute 
	 * @param dataFine data finale fino alla quale ottenere le spese sostenute 
	 * @param str stringa da cercare
	 * @return Cursor con i campi data e totale_spesa
	 */
	public Cursor getSpeseSostenuteIntervalloTotaliPerDataFiltrato(String conto, Long dataInizio, Long dataFine, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_TOTALI_PER_DATA_FILTRATO, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), "%" + str + "%", "%" + str + "%"});
	}
	
	
	/**
	 * Si fornisce in input la data iniziale e la data finale e si ottengono tutte le spese sostenute tra le 
	 * due date. Il risultato viene raggruppato per voce di spesa, con totali per ogni voce, ed ordinato
	 * per totale spesa discendente.
	 * 
	 * @param dataInizio data iniziale da cui ottenere le spese sostenute
	 * @param dataFine data finale fino alla quale ottenere le spese sostenute
	 * @return Cursor con i campi voce e totale_spesa tra le due date specificate
	 */
	public Cursor getSpeseSostenuteIntervalloTotaliPerVoceOrdinatoPerImporto(Long dataInizio, Long dataFine) {	
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO, new String[] {dataInizio.toString(), dataFine.toString()});
	}
	
	/*
	 * Metodo analogo al precedente ma escludendo dalle spese tutti i giroconti, che non devono entrare nelle
	 * statistiche sulle spese, non essendo spese.
	 * Passare il nome della voce, localizzata per lingua, utilizzata per identificare i trasferimenti.
	 */
	public Cursor getSpeseSostenuteIntervalloTotaliPerVoceOrdinatoPerImportoNoTrasf(Long dataInizio, Long dataFine, String trasf) {	
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_ORDINATO_PER_IMPORTO_NO_TRASF, new String[] {dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	/**
	 * Si fornisce in input la data iniziale, la data finale e la stringa da cercare e si ottengono tutte le 
	 * spese sostenute tra le due date con i campi voce o descrizione che contengono la stringa cercata. Il 
	 * risultato viene raggruppato per voce di spesa, con totali per ogni voce.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le spese sostenute
	 * @param dataFine data finale fino alla quale ottenere le spese sostenute
	 * @param str stringa da cercare
	 * @return Cursor con i campi data e totale_spesa tra le due date specificate
	 */
	public Cursor getSpeseSostenuteIntervalloTotaliPerVoceFiltrato(String conto, Long dataInizio, Long dataFine, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_TOTALI_PER_VOCE_FILTRATO, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), "%" + str + "%", "%" + str + "%"});
	}

	
	/**
	 * Questa funzione è uguale a quella precedente ma è pensata per ricercare più voci di spesa
	 * contemporaneamente (il numero non è definito a priori). Quindi sia la query di ricerca
	 * che gli argomenti devono essere creati dinamicamente dal programma e passati alla funzione.
	 * Il raggruppamento può essere fatto per data o voce a seconda della query utilizzata.
	 * 
	 * @param query query di ricerca in SQL
	 * @param args[] array di String contenenti le variabili da sostituire ai ? nella query
	 * @return Cursor con i campi data e totale_spesa
	 */
	public Cursor getSpeseSostenuteIntervalloSpeseXYZTotaliPerDataOVoce(String query, String[] args) {	
		return mioSQLiteDatabase.rawQuery(query, args);
	}

	
	/**
	 * Si fornisce in input una dataInizio, una dataFine, una voce di spesa e una stringa di ricerca 
	 * e si ottiene l'elenco di tutte le spese sostenute con quella voce (stessa voce di spesa) 
	 * tra le due date indicate, con i campi voce o descrizione che contengono la stringa cercata.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale da cui ottenere le spese sostenute 
	 * @param dataFine data finale fino alla quale ottenere le spese sostenute
	 * @param voce voce della spesa da ricercare
	 * @param str stringa da cercare
	 * @return Cursor con i campi data, voce, importo_valprin e ripetizione_id
	 */
	public Cursor getSpeseSostenuteIntervalloSpesaXFiltrato(String conto, Long dataInizio, Long dataFine, String voce, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_FILTRATO, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), voce, "%" + str + "%", "%" + str + "%"});
	}

	/**
	 * Idem di prima, ma con i campi che servono per visualizzare le spese incluse in un budget.
	 *
	 * @param dataInizio data iniziale del budget
	 * @param dataFine   data finale del budget
	 * @param voce       voce di spesa
	 * @return Cursor con i campi data, importo_valprin, descrizione e conto
	 */
	public Cursor getSpeseSostenuteIntervalloSpesaXBudgetSpeseIncluse(Long dataInizio, Long dataFine, String voce) {
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_X_BUDGET_SPESE_INCLUSE, new String[]{dataInizio.toString(), dataFine.toString(), voce});
	}
	
	/**
	 * Questo metodo è uguale a quello precedente, solo che si possono inserire più voci di 
	 * spesa contemporaneamente. La query di ricerca e l'elenco degli argomenti vanno creati
	 * dinamicamente dal programma.
	 * 
	 * @param query query di ricerca SQL 
	 * @param args array di String con i parametri da sostituire ai ? nella query
	 * @return Cursor con i campi data, voce, importo_valprin e ripetizione_id
	 */
	public Cursor getSpeseSostenuteIntervalloSpeseXYZ(String query, String[] args) {	
		return mioSQLiteDatabase.rawQuery(query, args);
	}
	
	
	/**
	 * Si fornisce in input la data iniziale e la data finale e si ottiene il totale delle spese
	 * sostenute in quell'intervallo.
	 * 
	 * @param conto nome del conto
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @return Cursor con il campo totale_spesa (spesa totale sostenuta nel periodo selezionato)
	 */
	public Cursor getSpeseSostenuteIntervalloTotale(String conto, Long dataInizio, Long dataFine) {
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_TOTALE, new String[] {paramConto, dataInizio.toString(), dataFine.toString()});
	}
	
	/*
	 * Metodo analogo al precedente ma escludendo dalle spese tutti i giroconti, che non devono entrare nelle
	 * statistiche sulle spese, non essendo spese.
	 * Passare il nome della voce, localizzata per lingua, utilizzata per identificare i trasferimenti.
	 */
	public Cursor getSpeseSostenuteIntervalloTotaleNoTrasf(String conto, Long dataInizio, Long dataFine, String trasf) {
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_TOTALE_NO_TRASF, new String[] {paramConto, dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	
	/**
	 * Si fornisce in input la data iniziale, la data finale e la voce della spesa e si ottiene il totale 
	 * di quella specifica spesa sostenuta in quell'intervallo.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce della spesa di cui calcolare il totale
	 * @return Cursor con il campo totale_spesa (totale della spesa specificata sostenuta nel periodo 
	 * selezionato)
	 */
	public Cursor getSpeseSostenuteIntervalloSpesaXTotale(Long dataInizio, Long dataFine, String voce) {
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_TOTALE, new String[] {dataInizio.toString(), dataFine.toString(), voce});
	}
	
	
	/**
	 * Fornisco in input la data iniziale, la data finale e la voce di spesa ed ottengo il valore minimo
	 * di quella voce di spesa nel periodo considerato.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di spesa di cui calcolare il valore minimo
	 * @return importo spesa minimo
	 */
	public double getSpeseSostenuteIntervalloSpesaXMin(Long dataInizio, Long dataFine, String voce) {
		Cursor cur = mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_MIN, new String[] {dataInizio.toString(), dataFine.toString(), voce});
		double importo = 0;
		if(cur.moveToFirst()) {
			importo = cur.getDouble(cur.getColumnIndex("min_spesa"));
		}
		
		return importo;
	}
	
	
	/**
	 * Fornisco in input la data iniziale, la data finale e la voce di spesa ed ottengo il valore massimo
	 * di quella voce di spesa nel periodo considerato.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di spesa di cui calcolare il valore massimo
	 * @return importo spesa massimo
	 */
	public double getSpeseSostenuteIntervalloSpesaXMax(Long dataInizio, Long dataFine, String voce) {
		Cursor cur = mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_MAX, new String[] {dataInizio.toString(), dataFine.toString(), voce});
		double importo = 0;
		if(cur.moveToFirst()) {
			importo = cur.getDouble(cur.getColumnIndex("max_spesa"));
		}
		
		return importo;
	}
	
	
	/**
	 * Fornisco in input la data iniziale, la data finale e la voce di spesa ed ottengo il valore medio
	 * di quella voce di spesa nel periodo considerato.
	 * 
	 * @param dataInizio data iniziale del periodo di interesse
	 * @param dataFine data finale del periodo di interesse
	 * @param voce voce di spesa di cui calcolare il valore medio
	 * @return importo spesa medio
	 */
	public double getSpeseSostenuteIntervalloSpesaXAvg(Long dataInizio, Long dataFine, String voce) {
		Cursor cur = mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_AVG, new String[] {dataInizio.toString(), dataFine.toString(), voce});
		double importo = 0;
		if(cur.moveToFirst()) {
			importo = cur.getDouble(cur.getColumnIndex("avg_spesa"));
		}
		
		return importo;
	}
	
	
	/**
	 * Si fornisce in input la data iniziale e quella finale e si ottiene un Cursor contenente
	 * per ogni voce di spesa la spesa minima e quella massima sostenute nel periodo.
	 * 
	 * @param dataInizio data iniziale del periodo
	 * @param dataFine data finale del periodo
	 * @param trasf nome della voce relativa ai giroconti (vanno esclusi dalle statistiche su spese/entrate)
	 * @return Cursor con i campi _id, voce, min_spesa e max_spesa
	 */
	public Cursor getSpeseSostenuteIntervalloMinMaxPerVoce(Long dataInizio, Long dataFine, String trasf) {
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_MIN_MAX_PER_VOCE, new String[] {dataInizio.toString(), dataFine.toString(), trasf});
	}
	
	
	/**
	 * Si fornisce in input la data x e la stringa di ricerca e si ottengono tutte le spese sostenute in quella 
	 * data con i campi voce o descrizione che contengono la stringa cercata. Non viene fatto alcun 
	 * raggruppamento, quindi la stessa voce di spesa potrebbe comparire più volte.
	 * 
	 * @param conto nome del conto
	 * @param dataX data in relazione alla quale elencare le spese sostenute
	 * @param str stringa da cercare
	 * @return Cursor i campi voce, ripetizione_id e importo_valprin di tutte le spese sostenute alla data X
	 */
	public Cursor getSpeseSostenuteDataXElencoFiltrato(String conto, Long dataX, String str) {	
		String paramConto = "%" + conto + "%";
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_DATAX_ELENCO_FILTRATO, new String[] {paramConto, dataX.toString(), "%" + str + "%", "%" + str + "%"});
	}
	
	
	/**
	 * Si fornisce in input l'id della spesa e si ottengono informazioni di dettaglio di tale spesa.
	 * 
	 * @param spesaId id della spesa da estrarre dalla tabella
	 * @return Cursor con i seguenti campi del database relativi alla spesa specificata: voce, 
	 * importo_valprin, data, descrizione, ripetizione_id, ripetizione
	 */
	public Cursor getSpesaSostenutaX(Long spesaId) {	
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_SPESAX_DETTAGLIO, new String[] {spesaId.toString()});
	}
	
	/**
	 * Restituisce tutte le spese appartenenti al conto indicato.
	 * @return Cursor con tutti i campi
	 */
	public Cursor getTutteLeSpeseContoX(String conto) {
		return mioSQLiteDatabase.query("spese_sost", null, "conto = ?", new String[] {conto}, null, null, null);
	}
	
	//metodo di debug: restituisce tutte le spese della tabella (tutti i campi)
	public Cursor getTutteLeSpese() {
		return mioSQLiteDatabase.query("spese_sost", null, null, null, null, null, null);
	}

	/**
	 * Restituisce le spese sostenute impostate dall'utente come preferite (campo favorite
	 * su 1).
	 * @return Cursor contenente tutti i campi delle spese preferite
	 */
	public Cursor getSpeseSostenutePreferite() {
		return mioSQLiteDatabase.rawQuery(SPESE_SOST_PREFERITE, new String[] {});
	}

}

