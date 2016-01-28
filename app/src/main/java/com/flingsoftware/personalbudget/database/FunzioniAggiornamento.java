/**
 * Questa classe contiene funzioni di aggiornamento automatico del database, tipicamente da lanciare ad
 * ogni avvio dell'applicazione (inserimento di spese ripetute, entrate ripetute, ecc.).
 */

package com.flingsoftware.personalbudget.database;

//stringhe SQL usate in questa classe
import static com.flingsoftware.personalbudget.database.StringheSQL.*;

import com.flingsoftware.personalbudget.app.FunzioniComuni;
import com.flingsoftware.personalbudget.oggetti.SpesaEntrata;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;


public class FunzioniAggiornamento {
	
	public FunzioniAggiornamento(Context context) {
		this.mioContext = context;
		mioDatabaseOpenHelper = new DatabaseOpenHelper(mioContext, DatabaseOpenHelper.NOME_DATABASE, null);
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
	 * Partendo dai dati contenuti nella tabella spese_ripet si aggiorna la tabella spese_sost inserendo
	 * le spese effettivamente sostenute di volta in volta.
	 * 
	 * @return numero di spese inserite nella tabella spese_sost, -1 in caso di errore
	 */
	public int aggiornaSpeseRipetute() {
		int speseInserite = 0;
		// calcolo la data attuale in millisecondi
		oggi = FunzioniComuni.getDataAttuale();
		
		openModifica();
		
		mioSQLiteDatabase.beginTransaction();
		try {
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_GIORNALIERE, GregorianCalendar.DAY_OF_YEAR, 1);
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_SETTIMANALI, GregorianCalendar.WEEK_OF_YEAR, 1);
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_BISETTIMANALI, GregorianCalendar.WEEK_OF_YEAR, 2);
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_MENSILI, GregorianCalendar.MONTH, 1);
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_ANNUALI, GregorianCalendar.YEAR, 1);
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_GIORNI_LAVORATIVI, GregorianCalendar.DAY_OF_YEAR, 1);
			speseInserite += aggiornaSpese(ESTRAI_SPESERIPETUTE_WEEKEND, GregorianCalendar.DAY_OF_YEAR, 1);
			
			mioSQLiteDatabase.setTransactionSuccessful();
		}
		catch(SQLiteException exc) {
			exc.printStackTrace();
			return -1;
		}
		finally {
			mioSQLiteDatabase.endTransaction();
		}
		
		close();
		
		return speseInserite;
	}
	
	/**
	 * Aggiorna le spese ripetute giornaliere, settimanali, bisettimanali, mensili, annuali, giorni
	 * lavorativi e weekend.
	 * 
	 * @param campo campo GregorianCalendar appropriato per l'aggiornamento (DAY_OF_YEAR, WEEK_OF_YEAR, MONTH e YEAR)
	 * @param incremento incremento da applicare al campo da aggiornare (es. +1 per incrementare il giorno, settimana, mese di 1; +2 applicato al campo WEEK_OF_YEAR aggiorna le ripetizioni bisettimanali)
	 * 
	 * @return numero di spese inserite nella tabella spese_sost
	 */
	private int aggiornaSpese(String query, int campo, int incremento) {	
		int speseInserite = 0;
		
		// ricavo un Cursor con tutte le spese ripetute da aggiornare
		Cursor speseRipetuteDaInserire = mioSQLiteDatabase.rawQuery(query, new String[] {oggi.toString()});
		if (speseRipetuteDaInserire == null) return speseInserite;
		
		
		/* itero sul Cursor aggiornando la tabella spese_sost (non uso la funzione di inserimento
		 * della classe DBCSpeseSostenute perch� non voglio aprire e chiudere il database per ogni
		 * spesa che inserisco: lo faccio una volta sola)
		 */
		
		speseRipetuteDaInserire.moveToFirst();
		while(!speseRipetuteDaInserire.isAfterLast()) {
			// ricavo i campi da inserire nella tabella spese_sost ricavandoli da spese_ripet
			long _id = speseRipetuteDaInserire.getLong(speseRipetuteDaInserire.getColumnIndex("_id"));
			String voce = speseRipetuteDaInserire.getString(speseRipetuteDaInserire.getColumnIndex("voce"));
			String ripetizione = speseRipetuteDaInserire.getString(speseRipetuteDaInserire.getColumnIndex("ripetizione"));
			double importo = speseRipetuteDaInserire.getDouble(speseRipetuteDaInserire.getColumnIndex("importo"));
			String valuta = speseRipetuteDaInserire.getString(speseRipetuteDaInserire.getColumnIndex("valuta"));
			double importo_valprin = speseRipetuteDaInserire.getDouble(speseRipetuteDaInserire.getColumnIndex("importo_valprin"));
			String descrizione = speseRipetuteDaInserire.getString(speseRipetuteDaInserire.getColumnIndex("descrizione"));
			long data_inizio = speseRipetuteDaInserire.getLong(speseRipetuteDaInserire.getColumnIndex("data_inizio"));
			long flag_fine = speseRipetuteDaInserire.getLong(speseRipetuteDaInserire.getColumnIndex("flag_fine"));
			long data_fine = speseRipetuteDaInserire.getLong(speseRipetuteDaInserire.getColumnIndex("data_fine"));
			long aggiornato_a = speseRipetuteDaInserire.getLong(speseRipetuteDaInserire.getColumnIndex("aggiornato_a"));
			String conto = speseRipetuteDaInserire.getString(speseRipetuteDaInserire.getColumnIndex("conto"));
			
			// calcolo l'intervallo delle spese da inserire come dati GregorianCalendar
			GregorianCalendar dataInizialeGreg = new GregorianCalendar();
			dataInizialeGreg.setTimeInMillis(aggiornato_a);
			dataInizialeGreg.add(campo, incremento);

			long dataFinale = Math.min(oggi, data_fine);
			GregorianCalendar dataFinaleGreg = new GregorianCalendar();
			dataFinaleGreg.setTimeInMillis(dataFinale);
			
			// aggiornamento tabella spese_sost
			boolean inseritaAlmenoUnaSpesa = false;
			GregorianCalendar data = new GregorianCalendar();
			for(data=dataInizialeGreg; data.compareTo(dataFinaleGreg) <= 0; data.add(campo, incremento)) {
				// nelle ripetizioni giorni lavorativi non aggiorno per sabato e domenica
				if(query.equals(ESTRAI_SPESERIPETUTE_GIORNI_LAVORATIVI) && (data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)) {
					continue;
				}
				
				// nelle ripetizioni weekend salto se non � sabato o domenica
				if(query.equals(ESTRAI_SPESERIPETUTE_WEEKEND) && (data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.TUESDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.WEDNESDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY)) {
					continue;
				}
				
				ContentValues nuovoContact = new ContentValues();
				long dataMillis = data.getTimeInMillis();
				nuovoContact.put("data", dataMillis);
				nuovoContact.put("voce", voce);
				nuovoContact.put("importo", importo);
				nuovoContact.put("valuta", valuta);
				nuovoContact.put("importo_valprin", importo_valprin);
				nuovoContact.put("descrizione", descrizione);
				nuovoContact.put("ripetizione_id", _id);
				nuovoContact.put("conto", conto);
				nuovoContact.put("favorite", 0);
				
				mioSQLiteDatabase.insert("spese_sost", null, nuovoContact);
				aggiornato_a = dataMillis;
				inseritaAlmenoUnaSpesa = true;
				speseInserite++;
				
				//aggiorno la tabella spese_budget a seguito dell'inserimento della spesa
				aggiornaSpeseSostTabellaBudget(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + voce + "%", Long.valueOf(dataMillis).toString(), Long.valueOf(dataMillis).toString());
			}
			
			// aggiornamento tabella spese_ripet
			if(aggiornato_a >= data_fine) {
				flag_fine = 1;
			}
			
			if(inseritaAlmenoUnaSpesa) { // se non ho inserito almeno una spesa ripetuta nella tabella spese_sost non occorre aggiornare il record di spese_ripet perch� resta =
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
			
				mioSQLiteDatabase.update("spese_ripet", aggiornaContact, "_id=" + _id, null);
			}
			
			speseRipetuteDaInserire.moveToNext();
		}
		speseRipetuteDaInserire.close();
		
		return speseInserite;
	}
	
	/*
	 * Ogni volta che inserisco una spesa ripetuta chiamo questo metodo per aggiornare il campo
	 * spese_sost e risparmio di tutti i budget interessati da quella spesa.
	 */
	private void aggiornaSpeseSostTabellaBudget(String query, String... args) {
		Cursor curBudgetNonScaduti = mioSQLiteDatabase.rawQuery(query, args);
		
		while(curBudgetNonScaduti.moveToNext()) {
			long id = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("_id"));
			String voce = curBudgetNonScaduti.getString(curBudgetNonScaduti.getColumnIndex("voce"));
			String ripetizione = curBudgetNonScaduti.getString(curBudgetNonScaduti.getColumnIndex("ripetizione"));
			double importo = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("importo"));
			String valuta = curBudgetNonScaduti.getString(curBudgetNonScaduti.getColumnIndex("valuta"));
			double importoValprin = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("importo_valprin"));
			long dataInizio = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("data_inizio"));
			long dataFine = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("data_fine"));
			int aggiungereRimanenza = curBudgetNonScaduti.getInt(curBudgetNonScaduti.getColumnIndex("aggiungere_rimanenza"));
			double spesaSost = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("spesa_sost"));
			double risparmio = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("risparmio"));
			long budgetIniziale = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("budget_iniziale"));
			aggiornaBudget(id, voce, ripetizione, importo, valuta, importoValprin, dataInizio, dataFine, aggiungereRimanenza, spesaSost, risparmio, budgetIniziale);
		}
		curBudgetNonScaduti.close();
	}
	
	/**
	 * Partendo dai dati contenuti nella tabella entrate_ripet si aggiorna la tabella entrate_inc inserendo
	 * le entrate effettivamente incassate di volta in volta.
	 * 
	 * @return numero di spese inserite nella tabella entrate_inc, -1 in caso di errore
	 */
	public int aggiornaEntrateRipetute() {
		int entrateInserite = 0;
		// calcolo la data attuale in millisecondi
		oggi = FunzioniComuni.getDataAttuale();
		
		openModifica();
		
		mioSQLiteDatabase.beginTransaction();
		try {
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_GIORNALIERE, GregorianCalendar.DAY_OF_YEAR, 1);
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_SETTIMANALI, GregorianCalendar.WEEK_OF_YEAR, 1);
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_BISETTIMANALI, GregorianCalendar.WEEK_OF_YEAR, 2);
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_MENSILI, GregorianCalendar.MONTH, 1);
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_ANNUALI, GregorianCalendar.YEAR, 1);
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_GIORNI_LAVORATIVI, GregorianCalendar.DAY_OF_YEAR, 1);
			entrateInserite += aggiornaEntrate(ESTRAI_ENTRATERIPETUTE_WEEKEND, GregorianCalendar.DAY_OF_YEAR, 1);
			
			mioSQLiteDatabase.setTransactionSuccessful();
		}
		catch(SQLiteException exc) {
			exc.printStackTrace();
			return -1;
		}
		finally {
			mioSQLiteDatabase.endTransaction();
		}
		
		close();
		
		return entrateInserite;
	}
	
	/**
	 * Aggiorna le entrate ripetute giornaliere, settimanali, bisettimanali, mensili, annuali, giorni
	 * lavorativi e weekend.
	 * 
	 * @param campo campo GregorianCalendar appropriato per l'aggiornamento (DAY_OF_YEAR, WEEK_OF_YEAR, MONTH e YEAR)
	 * @param incremento incremento da applicare al campo da aggiornare (es. +1 per incrementare il giorno, settimana, mese di 1; +2 applicato al campo WEEK_OF_YEAR aggiorna le ripetizioni bisettimanali)
	 * 
	 * @return numero di entrate inserite nella tabella entrate_inc
	 */
	private int aggiornaEntrate(String query, int campo, int incremento) {	
		int entrateInserite = 0;
		
		// ricavo un Cursor con tutte le entrate ripetute da aggiornare
		Cursor entrateRipetuteDaInserire = mioSQLiteDatabase.rawQuery(query, new String[] {oggi.toString()});
		if (entrateRipetuteDaInserire == null) return entrateInserite;
		
		
		/* itero sul Cursor aggiornando la tabella entrate_inc (non uso la funzione di inserimento
		 * della classe DBCEntrateIncassate perch� non voglio aprire e chiudere il database per ogni
		 * entrata che inserisco: lo faccio una volta sola)
		 */
		
		while(entrateRipetuteDaInserire.moveToNext()) {
			// ricavo i campi da inserire nella tabella entrate_inc ricavandoli da entrate_ripet
			long _id = entrateRipetuteDaInserire.getLong(entrateRipetuteDaInserire.getColumnIndex("_id"));
			String voce = entrateRipetuteDaInserire.getString(entrateRipetuteDaInserire.getColumnIndex("voce"));
			String ripetizione = entrateRipetuteDaInserire.getString(entrateRipetuteDaInserire.getColumnIndex("ripetizione"));
			double importo = entrateRipetuteDaInserire.getDouble(entrateRipetuteDaInserire.getColumnIndex("importo"));
			String valuta = entrateRipetuteDaInserire.getString(entrateRipetuteDaInserire.getColumnIndex("valuta"));
			double importo_valprin = entrateRipetuteDaInserire.getDouble(entrateRipetuteDaInserire.getColumnIndex("importo_valprin"));
			String descrizione = entrateRipetuteDaInserire.getString(entrateRipetuteDaInserire.getColumnIndex("descrizione"));
			long data_inizio = entrateRipetuteDaInserire.getLong(entrateRipetuteDaInserire.getColumnIndex("data_inizio"));
			long flag_fine = entrateRipetuteDaInserire.getLong(entrateRipetuteDaInserire.getColumnIndex("flag_fine"));
			long data_fine = entrateRipetuteDaInserire.getLong(entrateRipetuteDaInserire.getColumnIndex("data_fine"));
			long aggiornato_a = entrateRipetuteDaInserire.getLong(entrateRipetuteDaInserire.getColumnIndex("aggiornato_a"));
			String conto = entrateRipetuteDaInserire.getString(entrateRipetuteDaInserire.getColumnIndex("conto"));
			
			// calcolo l'intervallo delle entrate da inserire come dati GregorianCalendar
			GregorianCalendar dataInizialeGreg = new GregorianCalendar();
			dataInizialeGreg.setTimeInMillis(aggiornato_a);
			dataInizialeGreg.add(campo, incremento);

			long dataFinale = Math.min(oggi, data_fine);
			GregorianCalendar dataFinaleGreg = new GregorianCalendar();
			dataFinaleGreg.setTimeInMillis(dataFinale);
			
			// aggiornamento tabella entrate_inc
			boolean inseritaAlmenoUnaEntrata = false;
			GregorianCalendar data = new GregorianCalendar();
			for(data=dataInizialeGreg; data.compareTo(dataFinaleGreg) <= 0; data.add(campo, incremento)) {
				// nelle ripetizioni giorni lavorativi non aggiorno per sabato e domenica
				if(query.equals(ESTRAI_ENTRATERIPETUTE_GIORNI_LAVORATIVI) && (data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)) {
					continue;
				}
				
				// nelle ripetizioni weekend salto se non � sabato o domenica
				if(query.equals(ESTRAI_ENTRATERIPETUTE_WEEKEND) && (data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.TUESDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.WEDNESDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY)) {
					continue;
				}
				
				ContentValues nuovoContact = new ContentValues();
				long dataMillis = data.getTimeInMillis();
				nuovoContact.put("data", dataMillis);
				nuovoContact.put("voce", voce);
				nuovoContact.put("importo", importo);
				nuovoContact.put("valuta", valuta);
				nuovoContact.put("importo_valprin", importo_valprin);
				nuovoContact.put("descrizione", descrizione);
				nuovoContact.put("ripetizione_id", _id);
				nuovoContact.put("conto", conto);
				nuovoContact.put("favorite", 0);
				
				mioSQLiteDatabase.insert("entrate_inc", null, nuovoContact);
				aggiornato_a = dataMillis;
				inseritaAlmenoUnaEntrata = true;
				entrateInserite++;
			}
			
			// aggiornamento tabella entrate_ripet
			if(aggiornato_a >= data_fine) {
				flag_fine = 1;
			}
			
			if(inseritaAlmenoUnaEntrata) { // se non ho inserito almeno una entrata ripetuta nella tabella entrate_inc non occorre aggiornare il record di entrate_ripet perch� resta =
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
			
				mioSQLiteDatabase.update("entrate_ripet", aggiornaContact, "_id=" + _id, null);
			}
		}
		entrateRipetuteDaInserire.close();
		
		return entrateInserite;
	}

    /**
     * Funzione per l'inserimento di spese ed entrate multiple (tutte le entrate ripetute sono
     * inserite subito nel database.
     * @param tipoVoce tipo di voce inserita ("spesa" o "entrata"
     * @param voce voce contenenete le informazioni sull'entrata da inserire
     * @param ripetizione tipologia di ripetizione
     * @param dataFine data finale della ripetizione
     * @return numero di voci inserite, -1 in caso di errore
     */
    public int inserimentoMultiplo(String tipoVoce, SpesaEntrata voce, String ripetizione, long dataFine) {
        int vociInserite = 0;

        openModifica();

        mioSQLiteDatabase.beginTransaction();
        try {
            vociInserite += inserisciVoceMultipla(tipoVoce, voce, ripetizione, dataFine);

            mioSQLiteDatabase.setTransactionSuccessful();
        }
        catch(SQLiteException exc) {
            exc.printStackTrace();
            return -1;
        }
        finally {
            mioSQLiteDatabase.endTransaction();
        }

        close();

        return vociInserite;
    }

    private int inserisciVoceMultipla(String tipoVoce, SpesaEntrata voce, String ripetizione, long dataFine) {
        int vociInserite = 0;

        GregorianCalendar dataInizialeGreg = new GregorianCalendar();
        dataInizialeGreg.setTimeInMillis(voce.getData());
        GregorianCalendar dataFinaleGreg = new GregorianCalendar();
        dataFinaleGreg.setTimeInMillis(dataFine);
        int campo = calcolaCampo(ripetizione);
        int incremento = calcolaIncremento(ripetizione);

        GregorianCalendar data = new GregorianCalendar();
        for(data=dataInizialeGreg; data.compareTo(dataFinaleGreg) <= 0; data.add(campo, incremento)) {
            // nelle ripetizioni giorni lavorativi non aggiorno per sabato e domenica
            if (ripetizione.equals("giorni_lavorativi") && (data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)) {
                continue;
            }

            // nelle ripetizioni weekend salto se non � sabato o domenica
            if (ripetizione.equals("weekend") && (data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.MONDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.TUESDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.WEDNESDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.THURSDAY || data.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY)) {
                continue;
            }

            ContentValues nuovoContact = new ContentValues();
            long dataMillis = data.getTimeInMillis();
            nuovoContact.put("data", dataMillis);
            nuovoContact.put("voce", voce.getVoce());
            nuovoContact.put("importo", voce.getImporto());
            nuovoContact.put("valuta", voce.getValuta());
            nuovoContact.put("importo_valprin", voce.getImportoValprin());
            nuovoContact.put("descrizione", voce.getDescrizione());
            nuovoContact.put("ripetizione_id", voce.getRipetizioneId());
            nuovoContact.put("conto", voce.getConto());
			nuovoContact.put("favorite", 0);

            if(tipoVoce.equals(InserimentoMultiploIntentService.TIPO_VOCE_ENTRATA)) {
                mioSQLiteDatabase.insert("entrate_inc", null, nuovoContact);
            } else if(tipoVoce.equals(InserimentoMultiploIntentService.TIPO_VOCE_SPESA)) {
                mioSQLiteDatabase.insert("spese_sost", null, nuovoContact);
                //aggiorno la tabella spese_budget a seguito dell'inserimento della spesa
                aggiornaSpeseSostTabellaBudget(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + voce.getVoce() + "%", Long.valueOf(dataMillis).toString(), Long.valueOf(dataMillis).toString());
            }
            vociInserite++;
        }

        return vociInserite;
    }

    private int calcolaCampo(String ripetizione) {
        int campo;
        switch(ripetizione) {
            case "giornaliero":
                campo = Calendar.DAY_OF_YEAR;
                break;
            case "settimanale":
                campo = Calendar.WEEK_OF_YEAR;
                break;
            case "bisettimanale":
                campo = Calendar.WEEK_OF_YEAR;
                break;
            case "mensile":
                campo = Calendar.MONTH;
                break;
            case "annuale":
                campo = Calendar.YEAR;
                break;
            case "giorni_lavorativi":
                campo = Calendar.DAY_OF_YEAR;
                break;
            case "weekend":
                campo = Calendar.DAY_OF_YEAR;
                break;
            default:
                campo = Calendar.DAY_OF_YEAR;
                break;
        }

        return campo;
    }

    private int calcolaIncremento(String ripetizione) {
        int incremento = 0;
        if(ripetizione.equals("bisettimanale")) {
            incremento = 2;
        }
        else {
            incremento = 1;
        }

        return incremento;
    }


    /**
	 * Questo metodo ricava tutti i budget della tabella spese_budget che interessano, da 
	 * specificare fornendo la query SQL e gli argomenti della stessa, calcola
	 * le spese sostenute su quel budget ed aggiorna il campo spese_sost della tabella spese_budget.
	 * @param query query usata per selezionare i budget da modificare
	 * @return numero di record aggiornati nella tabella spese_budget, -1 in caso di errore
	 */
	public int aggiornaTabBudgetSpeseSost(String query, String... args) {
		int recordAggiornati = 0;
		
		openModifica();
		
		mioSQLiteDatabase.beginTransaction();
		try {
			Cursor curBudgetNonScaduti = mioSQLiteDatabase.rawQuery(query, args);
			while(curBudgetNonScaduti.moveToNext()) {
				long id = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("_id"));
				String voce = curBudgetNonScaduti.getString(curBudgetNonScaduti.getColumnIndex("voce"));
				String ripetizione = curBudgetNonScaduti.getString(curBudgetNonScaduti.getColumnIndex("ripetizione"));
				double importo = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("importo"));
				String valuta = curBudgetNonScaduti.getString(curBudgetNonScaduti.getColumnIndex("valuta"));
				double importoValprin = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("importo_valprin"));
				long dataInizio = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("data_inizio"));
				long dataFine = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("data_fine"));
				int aggiungereRimanenza = curBudgetNonScaduti.getInt(curBudgetNonScaduti.getColumnIndex("aggiungere_rimanenza"));
				double spesaSost = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("spesa_sost"));
				double risparmio = curBudgetNonScaduti.getDouble(curBudgetNonScaduti.getColumnIndex("risparmio"));
				long budgetIniziale = curBudgetNonScaduti.getLong(curBudgetNonScaduti.getColumnIndex("budget_iniziale"));
				recordAggiornati += aggiornaBudget(id, voce, ripetizione, importo, valuta, importoValprin, dataInizio, dataFine, aggiungereRimanenza, spesaSost, risparmio, budgetIniziale);
			}
			
			curBudgetNonScaduti.close();
			mioSQLiteDatabase.setTransactionSuccessful();
		}
		catch(SQLiteException exc) {
			exc.printStackTrace();
			return -1;
		}
		finally {
			mioSQLiteDatabase.endTransaction();
		}
		
		close();
		
		return recordAggiornati;
	}
	
	/*
	 * Questo metodo riceve in input tutti i campi di un dato budget della tabella spese_budget,
	 * calcola il totale spese sostenute per quel budget e aggiorna il record.
	 * Restituisce il numero di record modificati.
	 */
	private int aggiornaBudget(long id, String voce, String ripetizione, double importo, String valuta, double importoValprin, Long dataInizio, Long dataFine, int aggiungereRimanenza, double spesaSost, double risparmio, long budgetIniziale) {
		double spesaTot = 0.0;
		
		//calcolo il totale spese sostenute per questo budget
		try {
			StringTokenizer st = new StringTokenizer(voce, ",");
			while(st.hasMoreTokens()) {
				String voceSpesa = st.nextToken();
				Cursor curSomma = mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_TOTALE, new String[] {dataInizio.toString(), dataFine.toString(), voceSpesa});
				curSomma.moveToFirst();
				spesaTot += curSomma.getDouble(curSomma.getColumnIndex("totale_spesa"));
				curSomma.close();
			}	
		}
		catch (SQLiteException exc) {
			exc.printStackTrace();
			return 0;
		}
		
		//aggiorno il record del budget
		ContentValues aggiornaContact = new ContentValues();
		aggiornaContact.put("voce", voce);
		aggiornaContact.put("ripetizione", ripetizione);
		aggiornaContact.put("importo", importo);
		aggiornaContact.put("valuta", valuta);
		aggiornaContact.put("importo_valprin", importoValprin);
		aggiornaContact.put("data_inizio", dataInizio);
		aggiornaContact.put("data_fine", dataFine);
		aggiornaContact.put("aggiungere_rimanenza", aggiungereRimanenza);
		aggiornaContact.put("spesa_sost", spesaTot);	
		aggiornaContact.put("risparmio", importoValprin - spesaTot);
		aggiornaContact.put("budget_iniziale", budgetIniziale);
		mioSQLiteDatabase.update("spese_budget", aggiornaContact, "_id=" + id, null);
		
		return 1;
	}
	
	/**
	 * Ad ogni lancio dell'app si prendono tutti i budget diversi da una_tantum scaduti e per ognuno
	 * si inseriscono nuovi budget (uno o pi� di uno, a seconda di quando viene lanciata l'app 
	 * dall'ultima volta), aggiungendo eventualmente l'importo risparmiato sul budget precedente.
	 * @return numero di record aggiornati nella tabella spese_budget, -1 in caso di errore
	 */
	public int aggiornaTabBudgetNuoviBudget() {
		//variabili utilizzate nel metodo
		int budgetAggiunti = 0;
		GregorianCalendar dataInizioGreg = new GregorianCalendar();
		GregorianCalendar dataFineGreg = new GregorianCalendar();
		int campoData = 0;
		int moltiplicatore = 1;
		
		openModifica();
		mioSQLiteDatabase.beginTransaction();
		try {		
			Long oggi = FunzioniComuni.getDataAttuale();
			long idBudgetAggiunto;
			double spesaTot;
			
			Cursor curBudgetScaduti = mioSQLiteDatabase.rawQuery(ESTRAI_BUDGET_PERIODICI_ULTIMI_SCADUTI, new String[] {oggi.toString()});
			while(curBudgetScaduti.moveToNext()) {
				//ricavo i campi del budget
				long id = curBudgetScaduti.getLong(curBudgetScaduti.getColumnIndex("_id"));
				String voce = curBudgetScaduti.getString(curBudgetScaduti.getColumnIndex("voce"));
				String ripetizione = curBudgetScaduti.getString(curBudgetScaduti.getColumnIndex("ripetizione"));
				double importo = curBudgetScaduti.getDouble(curBudgetScaduti.getColumnIndex("importo"));
				String valuta = curBudgetScaduti.getString(curBudgetScaduti.getColumnIndex("valuta"));
				double importoValprin = curBudgetScaduti.getDouble(curBudgetScaduti.getColumnIndex("importo_valprin"));
				long dataInizio = curBudgetScaduti.getLong(curBudgetScaduti.getColumnIndex("data_inizio"));
				long dataFine = curBudgetScaduti.getLong(curBudgetScaduti.getColumnIndex("data_fine"));
				int aggiungereRimanenza = curBudgetScaduti.getInt(curBudgetScaduti.getColumnIndex("aggiungere_rimanenza"));
				double spesaSost = curBudgetScaduti.getDouble(curBudgetScaduti.getColumnIndex("spesa_sost"));
				double risparmioOrig = curBudgetScaduti.getDouble(curBudgetScaduti.getColumnIndex("risparmio"));
				long budgetIniziale = curBudgetScaduti.getLong(curBudgetScaduti.getColumnIndex("budget_iniziale"));
				
				//imposto ultimoAggiornato su 0 e aggiorno il record
				ContentValues aggiornaContact = new ContentValues();
				aggiornaContact.put("voce", voce);
				aggiornaContact.put("ripetizione", ripetizione);
				aggiornaContact.put("importo", importo);
				aggiornaContact.put("valuta", valuta);
				aggiornaContact.put("importo_valprin", importoValprin);
				aggiornaContact.put("data_inizio", dataInizio);
				aggiornaContact.put("data_fine", dataFine);
				aggiornaContact.put("aggiungere_rimanenza", aggiungereRimanenza);
				aggiornaContact.put("spesa_sost", spesaSost);
				aggiornaContact.put("risparmio", risparmioOrig);
				aggiornaContact.put("budget_iniziale", budgetIniziale);
				aggiornaContact.put("ultimo_aggiunto", 0);
				mioSQLiteDatabase.update("spese_budget", aggiornaContact, "_id=" + id, null);
				
				//inizializzo le date del budget e imposto il campo di riferimento per il periodo
				dataInizioGreg.setTimeInMillis(dataInizio);
				dataFineGreg.setTimeInMillis(dataInizio);
				if(ripetizione.equals("giornaliero")) {
					campoData = GregorianCalendar.DATE;
					moltiplicatore = 1;
				}
				else if(ripetizione.equals("settimanale")) {
					campoData = GregorianCalendar.WEEK_OF_YEAR;
					moltiplicatore = 1;
				}
				else if(ripetizione.equals("bisettimanale")) {
					campoData = GregorianCalendar.WEEK_OF_YEAR;
					moltiplicatore = 2;
				}
				else if(ripetizione.equals("mensile")) {
					campoData = GregorianCalendar.MONTH;
					moltiplicatore = 1;
				}
				else if(ripetizione.equals("annuale")) {
					campoData = GregorianCalendar.YEAR;
					moltiplicatore = 1;
				}
				
				double risparmio;
				double nuovoImportoValprin = importoValprin;
				if(aggiungereRimanenza == 1) {
					risparmio = importoValprin - spesaSost;
					nuovoImportoValprin += (risparmio>0? risparmio: 0);
				}
				
				//routine per aggiungere i budget periodici mancanti ad oggi
				do {	
					dataInizioGreg.add(campoData, 1 * moltiplicatore);
					dataFineGreg.setTimeInMillis(dataInizioGreg.getTimeInMillis());
					dataFineGreg.add(campoData, 1 * moltiplicatore);
					dataFineGreg.add(GregorianCalendar.DATE, -1);
					
					//calcolo il totale spese sostenute per questo budget
					spesaTot = 0.0;
					StringTokenizer st = new StringTokenizer(voce, ",");
					while(st.hasMoreTokens()) {
						String voceSpesa = st.nextToken();
						Cursor curSomma = mioSQLiteDatabase.rawQuery(SPESE_SOST_INTERVALLO_SPESAX_TOTALE, new String[] {Long.valueOf(dataInizioGreg.getTimeInMillis()).toString(), Long.valueOf(dataFineGreg.getTimeInMillis()).toString(), voceSpesa});
						curSomma.moveToFirst();
						spesaTot += curSomma.getDouble(curSomma.getColumnIndex("totale_spesa"));
						curSomma.close();
					}	
				
					//aggiungo il record del budget
					risparmio = nuovoImportoValprin - spesaTot;
					
					ContentValues aggiungiContact = new ContentValues();
					aggiungiContact.put("voce", voce);
					aggiungiContact.put("ripetizione", ripetizione);
					aggiungiContact.put("importo", nuovoImportoValprin);
					aggiungiContact.put("valuta", valuta);
					aggiungiContact.put("importo_valprin", nuovoImportoValprin);
					aggiungiContact.put("data_inizio", dataInizioGreg.getTimeInMillis());
					aggiungiContact.put("data_fine", dataFineGreg.getTimeInMillis());
					aggiungiContact.put("aggiungere_rimanenza", aggiungereRimanenza);
					aggiungiContact.put("spesa_sost", spesaTot);	
					aggiungiContact.put("risparmio", risparmio);
					aggiungiContact.put("budget_iniziale", budgetIniziale);
					aggiungiContact.put("ultimo_aggiunto", 0);
					idBudgetAggiunto = mioSQLiteDatabase.insert("spese_budget", null, aggiungiContact);
					
					budgetAggiunti++;
						
					if(aggiungereRimanenza == 1) {	 
						nuovoImportoValprin += (risparmio>0? risparmio: 0);
					}

				} while(dataFineGreg.getTimeInMillis() < oggi);
				
				//per l'ultimo budget aggiunto della stessa serie imposto ultimo_aggiunto su 1
				if(aggiungereRimanenza == 1) {	 
					nuovoImportoValprin -= (risparmio>0? risparmio: 0);
				}
				
				ContentValues cvUltimoBudget = new ContentValues();
				cvUltimoBudget.put("voce", voce);
				cvUltimoBudget.put("ripetizione", ripetizione);
				cvUltimoBudget.put("importo", nuovoImportoValprin);
				cvUltimoBudget.put("valuta", valuta);
				cvUltimoBudget.put("importo_valprin", nuovoImportoValprin);
				cvUltimoBudget.put("data_inizio", dataInizioGreg.getTimeInMillis());
				cvUltimoBudget.put("data_fine", dataFineGreg.getTimeInMillis());
				cvUltimoBudget.put("aggiungere_rimanenza", aggiungereRimanenza);
				cvUltimoBudget.put("spesa_sost", spesaTot);
				cvUltimoBudget.put("risparmio", risparmio);
				cvUltimoBudget.put("budget_iniziale", budgetIniziale);
				cvUltimoBudget.put("ultimo_aggiunto", 1);
				mioSQLiteDatabase.update("spese_budget", cvUltimoBudget, "_id=" + idBudgetAggiunto, null);
				
			}
			
			curBudgetScaduti.close();
			
			//elimino tutti i budget una_tantum scaduti
			mioSQLiteDatabase.delete("spese_budget", "ripetizione=\'una_tantum\' AND ? > data_fine", new String[] {oggi.toString()});
			
			mioSQLiteDatabase.setTransactionSuccessful();
		}
		catch(SQLiteException exc) {
			exc.printStackTrace();
			return -1;
		}
		finally {
			mioSQLiteDatabase.endTransaction();
			close();
		}
		
		return budgetAggiunti;
	}
		
		
	// variabili d'istanza
	private SQLiteDatabase mioSQLiteDatabase;
	private DatabaseOpenHelper mioDatabaseOpenHelper;
	private Context mioContext;
	private Long oggi;
}
