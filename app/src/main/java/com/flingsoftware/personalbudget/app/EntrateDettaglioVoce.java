/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.content.Intent;

import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCEntrateRipetute;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.database.DBCExpEarAbs;
import com.flingsoftware.personalbudget.database.DBCExpEarRepeatedAbs;
import com.flingsoftware.personalbudget.database.DBCVociAbs;


/*
    This class extends ExpenseEarningsDetails abstract Activity to display an earning.
 */
public class EntrateDettaglioVoce extends ExpenseEarningDetails {

    @Override
    public DBCExpEarAbs getDBCExpEar() {
        return new DBCEntrateIncassate(this);
    }

    @Override
    public DBCExpEarRepeatedAbs getDBCExpEarRepeated() {
        return new DBCEntrateRipetute(this);
    }

    @Override
    public DBCVociAbs getDBCExpEarTags() {
        return new DBCEntrateVoci(this);
    }

    @Override
    public Intent getEditIntent() {
        return new Intent(this, EntrateAggiungi.class);
    }

    @Override
    public void updateBudgetTable(String query, String... args) {
        // no implementation: earnings don't have budgets
    }

}
