/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelperWrapper.sDataLock;

/**
 * Abstract class for repeated expenses/earnings.
 * Work in progress...
 */

public abstract class DBCExpEarRepeatedAbs {

    // Columns in the table.
    public static final String COL_TAG = "voce";
    public static final String COL_REP = "ripetizione";
    public static final String COL_AMOUNT = "importo";
    public static final String COL_CURRENCY = "valuta";
    public static final String COL_AMOUNT_MAIN_CURR = "importo_valprin";
    public static final String COL_DESC = "descrizione";
    public static final String COL_DATE_START = "data_inizio";
    public static final String COL_FLAG_END = "flag_fine";
    public static final String COL_DATE_END = "data_fine";
    public static final String COL_UPDATED_TO = "aggiornato_a";
    public static final String COL_ACCOUNT = "conto";

    // Variables.
    protected SQLiteDatabase mioSQLiteDatabase;


    public DBCExpEarRepeatedAbs(Context context) {

    }

    public void openModifica() throws SQLException {
        mioSQLiteDatabase = DatabaseOpenHelperWrapper.getDatabase();
        mioSQLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
    }

    public void openLettura() throws SQLException {
        mioSQLiteDatabase = DatabaseOpenHelperWrapper.getDatabase();
    }

    public void close() {
        // Do nothing because Android closes the database automatically.
        /*if (mioSQLiteDatabase != null)
            mioSQLiteDatabase.close();*/
    }


    // Gets the name of the table (spese_sost or entrate_inc).
    public abstract String getTableName();

    // Get a specific expense/earning.
    public abstract Cursor getItemRepeated(long id);


    /**
     * Aggiorna una spesa ripetuta nella tabella spese_ripet.
     * Ad ogni lancio dell'app aggiornare le spese ripetute nella tabella delle spese ripetute.
     * Es. al 26/3 aggiornare tutte le spese ripetute sostenute fino a quella data a partire
     * dall'ultimo aggiornamento, quindi aggiornare il campo aggiornato_a.
     * Se voglio eliminare tutte le spese ripetute del codice 2 per es. le ricavo facilmente dall'altra
     * tabella cercando tutte le corrispondenze di tale codice nell'ultima colonna
     *
     * @param id          ide della spesa ripetuta di questa tabella
     * @param tag         voce della spesa che deve essere ripetuta
     * @param repetition  tipo di ripetizione (giornaliero, settimanale, bisettimanale, mensile, annuale, giorni_lavorativi, weekend)
     * @param amount      importo spesa
     * @param currency    simbolo valuta
     * @param amountCurr  importo nella valuta di default
     * @param description descrizione della spesa ripetuta
     * @param dateFrom    data di inizio ripetizione spesa nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
     * @param endFlag     la ripetizione è già finita (1) o è in corso(0)
     * @param dateEnd     data di fine ripetizione spesa nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
     * @param updatedTo   aggiornamento tabella spese_sost fino a questa data nel formato Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC
     * @param account     nome del conto
     */
    public void updateElement(long id, String tag, String repetition, double amount, String currency, double amountCurr, String description, long dateFrom, int endFlag, long dateEnd, long updatedTo, String account) {
        ContentValues aggiornaContact = new ContentValues();
        aggiornaContact.put("voce", tag);
        aggiornaContact.put("ripetizione", repetition);
        aggiornaContact.put("importo", amount);
        aggiornaContact.put("valuta", currency);
        aggiornaContact.put("importo_valprin", amountCurr);
        aggiornaContact.put("descrizione", description);
        aggiornaContact.put("data_inizio", dateFrom);
        aggiornaContact.put("flag_fine", endFlag);
        aggiornaContact.put("data_fine", dateEnd);
        aggiornaContact.put("aggiornato_a", updatedTo);
        aggiornaContact.put("conto", account);

        synchronized (sDataLock) {
            openModifica();
            mioSQLiteDatabase.update(getTableName(), aggiornaContact, "_id=" + id, null);
            close();
        }
    }


    /**
     * Deletes a repeated expense/earning from spese_ripet or entrate_ripet table.
     *
     * @param id id of the element to delete from the table
     */

    public void deleteElementRepeated(long id) {
        synchronized (sDataLock) {
            openModifica();
            mioSQLiteDatabase.delete(getTableName(), "_id=" + id, null);
            close();
        }
    }

}
