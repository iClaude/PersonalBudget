/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import static com.flingsoftware.personalbudget.database.DatabaseOpenHelper.sDataLock;

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
    protected DatabaseOpenHelper mioDatabaseOpenHelper;


    public DBCExpEarRepeatedAbs(Context context) {
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

    // Get a specific expense/earning.
    public abstract Cursor getItemRepeated(long id);


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
