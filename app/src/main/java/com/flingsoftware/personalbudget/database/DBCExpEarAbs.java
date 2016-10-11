/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;


/**
 * Abstract class for expenses/earnings.
 * Work in progress...
 */

public abstract class DBCExpEarAbs {

    // Variables.
    protected SQLiteDatabase mioSQLiteDatabase;
    protected DatabaseOpenHelper mioDatabaseOpenHelper;


    public DBCExpEarAbs(Context context) {
        mioDatabaseOpenHelper = new DatabaseOpenHelper(context, DatabaseOpenHelper.NOME_DATABASE, null);
    }

    public void openModifica() throws SQLException {
        mioSQLiteDatabase = mioDatabaseOpenHelper.getWritableDatabase();
        mioSQLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
    }

    public void openLettura() throws SQLException {
        mioSQLiteDatabase = mioDatabaseOpenHelper.getReadableDatabase();
    }

    public void close() {
        if (mioSQLiteDatabase != null)
            mioSQLiteDatabase.close();
    }


    // Gets the name of the table (spese_sost or entrate_inc).
    public abstract String getTableName();

    /**
     * Insert a new expense/earning.
     *
     * @param data            date of this element in the format Unix Time, the number of seconds since
     *                        1970-01-01 00:00:00 UTC
     * @param voce            tag of this element (see table spese_voci)
     * @param importo         amount
     * @param valuta          currecy symbol
     * @param importo_valprin amount in default currency
     * @param descrizione     description
     * @param ripetizione_id  id per eventuali spese ripetute, come da tabella spese_ripet. 0 se non c'è
     *                        ripetizione.
     * @param conto           nome del conto
     * @param favorite        1 for favorite transactions, 0 otherwise
     */
    public void insertElement(long data, String voce, double importo, String valuta, double importo_valprin, String descrizione, long ripetizione_id, String conto, int favorite) {
        ContentValues nuovoContact = new ContentValues();
        nuovoContact.put("data", data);
        nuovoContact.put("voce", voce);
        nuovoContact.put("importo", importo);
        nuovoContact.put("valuta", valuta);
        nuovoContact.put("importo_valprin", importo_valprin);
        nuovoContact.put("descrizione", descrizione);
        nuovoContact.put("ripetizione_id", ripetizione_id);
        nuovoContact.put("conto", conto);
        nuovoContact.put("favorite", favorite);

        synchronized (sDataLock) {
            openModifica();
            mioSQLiteDatabase.insert(getTableName(), null, nuovoContact);
            close();
        }
    }

    // Delete an expense/earning.
    public int deleteElement(long id) {
        synchronized (sDataLock) {
            int elementsDeleted = 0;
            openModifica();
            elementsDeleted = mioSQLiteDatabase.delete(getTableName(), "_id=" + id, null);
            close();

            return elementsDeleted;
        }
    }

    ;

    /**
     * Delete all repeated elements with a given repetition id from the expenses or earnings table
     * and returns the number of deleted records.
     *
     * @param repetitionId repetition id (referred to the table spese_ripet
     * @return number of records deleted
     */
    public int deleteElementRepeated(long repetitionId) {
        synchronized (sDataLock) {
            openModifica();
            int num = mioSQLiteDatabase.delete(getTableName(), "ripetizione_id=" + repetitionId, null);
            close();

            return num;
        }
    }

}
