/**
 * Gestione tabella entrate_voci
 * Funzionamento del database:
 * per ogni aspetto non specificatamente commentato in questa classe fare riferimento alla guida
 * "Siluppare App per Android" - Deitel, da pag. 284.
 * @author Claudio "iClaude" Agostini
 */
package com.flingsoftware.personalbudget.database;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;
import static com.flingsoftware.personalbudget.database.StringheSQL.ENTRATE_VOCI_VOCI_CONTENENTI_STRINGA;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DBCEntrateVoci {
	
	public DBCEntrateVoci(Context context) {
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
	 * Inserisce una voce di entrata (tag) nella tabella entrate_voci.
	 * 
	 * @param voce voce di entrata (tag)
	 */
	public void inserisciVoceEntrata(String voce, int icona) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("voce", voce);
		nuovoContact.put("icona", icona);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.insert("entrate_voci", null, nuovoContact);
			close();
		}
	}
	
	
	/**
	 * Inserisce una voce di entrata (tag) nella tabella entrate_voci. Questo metodo lancia
	 * un'eccezione in caso di fallimento (a differenza del precedente).
	 * 
	 * @param voce voce di entrata (tag)
	 */
	public void inserisciVoceEntrataEccezione(String voce, int icona) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("voce", voce);
		nuovoContact.put("icona", icona);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.insertOrThrow("entrate_voci", null, nuovoContact);
			close();
		}
	}
	
	
	/**
	 * Aggiorna una voce di entrata (tag) nella tabella entrate_voci.
	 * 
	 * @param id id della voce di entrata
	 * @param voce voce di entrata (tag)
	 */
	public void aggiornaVoceEntrata(long id, String voce, int icona) {
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("voce", voce);
		aggiornaContact.put("icona", icona);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.update("entrate_voci", aggiornaContact, "_id=" + id, null);
			close();
		}
	}
	
	
	/**
	 * Ottiene un Cursor che rappresenta una voce di entrata dalla tabella entrate_voci.
	 * 
	 * @param id id della voce di entrata di questa tabella
	 * @return un Cursor che rappresenta la voce di entrata selezionata
	 */
	public Cursor getVoceEntrata(long id) {
		return mioSQLiteDatabase.query("entrate_voci", null, "_id=" + id,  null,  null,  null,  null);
	}
	
	
	/**
	 * Elimina una voce di entrata dalla tabella entrate_voci.
	 * 
	 * @param id id della voce di entrata da eliminare in questa tabella
	 */
	public void eliminaVoceEntrata(long id) {
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.delete("entrate_voci", "_id=" + id, null);
			close();
		}
	}
	
	
	/**
	 * Restituisce un Cursor contenente tutti i record della tabella (tutti i campi).
	 * 
	 * @return Cursor contenente tutti i record della tabella (tutti i campi)
	 */
	public Cursor getTutteLeVoci() {
		return mioSQLiteDatabase.query("entrate_voci", new String[] {"_id",  "voce", "icona"}, null, null, null, null, "voce");
	}

	/*
	 * Analogo al metodo precedente, ma esclude la voce che identifica i trasferimenti. Passare come
	 * argomento la voce nella lingua corrente dell'app.
	 */
	public Cursor getTutteLeVociNoTrasf(String trasf) {
		return mioSQLiteDatabase.query("entrate_voci", new String[] {"_id",  "voce", "icona"}, "voce <> ?", new String[] {trasf}, null, null, "voce");
	}
	
	/**
	 * Restituisce un Cursor contenente tutti i record della tabella (tutti i campi) che
	 * contengono la stringa di ricerca nel campo voce.
	 * 
	 * @param ricerca stringa da cercare nel campo voce
	 * @return Cursor contenente tutti i record della tabella (tutti i campi)
	 */
	public Cursor getTutteLeVociFiltrato(String ricerca) {
		return mioSQLiteDatabase.query("entrate_voci", new String[] {"_id",  "voce", "icona"}, "voce LIKE ?", new String[] {"%" + ricerca + "%"}, null, null, "voce");
	}
	
	
	/**
	 * Restituisce un Cursor contenente tutte le voci della tabella entrate_voci che contengono la stringa
	 * specificata come parametro.
	 * 
	 * @param stringa verranno restituite tutte le voci che contengono questa stringa
	 * @return Cursor con i campi _id e voce
	 */
	public Cursor getVociContenentiStringa (String stringa) {
			String param = "%" + stringa + "%";
			return mioSQLiteDatabase.rawQuery(ENTRATE_VOCI_VOCI_CONTENENTI_STRINGA, new String[] {param});
	}
		
	
	// variabili d'istanza
	private SQLiteDatabase mioSQLiteDatabase;
	private DatabaseOpenHelper mioDatabaseOpenHelper;
}


