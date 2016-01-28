package com.flingsoftware.personalbudget.database;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DBCConti {

	// variabili d'istanza
	private SQLiteDatabase mioSQLiteDatabase;
	private DatabaseOpenHelper mioDatabaseOpenHelper;
	
	
	public DBCConti(Context context) {
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
	 * Inserisce un nuovo conto nella tabella "conti".
	 * 
	 * @param mioConto oggetto Conto da inserire
	 */
	public void inserisciConto(Conto mioConto) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("conto", mioConto.getConto());
		nuovoContact.put("saldo", mioConto.getSaldo());
		nuovoContact.put("data_saldo", mioConto.getDataSaldo());
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.insert("conti", null, nuovoContact);
			close();
		}

	}
	
	/**
	 * Inserisce un nuovo conto nella tabella "conti". Questo metodo lancia un'eccezione in 
	 * caso di errore.
	 * 
	 * @param mioConto oggetto Conto da inserire
	 */
	public void inserisciContoEccezione(Conto mioConto) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("conto", mioConto.getConto());
		nuovoContact.put("saldo", mioConto.getSaldo());
		nuovoContact.put("data_saldo", mioConto.getDataSaldo());
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.insertOrThrow("conti", null, nuovoContact);
			close();
		}

	}
	
	/**
	 * Aggiorna un conto esistente nella tabella "conti".
	 * 
	 * @param mioConto oggetto Conto da aggiornare con i campi aggiornati
	 */
	public void aggiornaConto(Conto mioConto) {
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("conto", mioConto.getConto());
		aggiornaContact.put("saldo", mioConto.getSaldo());
		aggiornaContact.put("data_saldo", mioConto.getDataSaldo());
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.update("conti", aggiornaContact, "_id=" + mioConto.getId(), null);
			close();
		}
	}
	
	/**
	 * Ottiene un Cursor che rappresenta un conto nella tabella "conti".
	 * 
	 * @param id id del conto di questa tabella
	 * @return un Cursor che rappresenta il conto selezionato
	 */
	public Cursor getConto(long id) {
		return mioSQLiteDatabase.query("conti", null, "_id=" + id,  null,  null,  null,  null);
	}
	
	/**
	 * Si fornisce il nome di un conto e restituisce un Cursor che rappresenta un conto della tabella 
	 * "conti". 
	 * 
	 * @param conto nome del conto da cercare
	 * @return Cursor contenente tutti i record della tabella (tutti i campi)
	 */
	public Cursor getContoConNome(String conto) {
		return mioSQLiteDatabase.query("conti", null, "conto = ?", new String[] {conto}, null, null, null);
	}
	
	/**
	 * Elimina un conto dalla tabella "conti".
	 * 
	 * @param id id del conto da eliminare in questa tabella
	 */
	public void eliminaConto(long id) {
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.delete("conti", "_id=" + id, null);
			close();
		}
	}
	
	/**
	 * Restituisce un Cursor contenente tutti i record della tabella (tutti i campi).
	 * 
	 * @return Cursor contenente tutti i record della tabella (tutti i campi)
	 */
	public Cursor getTuttiIConti() {
		return mioSQLiteDatabase.query("conti", null, null, null, null, null, "conto");
	}
	
	/**
	 * Restituisce un Cursor contenente tutti i record della tabella (tutti i campi).
	 * A differenza del metodo precedente, qua i recordo non sono ordinati per il 
	 * nome del conto.
	 * 
	 * @return Cursor contenente tutti i record della tabella (tutti i campi)
	 */
	public Cursor getTuttiIContiNonOrdinato() {
		return mioSQLiteDatabase.query("conti", null, null, null, null, null, null);
	}
}
