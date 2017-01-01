/*
 * Copyright (c) - Software developed by iClaude.
 */

/**
 * Gestione tabella spese_budget
 * Funzionamento del database:
 * per ogni aspetto non specificatamente commentato in questa classe fare riferimento alla guida
 * "Siluppare App per Android" - Deitel, da pag. 284.
 * @author Claudio "iClaude" Agostini
 */
package com.flingsoftware.personalbudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.StringTokenizer;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelperWrapper.sDataLock;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_ANNUALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_BISETTIMANALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_BUDGET_ANALOGHI;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_BUDGET_CONTENENTI_VOCE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_BUDGET_SCADUTI_O_QUASI;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_COMPLETO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_COMPLETO_GREZZO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_GIORNALIERO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_MENSILE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_NON_SCADUTI;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_NON_SCADUTI_FILTRATO;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_SETTIMANALE;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_BUDGET_ELENCO_UNA_TANTUM;


public class DBCSpeseBudget {
	
	public DBCSpeseBudget(Context context) {

	}
	
	
	public void openModifica() throws SQLException {
		mioSQLiteDatabase = DatabaseOpenHelperWrapper.getDatabase();
		mioSQLiteDatabase.execSQL("PRAGMA foreign_keys=ON;"); // bisogna abilitare le foreign keys qui
	}
	
	
	public void openLettura() throws SQLException {
		mioSQLiteDatabase = DatabaseOpenHelperWrapper.getDatabase();
	}
	
	
	public void close() {
		// No need to close the database because Android does that automatically for you.
		/*if(mioSQLiteDatabase != null)
			mioSQLiteDatabase.close();*/
	}
	
	
	/**
	 * Inserisce un budget per una specifica spesa.
	 * La scheda del budget funziona così:
	 * prendo la data odierna (es. 26/3) e prendo tutti i budget con riferimento a tale data: es. 
	 * giornaliero, mensile al 31/3, settimanale per il 31/3 ecc.
	 * calcolo il totale spese sostenute fino a in quel lasso di tempo
	 * calcolo il residuo
	 * 
	 * @param voce voce della spesa come da tabella spese_voci
	 * @param ripetizione tipo di ripetizione (giornaliero, settimanale, bisettimanale, mensile, annuale 
	 * e una_tantum)
	 * @param importo importo budget in valuta
	 * @param valuta simbolo valuta
	 * @param importo_valprin importo budget nella valuta di default
	 * @param data_inizio data di inizio solo per la ripetizione una_tantum nel formato Unix Time, the 
	 * number of seconds since 1970-01-01 00:00:00 UTC (la data iniziale si ricava dalla data odierna). 
	 * Per tutte le altre ripetizioni lasciare in bianco.
	 * @param aggiungere_rimanenza 0 no, 1 si (aggiungere l'importo risparmiato su questo budget a quello
	 * successivo, per i budget che si ripetono nel tempo
	 * @param data_fine data finale solo per la ripetizione una_tantum nel formato Unix Time, the number 
	 * of seconds since 1970-01-01 00:00:00 UTC. Per tutte le altre ripetizioni vale fino ad infinito.
	 * @param spesa_sost spesa sostenuta fino a questo momento su questo budget
	 * @param risparmio differenza tra importo_valprin e spesa_sost (importo risparmiato su questo budget)
	 * @param budget_iniziali per i budget che si ripetono (tutti tranne una_tantum), indica l'id del budget iniziale
	 * da cui questo budget deriva
	 * @param ultimoAggiunto per i budget ripetuti vale 1 se è l'ultimo budget aggiunto della
	 * stessa serie, 0 in caso contrario
	 * @return id del record inserito
	 */
	public long inserisciSpesaBudget(String voce, String ripetizione, double importo, String valuta, double importo_valprin, long data_inizio, long data_fine, int aggiungere_rimanenza, double spesa_sost, double risparmio, long budget_iniziale, int ultimoAggiunto) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("voce", voce);
		nuovoContact.put("ripetizione", ripetizione);
		nuovoContact.put("importo", importo);
		nuovoContact.put("valuta", valuta);
		nuovoContact.put("importo_valprin", importo_valprin);
		nuovoContact.put("data_inizio", data_inizio);
		nuovoContact.put("data_fine", data_fine);
		nuovoContact.put("aggiungere_rimanenza", aggiungere_rimanenza);
		nuovoContact.put("spesa_sost", spesa_sost);
		nuovoContact.put("risparmio", risparmio);
		nuovoContact.put("budget_iniziale", budget_iniziale);
		nuovoContact.put("ultimo_aggiunto", ultimoAggiunto);
		
		long id;
		synchronized (sDataLock) {
			openModifica();
			id = mioSQLiteDatabase.insert("spese_budget", null, nuovoContact);
			close();
		}
		
		return id;
	}
	
	
	/**
	 * Aggiorna il budget per una specifica voce di spesa.
	 * @param id id del budget da modificare
	 * @param voce voce della spesa come da tabella spese_voci
	 * @param ripetizione tipo di ripetizione (giornaliero, settimanale, bisettimanale, mensile, annuale 
	 * e una_tantum)
	 * @param importo importo budget in valuta
	 * @param valuta simbolo valuta
	 * @param importo_valprin importo budget nella valuta di default
	 * @param data_inizio data di inizio solo per la ripetizione una_tantum nel formato Unix Time, the 
	 * number of seconds since 1970-01-01 00:00:00 UTC (la data iniziale si ricava dalla data odierna). 
	 * Per tutte le altre ripetizioni lasciare in bianco.
	 * @param data_fine data finale solo per la ripetizione una_tantum nel formato Unix Time, the number 
	 * of seconds since 1970-01-01 00:00:00 UTC. Per tutte le altre ripetizioni vale fino ad infinito.
	 * @param aggiungere_rimanenza 0 no, 1 si (aggiungere l'importo risparmiato su questo budget a quello
	 * successivo, per i budget che si ripetono nel tempo
	 * @param spesa_sost spesa sostenuta fino a questo momento su questo budget
	 * @param risparmio differenza tra importo_valprin e spesa_sost (importo risparmiato su questo budget)
	 * @param budget_iniziali per i budget che si ripetono (tutti tranne una_tantum), indica l'id del budget iniziale
	 * da cui questo budget deriva
	 * @param ultimoAggiunto per i budget ripetuti vale 1 se è l'ultimo budget aggiunto della
	 * stessa serie, 0 in caso contrario
	 */
	public void aggiornaSpesaBudget(long id, String voce, String ripetizione, double importo, String valuta, double importo_valprin, long data_inizio, long data_fine, int aggiungere_rimanenza, double spesa_sost, double risparmio, long budget_iniziale, int ultimoAggiunto) {
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("voce", voce);
		aggiornaContact.put("ripetizione", ripetizione);
		aggiornaContact.put("importo", importo);
		aggiornaContact.put("valuta", valuta);
		aggiornaContact.put("importo_valprin", importo_valprin);
		aggiornaContact.put("data_inizio", data_inizio);
		aggiornaContact.put("data_fine", data_fine);
		aggiornaContact.put("aggiungere_rimanenza", aggiungere_rimanenza);
		aggiornaContact.put("spesa_sost", spesa_sost);
		aggiornaContact.put("risparmio", risparmio);
		aggiornaContact.put("budget_iniziale", budget_iniziale);
		aggiornaContact.put("ultimo_aggiunto", ultimoAggiunto);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.update("spese_budget", aggiornaContact, "_id=" + id, null);
			close();
		}
	}
	
	
	/**
	 * Ottiene un Cursor che rappresenta un budget di spesa dalla tabella spese_budget.
	 * 
	 * @param id id del budget di spesa di questa tabella
	 * @return un Cursor che rappresenta il budget di spesa selezionato
	 */
	public Cursor getSpesaBudget(long id) {
		return mioSQLiteDatabase.query("spese_budget", null, "_id=" + id,  null,  null,  null,  null);
	}
	
	
	/**
	 * Elimina un budget di spesa dalla tabella spese_budget.
	 * 
	 * @param id id del budget di spesa da eliminare in questa tabella
	 * @return numero record eliminati
	 */
	public int eliminaSpesaBudget(long id) {
		synchronized (sDataLock) {
			openModifica();
			int budgetEliminati = mioSQLiteDatabase.delete("spese_budget", "_id=" + id, null);
			close();
			
			return budgetEliminati;
		}
	}
	
	
	/**
	 * Per i budget periodici (tutti tranne una_tantum) elimina tutti i budget ripetuti della stessa serie,
	 * ovvero che derivano dal medesimo budget iniziale.
	 * @param budgetIniziale id del record del budget iniziale da cui deriva questa serie; è lo stesso id che
	 * risulta nel campo budget_iniziale di tutti i budget periodici che derivano da questo
	 * @return numero di record eliminati
	 */
	public int eliminaBudgetAnaloghi(Long budgetIniziale) {
		openModifica();
		int budgetEliminati = mioSQLiteDatabase.delete("spese_budget", "budget_iniziale=?", new String[] {budgetIniziale.toString()});
		close();
		
		return budgetEliminati;
	}
	
	
	/**
	 * Per i budget periodici (tutti tranne una_tantum) elimina tutti i budget ripetuti della stessa serie,
	 * ovvero che derivano dal medesimo budget iniziale, fatta eccezione per l'ultimo.
	 * Questo metodo viene tipicamente usato quando si modifica l'ultimo budget e la modifica riguarda voci
	 * rilevanti (tipo il tag, la periodicità, ecc.), per cui l'ultimo budget diventa di fatto un nuovo
	 * budget e tutti quelli precedenti della stessa serie vanno elimanati.
	 * @param budgetIniziale id del record del budget iniziale da cui deriva questa serie; è lo stesso id che
	 * risulta nel campo budget_iniziale di tutti i budget periodici che derivano da questo
	 * @return numero di record eliminati
	 */
	public int eliminaBudgetAnaloghiTranneUltimo(Long budgetIniziale) {
		openModifica();
		int budgetEliminati = mioSQLiteDatabase.delete("spese_budget", "budget_iniziale=? AND ultimo_aggiunto=0", new String[] {budgetIniziale.toString()});
		close();
		
		return budgetEliminati;
		
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato di tutti i budget.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget: voce_budget, importo_valprin 
	 * (il budget), data_inizio, data_fine, spesa_sost, risparmio, budget_iniziale
	 */
	public Cursor getSpeseBudgetElencoCompleto() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_COMPLETO, null);
	}
	
	//da cancellare
	public Cursor getSpeseBudgetElencoCompletoGrezzo() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_COMPLETO_GREZZO, null);
	}
	
	
	/**
	 * Si fornisce in input la data odierna e si ottengono tutti i budget non ancora scaduti alla data
	 * odierna (budget dove oggi <= data_fine).
	 * @param oggi data di oggi in millisecondi
	 * @return Cursor con tutti i budget ad oggi non ancora scaduti (tutti i campi)
	 */
	
	public Cursor getSpeseBudgetElencoNonScaduti(Long oggi) {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_NON_SCADUTI, new String[] {oggi.toString()});
	}
	
	
	/**
	 * Si fornisce in input la data odierna e la stringa di ricerca e si ottengono tutti i budget non ancora scaduti 
	 * alla data odierna (budget dove oggi <= data_fine) e con il campo voce che contiene la stringa cercata.
	 * @param oggi data di oggi in millisecondi
	 * @param ricerca stringa da cercare nel campo voce dei budget per filtrare l'elenco
	 * @return Cursor con tutti i budget ad oggi non ancora scaduti (tutti i campi)
	 */
	
	public Cursor getSpeseBudgetElencoNonScadutiFiltrato(Long oggi, String ricerca) {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_NON_SCADUTI_FILTRATO, new String[] {oggi.toString(), "%" + ricerca + "%"});
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato dei budget annuali.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget annuali: _id, voce_budget, importo_valprin 
	 * (il budget), spesa_sost, risparmio, data_inizio, data_fine
	 */
	public Cursor getSpeseBudgetElencoAnnuale() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_ANNUALE, null);
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato dei budget mensili.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget mensili: voce_budget, importo_valprin 
	 * (il budget), spesa_sost, risparmio data_inizio, data_fine
	 */
	public Cursor getSpeseBudgetElencoMensile() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_MENSILE, null);
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato dei budget bisettimanali.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget bisettimanali: voce_budget, importo_valprin 
	 * (il budget), spesa_sost, risparmio, data_inizio, data_fine
	 */
	public Cursor getSpeseBudgetElencoBisettimanale() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_BISETTIMANALE, null);
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato dei budget settimanali.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget settimanali: voce_budget, importo_valprin 
	 * (il budget), spesa_sost, risparmio, data_inizio, data_fine
	 */
	public Cursor getSpeseBudgetElencoSettimanale() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_SETTIMANALE, null);
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato dei budget giornalieri.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget giornalieri: voce_budget, importo_valprin 
	 * (il budget), spesa_sost, risparmio, data_inizio, data_fine
	 */
	public Cursor getSpeseBudgetElencoGiornaliero() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_GIORNALIERO, null);
	}
	
	
	/**
	 * Restituisce l'elenco dettagliato dei budget una tantum.
	 * 
	 * @return Cursor con i seguenti campi relativi ai budget una tantum: voce_budget, importo_valprin
	 * (il budget), spesa_sost, risparmio, data_inizio, data_fine
	 */
	public Cursor getSpeseBudgetElencoUnaTantum() {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_UNA_TANTUM, null);
	}
	
	
	/**
	 * Per i budget ripetuti (tutti tranne una_tantum) questo metodo restituisce tutti i budget
	 * analoghi, ovvero i budget che derivano automaticamente dallo stesso budget iniziale.
	 * 
	 * @param budgetIniziale id del record del budget iniziale da cui deriva la serie di budget ripetuti
	 * @return Cursor contente tutti i campi della tabella spese_budget
	 */
	public Cursor getSpeseBudgetElencoBudgetAnaloghi(Long budgetIniziale) {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_BUDGET_ANALOGHI, new String[] {budgetIniziale.toString()});
	}
	
	
	/**
	 * Restituisce l'elenco dei budget scaduti o quasi (con risparmio <= 0 o completati per almeno il
	 * 95%).
	 * 
	 * @param oggi data attuale in millisecondi
	 * @return Cursor contenten i campi ripetizione e voce
	 */
	public Cursor getSpeseBudgetElencoBudgetScadutiOQuasi(Long oggi) {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_BUDGET_SCADUTI_O_QUASI, new String[] {oggi.toString()});
	}
	
	
	/**
	 * Restituisce l'elenco dei budget contenenti il tag voce nel campo voce.
	 * 
	 * @param voce tag da ricercare 
	 * @return Cursor con tutti i campi della tabella spese_budget
	 */
	
	public Cursor getSpeseBudgetElencoBudgetContenentiVoce(String voce) {
		return mioSQLiteDatabase.rawQuery(SPESE_BUDGET_ELENCO_BUDGET_CONTENENTI_VOCE, new String[] {"%" + voce + "%"});
	}
	
	
	/**
	 * Quando si elimina una voce di spesa dalle preferenze, chiamando questo metodo si elimina quella voce da tutti
	 * i budget esistenti (perchè quel tag con relative spese non esiste più). Se ci sono budget che hanno come
	 * unica voce la voce eliminata, allora il budget viene cancellato.
	 * 
	 * @param voce voce di spesa eliminata dalle preferenze
	 * @return numero di budget aggiornati/eliminati
	 */
	public int AggiornaBudgetPerEliminazioneVoce(String voce) {
		int budgetAggiornati = 0;
		Cursor curBudget = getSpeseBudgetElencoBudgetContenentiVoce(voce);
		while(curBudget.moveToNext()) {
			long budgetId = curBudget.getLong(curBudget.getColumnIndex("_id"));
			String budgetVoce = curBudget.getString(curBudget.getColumnIndex("voce"));
			if(budgetVoce.equals(voce) || budgetVoce.equals(voce + ",")) {
				eliminaSpesaBudget(budgetId);
				budgetAggiornati++;
			}
			else {
				String budgetRipetizione = curBudget.getString(curBudget.getColumnIndex("ripetizione"));
				double budgetImporto = curBudget.getDouble(curBudget.getColumnIndex("importo"));
				String valuta = curBudget.getString(curBudget.getColumnIndex("valuta"));
				double budgetImportoValprin = curBudget.getDouble(curBudget.getColumnIndex("importo_valprin"));
				long budgetDataInizio = curBudget.getLong(curBudget.getColumnIndex("data_inizio"));
				long budgetDataFine = curBudget.getLong(curBudget.getColumnIndex("data_fine"));
				int budgetAggiungereRimanenza = curBudget.getInt(curBudget.getColumnIndex("aggiungere_rimanenza"));
				double budgetSpesaSost = curBudget.getDouble(curBudget.getColumnIndex("spesa_sost"));
				double budgetRisparmio = curBudget.getDouble(curBudget.getColumnIndex("risparmio"));
				long budgetBudgetIniziale = curBudget.getLong(curBudget.getColumnIndex("budget_iniziale"));
				int budgetUltimoAggiunto = curBudget.getInt(curBudget.getColumnIndex("ultimo_aggiunto"));
				
				StringBuilder sbNuovaVoce = new StringBuilder();
				StringTokenizer st = new StringTokenizer(budgetVoce, ",");
				while(st.hasMoreTokens()) {
					String tag = st.nextToken();
					if(!tag.equals(voce)) {
						sbNuovaVoce.append(tag);
						sbNuovaVoce.append(",");
					}
				}
				aggiornaSpesaBudget(budgetId, sbNuovaVoce.toString(), budgetRipetizione, budgetImporto, valuta, budgetImportoValprin, budgetDataInizio, budgetDataFine, budgetAggiungereRimanenza, budgetSpesaSost, budgetRisparmio, budgetBudgetIniziale, budgetUltimoAggiunto);
				budgetAggiornati++;
			}
		}
		curBudget.close();
		
		return budgetAggiornati;
	}
	
	
	//metodo di debug: restituisce tutti i record (tutti i campi)
	public Cursor getTutteLeSpese() {
		return mioSQLiteDatabase.query("spese_budget", null, null, null, null, null, null);
	}
	
	
	// variabili d'istanza
	private SQLiteDatabase mioSQLiteDatabase;
}

