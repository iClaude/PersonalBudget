/*
 * Copyright (c) - Software developed by iClaude.
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


    public DBCVociAbs(Context context) {

    }


    public void openModifica() throws SQLException {
        mioSQLiteDatabase = DatabaseOpenHelperWrapper.getDatabase();
        mioSQLiteDatabase.execSQL("PRAGMA foreign_keys=ON;"); // bisogna abilitare le foreign keys qui
    }


    public void openLettura() throws SQLException {
        mioSQLiteDatabase = DatabaseOpenHelperWrapper.getDatabase();
    }


    public void close() {
        // Android closes the database automatically for you.
/*        if (mioSQLiteDatabase != null)
            mioSQLiteDatabase.close();*/
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
