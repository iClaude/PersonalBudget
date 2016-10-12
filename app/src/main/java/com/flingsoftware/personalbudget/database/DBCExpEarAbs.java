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
     * @param date            date of this element in the format Unix Time, the number of seconds since
     *                        1970-01-01 00:00:00 UTC
     * @param tag            tag of this element (see table spese_voci)
     * @param amount         amount
     * @param currency          currecy symbol
     * @param amountCurr amount in default currency
     * @param description     description
     * @param repetitionId  id per eventuali spese ripetute, come da tabella spese_ripet. 0 se non c'è
     *                        ripetizione.
     * @param account           nome del conto
     * @param favorite        1 for favorite transactions, 0 otherwise
     */
    public void insertElement(long date, String tag, double amount, String currency, double amountCurr, String description, long repetitionId, String account, int favorite) {
        ContentValues nuovoContact = new ContentValues();
        nuovoContact.put("data", date);
        nuovoContact.put("voce", tag);
        nuovoContact.put("importo", amount);
        nuovoContact.put("valuta", currency);
        nuovoContact.put("importo_valprin", amountCurr);
        nuovoContact.put("descrizione", description);
        nuovoContact.put("ripetizione_id", repetitionId);
        nuovoContact.put("conto", account);
        nuovoContact.put("favorite", favorite);

        synchronized (sDataLock) {
            openModifica();
            mioSQLiteDatabase.insert(getTableName(), null, nuovoContact);
            close();
        }
    }


    /**
     * Update an expense/earning.
     *
     * @param id           id of the expense/earning
     * @param date         date of this element in the format Unix Time, the number of seconds since
     *                     1970-01-01 00:00:00 UTC
     * @param tag          tag of this element (see table spese_voci)
     * @param amount       amount
     * @param currency     currecy symbol
     * @param amountCurr   amount in default currency
     * @param description  description
     * @param repetitionId id per eventuali spese ripetute, come da tabella spese_ripet. 0 se non c'è
     *                     ripetizione.
     * @param account      nome del conto
     * @param favorite     1 for favorite transactions, 0 otherwise
     */
    public void updateElement(long id, long date, String tag, double amount, String currency, double amountCurr, String description, long repetitionId, String account, int favorite) {
        ContentValues updateContact = new ContentValues();
        updateContact.put("data", date);
        updateContact.put("voce", tag);
        updateContact.put("importo", amount);
        updateContact.put("valuta", currency);
        updateContact.put("importo_valprin", amountCurr);
        updateContact.put("descrizione", description);
        updateContact.put("ripetizione_id", repetitionId);
        updateContact.put("conto", account);
        updateContact.put("favorite", favorite);

        synchronized (sDataLock) {
            openModifica();
            mioSQLiteDatabase.update(getTableName(), updateContact, "_id=" + id, null);
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


    /**
     * Delete all repeated expenses/earnings with a given repetition id, but only from the
     * supplied date, returning the number of records deleted.
     *
     * @param repetitionId refers to the field _id of the tables spese_ripet or entrate_ripet
     * @param fromDate date (in milliseconds, long) from which deleting repeated elements
     *
     * @return numero di record cancellati dalla tabella
     */
    public int deleteRepeatedElementsFromDate(long repetitionId, long fromDate) {
        synchronized (sDataLock) {
            openModifica();
            int num = mioSQLiteDatabase.delete(getTableName(), "ripetizione_id=" + repetitionId + " AND data>=" + fromDate, null);
            close();

            return num;
        }
    }

}
