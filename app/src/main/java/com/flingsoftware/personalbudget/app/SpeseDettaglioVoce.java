/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCExpEarAbs;
import com.flingsoftware.personalbudget.database.DBCExpEarRepeatedAbs;
import com.flingsoftware.personalbudget.database.DBCSpeseRipetute;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCVociAbs;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;


/*
    This class extends ExpenseEarningsDetails abstract Activity to display an earning.
 */
public class SpeseDettaglioVoce extends ExpenseEarningDetails {

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
        return new DBCSpeseSostenute(this);
    }

    @Override
    public DBCExpEarRepeatedAbs getDBCExpEarRepeated() {
        return new DBCSpeseRipetute(this);
    }

    @Override
    public DBCVociAbs getDBCExpEarTags() {
        return new DBCSpeseVoci(this);
    }

	@Override
    public Intent getEditIntent() {
        return new Intent(this, SpeseAggiungi.class);
    }

    /*
        When one or more expenses are added or deleted, the budget table must be updated.
     */
    @Override
    public void updateBudgetTable(String query, String... args) {
        new AggiornaTabellaBudgetTask(query, args).execute((Object[]) null);
    }



	//AsyncTask per aggiornare la tabella spese_budget campo spesa_sost a seguito della eliminazione della/e spesa/e
	private class AggiornaTabellaBudgetTask extends AsyncTask<Object, Object, Boolean> {
		String query;
		String args[];

		public AggiornaTabellaBudgetTask(String query, String... args) {
			this.query = query;
			this.args = args;
		}

		protected Boolean doInBackground(Object... params) {
			FunzioniAggiornamento aggBudget = new FunzioniAggiornamento(SpeseDettaglioVoce.this);
			int budgetAggiornati = aggBudget.aggiornaTabBudgetSpeseSost(query, args);

			return budgetAggiornati != -1;
		}

		protected void onPostExecute(Boolean result) {
			if (!result) {
				new MioToast(SpeseDettaglioVoce.this, getString(R.string.toast_aggiornamentoDatabase_errore)).visualizza(Toast.LENGTH_SHORT);
			}

			final Intent intAggiornaWidget = new Intent(WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
		}
	}

}
