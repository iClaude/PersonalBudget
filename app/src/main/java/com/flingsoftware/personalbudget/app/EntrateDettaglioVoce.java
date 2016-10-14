/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

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
    public void updateBudgetTable() {
        // no implementation: earnings don't have budgets
    }

}
