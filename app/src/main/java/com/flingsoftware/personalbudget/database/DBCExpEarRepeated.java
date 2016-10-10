/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Abstract class for repeated expenses/earnings.
 * Work in progress...
 */

public abstract class DBCExpEarRepeated {

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


    public DBCExpEarRepeated(Context context) {
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

    // Get a specific expense/earning.
    public abstract Cursor getItemRepeated(long id);

}
