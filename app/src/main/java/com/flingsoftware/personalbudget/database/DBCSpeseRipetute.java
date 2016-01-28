/**
 * Gestione tabella spese_ripet
 * Funzionamento del database:
 * per ogni aspetto non specificatamente commentato in questa classe fare riferimento alla guida
 * "Siluppare App per Android" - Deitel, da pag. 284.
 * @author Claudio "iClaude" Agostini
 */
package com.flingsoftware.personalbudget.database;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DBCSpeseRipetute {
	
	public DBCSpeseRipetute(Context context) {
		mioDatabaseOpenHelper = new DatabaseOpenHelper(context, DatabaseOpenHelper.NOME_DATABASE, null);
	}
	
	public void openModifica() throws SQLException {
		mioSQLiteDatabase = mioDatabaseOpenHelper.getWritableDatabase();
		mioSQLiteDatabase.execSQL("PRAGMA foreign_keys=ON;"); // bisogna abilitare le foreign keys qui
	}
	
	public void openLettura() throws SQLException {
		mioSQLiteDatabase = mioDatabaseOpenHelper.getReadableDatabase();
	}
	
	public void close() {
		if(mioSQLiteDatabase != null)
			mioSQLiteDatabase.close();
	}
	
	/**
	 * Inserisce una spesa ripetuta nella tabella spese_ripet e restituisce un valore long che rappresenta
	 * l'id nella tabella.
	 * Ad ogni lancio dell'app aggiornare le spese ripetute nella tabella delle spese ripetute. 
	 * Es. al 26/3 aggiornare tutte le spese ripetute sostenute fino a quella data a partire
	 * dall'ultimo aggiornamento, quindi aggiornare il campo aggiornato_a.
	 * Se voglio eliminare tutte le spese ripetute del codice 2 per es. le ricavo facilmente dall'altra 
	 * tabella cercando tutte le corrispondenze di tale codice nell'ultima colonna
	 * 
	 * @param voce voce della spesa che deve essere ripetuta
	 * @param ripetizione tipo di ripetizione (giornaliero, settimanale, bisettimanale, mensile, annuale, giorni_lavorativi, weekend)
	 * @param importo importo spesa
	 * @param valuta simbolo valuta
	 * @param importo_valprin importo nella valuta di default
	 * @param descrizione descrizione della spesa ripetuta
	 * @param data_inizio data di inizio ripetizione spesa nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
	 * @param flag_fine la ripetizione è già finita (1) o è in corso(0)
	 * @param data_fine data di fine ripetizione spesa nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
	 * @param aggiornato_a aggiornamento tabella spese_sost fino a questa data nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
	 * @param conto nome del conto
	 * 
	 * @return id id della riga della tabella dove è stato inserito il nuovo record
	 */
	public long inserisciSpesaRipetuta(String voce, String ripetizione, double importo, String valuta, double importo_valprin, String descrizione, long data_inizio, int flag_fine, long data_fine, long aggiornato_a, String conto) {
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
			id = mioSQLiteDatabase.insert("spese_ripet", null, nuovoContact);
			close();
		}
		
		return id;
	}
	
	/**
	 * Aggiorna una spesa ripetuta nella tabella spese_ripet.
	 * Ad ogni lancio dell'app aggiornare le spese ripetute nella tabella delle spese ripetute. 
	 * Es. al 26/3 aggiornare tutte le spese ripetute sostenute fino a quella data a partire
	 * dall'ultimo aggiornamento, quindi aggiornare il campo aggiornato_a.
	 * Se voglio eliminare tutte le spese ripetute del codice 2 per es. le ricavo facilmente dall'altra 
	 * tabella cercando tutte le corrispondenze di tale codice nell'ultima colonna
	 * 
	 * @param id ide della spesa ripetuta di questa tabella
	 * @param voce voce della spesa che deve essere ripetuta
	 * @param ripetizione tipo di ripetizione (giornaliero, settimanale, bisettimanale, mensile, annuale, giorni_lavorativi, weekend)
	 * @param importo importo spesa
	 * @param valuta simbolo valuta
	 * @param importo_valprin importo nella valuta di default
	 * @param descrizione descrizione della spesa ripetuta
	 * @param data_inizio data di inizio ripetizione spesa nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
	 * @param flag_fine la ripetizione è già finita (1) o è in corso(0)
	 * @param data_fine data di fine ripetizione spesa nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
	 * @param aggiornato_a aggiornamento tabella spese_sost fino a questa data nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
	 * @param conto nome del conto
	 */
	public void aggiornaSpesaRipetuta(long id, String voce, String ripetizione, double importo, String valuta, double importo_valprin, String descrizione, long data_inizio, int flag_fine, long data_fine, long aggiornato_a, String conto) {
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
			mioSQLiteDatabase.update("spese_ripet", aggiornaContact, "_id=" + id, null);
			close();
		}
	}
	
	/**
	 * Ottiene un Cursor che rappresenta una spesa ripetuta dalla tabella spese_ripet.
	 * 
	 * @param id id della spesa ripetuta di questa tabella
	 * @return un Cursor che rappresenta la spesa ripetuta selezionata
	 */
	public Cursor getSpesaRipetuta(long id) {
		return mioSQLiteDatabase.query("spese_ripet", null, "_id=" + id,  null,  null,  null,  null);
	}
	
	/**
	 * Elimina una spesa ripetuta dalla tabella spese_ripet.
	 * 
	 * @param id id della spesa ripetuta da eliminare in questa tabella
	 */
	
	public void eliminaSpesaRipetuta(long id) {
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.delete("spese_ripet", "_id=" + id, null);
			close();
		}
	}
	
	
	/**
	 * Restituisce tutte le spese appartenenti al conto indicato.
	 * @return Cursor con tutti i campi
	 */
	public Cursor getTutteLeSpeseContoX(String conto) {
		return mioSQLiteDatabase.query("spese_ripet", null, "conto = ?", new String[] {conto}, null, null, null);
	}
	
	
	//metodo di debug: restituisce tutti i record (tutti i campi)
	public Cursor getTutteLeSpese() {
		return mioSQLiteDatabase.query("spese_ripet", null, null, null, null, null, null);
	}
		
	
	// variabili d'istanza
	private SQLiteDatabase mioSQLiteDatabase;
	private DatabaseOpenHelper mioDatabaseOpenHelper;
}

