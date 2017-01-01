/*
 * Copyright (c) - Software developed by iClaude.
 */

/**
 * Gestione tabella spese_voci
 * Funzionamento del database:
 * per ogni aspetto non specificatamente commentato in questa classe fare riferimento alla guida
 * "Siluppare App per Android" - Deitel, da pag. 284.
 * @author Claudio "iClaude" Agostini
 */
package com.flingsoftware.personalbudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelperWrapper.sDataLock;
import static com.flingsoftware.personalbudget.database.StringheSQL.SPESE_VOCI_VOCI_CONTENENTI_STRINGA;


public class DBCSpeseVoci extends DBCVociAbs {
	
	public DBCSpeseVoci(Context context) {
		super(context);
	}


	@Override
	public String getNomeTabella() {
		return "spese_voci";
	}
	
	/**
	 * Inserisce una voce di spesa (tag) nella tabella spese_voci.
	 * 
	 * @param voce voce di spesa (tag)
	 */
	public void inserisciVoceSpesa(String voce, int icona) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("voce", voce);
		nuovoContact.put("icona", icona);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.insert("spese_voci", null, nuovoContact);
			close();
		}

	}
	
	/**
	 * Inserisce una voce di spesa (tag) nella tabella spese_voci. Questo metodo lancia
	 * un'eccezione in caso di errore.
	 * 
	 * @param voce voce di spesa (tag)
	 */
	public void inserisciVoceSpesaEccezione(String voce, int icona) {
		ContentValues nuovoContact = new ContentValues();
		nuovoContact.put("voce", voce);
		nuovoContact.put("icona", icona);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.insertOrThrow("spese_voci", null, nuovoContact);
			close();
		}

	}
	
	/**
	 * Aggiorna una voce di spesa (tag) nella tabella spese_voci.
	 * 
	 * @param id id della voce di spesa
	 * @param voce voce di spesa (tag)
	 */
	public void aggiornaVoceSpesa(long id, String voce, int icona) {
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("voce", voce);
		aggiornaContact.put("icona", icona);
		
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.update("spese_voci", aggiornaContact, "_id=" + id, null);
			close();
		}
	}
	
	/**
	 * Ottiene un Cursor che rappresenta una voce di spesa dalla tabella spese_voci.
	 * 
	 * @param id id della voce di spesa di questa tabella
	 * @return un Cursor che rappresenta la voce di spesa selezionata
	 */
	public Cursor getVoceSpesa(long id) {
		return mioSQLiteDatabase.query("spese_voci", null, "_id=" + id,  null,  null,  null,  null);
	}
	
	/**
	 * Elimina una voce di spesa dalla tabella spese_voci.
	 * 
	 * @param id id della voce di spesa da eliminare in questa tabella
	 */
	public void eliminaVoceSpesa(long id) {
		synchronized (sDataLock) {
			openModifica();
			mioSQLiteDatabase.delete("spese_voci", "_id=" + id, null);
			close();
		}
	}
	
	/**
	 * Restituisce un Cursor contenente tutti i record della tabella (tutti i campi).
	 * 
	 * @return Cursor contenente tutti i record della tabella (tutti i campi)
	 */
	public Cursor getTutteLeVoci() {
		return mioSQLiteDatabase.query("spese_voci", new String[] {"_id",  "voce", "icona"}, null, null, null, null, "voce");
	}
	
	/*
	 * Analogo al metodo precedente, ma esclude la voce che identifica i trasferimenti. Passare come
	 * argomento la voce nella lingua corrente dell'app.
	 */
	public Cursor getTutteLeVociNoTrasf(String trasf) {
		return mioSQLiteDatabase.query("spese_voci", new String[] {"_id",  "voce", "icona"}, "voce <> ?", new String[] {trasf}, null, null, "voce");
	}


	/**
	 * Restituisce un Cursor contenente tutte le voci della tabella spese_voci che contengono la stringa
	 * specificata come parametro.
	 * 
	 * @param stringa verranno restituite tutte le voci che contengono questa stringa
	 * @return Cursor con i campi _id e voce
	 */
	public Cursor getVociContenentiStringa (String stringa) {
			String param = "%" + stringa + "%";
			return mioSQLiteDatabase.rawQuery(SPESE_VOCI_VOCI_CONTENENTI_STRINGA, new String[] {param});
	}

}


