/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Classe astratta per le voci di entrata/spesa.
 */

public abstract class DBCVociAbs {
    // Variabili d'istanza (quelle protected sono utilizzabili anche dalle sottoclassi).
    protected SQLiteDatabase mioSQLiteDatabase;
    protected DatabaseOpenHelper mioDatabaseOpenHelper;


    public DBCVociAbs(Context context) {
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
        if (mioSQLiteDatabase != null)
            mioSQLiteDatabase.close();
    }


    // Restituisce il nome della tabella su cui agisco (spese_voci or entrate_voci).
    public abstract String getNomeTabella();


    /**
     * Restituisce un Cursor contenente tutti i record della tabella (tutti i campi) che
     * contengono la stringa di ricerca nel campo voce.
     *
     * @param ricerca stringa da cercare nel campo voce
     * @return Cursor contenente tutti i record della tabella (tutti i campi)
     */
    public Cursor getTutteLeVociFiltrato(String ricerca) {
        return mioSQLiteDatabase.query(getNomeTabella(), new String[]{"_id", "voce", "icona"}, "voce LIKE ?", new String[]{"%" + ricerca + "%"}, null, null, "voce");
    }
}
