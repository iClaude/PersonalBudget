/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.charts.piechart.AmountAndLabel;
import com.flingsoftware.personalbudget.charts.piechart.BalancePieChartBuilder;
import com.flingsoftware.personalbudget.charts.piechart.PieChartBuilder;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import org.achartengine.GraphicalView;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class FragmentSaldo extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	// Test task.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		dbcEntrateIncassate = new DBCEntrateIncassate(getActivity());
		dbcSpeseSostenute = new DBCSpeseSostenute(getActivity());
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_balance, container, false);

		//ottengo i reference ai vari componenti
		tvEntrate = (TextView) rootView.findViewById(R.id.tvEarnings);
		tvSpese = (TextView) rootView.findViewById(R.id.tvExpenses);
		tvEntrateFuture = (TextView) rootView.findViewById(R.id.tvEarningsFut);
		tvSpeseFuture = (TextView) rootView.findViewById(R.id.tvExpensesFut);
		tvSaldoPrevisto = (TextView) rootView.findViewById(R.id.tvBalance);
		tvSaldoGenerale = (TextView) rootView.findViewById(R.id.tvBalanceFinal);
        llChart = (LinearLayout) rootView.findViewById(R.id.llChart);

		//aggiorno tvPeriodo con il periodo iniziale e registro l'ascoltatore per il cambio preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.registerOnSharedPreferenceChangeListener(this);
		ricavaPeriodo(pref);

		//aggiorno i totali con i dati contenuti nel database in un thread separato
		new RefreshBalanceTask().execute((Object[]) null);

		return rootView;
	}


	//Nascondo le voci di menu visualizza per voce, ricerca e favoriti.
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem item = menu.findItem(R.id.menu_ricerca);
		//item.collapseActionView();
		item.setVisible(false);
		item = menu.findItem(R.id.menu_visualizzaPerVoce);
		item.setVisible(false);

		SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_ricerca));
		searchView.setIconified(true);
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();

		//deregistro l'ascoltatore per il cambio preferenze
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.unregisterOnSharedPreferenceChangeListener(this);
	}


	/*
	 * Ricavo il periodo di riferimento dal file delle preferenze.
	 */
	private void ricavaPeriodo(SharedPreferences prefTempo) {
		int tipoDataAutomatica = prefTempo.getInt(CostantiPreferenze.DATA_AUTOMATICA, -1);
		int offset = prefTempo.getInt(CostantiPreferenze.DATA_OFFSET, 1);

		if(tipoDataAutomatica == -1) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);

			long dataInizioLong = prefTempo.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
			dataInizio.setTimeInMillis(dataInizioLong);

			dataComodo.add(GregorianCalendar.MONTH, 1);
			dataComodo.add(GregorianCalendar.DATE, -1);
			long dataFineLong = prefTempo.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
			dataFine.setTimeInMillis(dataFineLong);
		}
		else {
			FunzioniComuni.impostaPeriodoAutomatico(tipoDataAutomatica, dataInizio, dataFine, offset);
		}
	}

	/*
	 * Listener per il cambio delle preferenze.
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(CostantiPreferenze.DATA_INIZIO) || key.equals(CostantiPreferenze.DATA_FINE)) {
			GregorianCalendar dataComodo = new GregorianCalendar();
			int mese = dataComodo.get(GregorianCalendar.MONTH);
			int anno = dataComodo.get(GregorianCalendar.YEAR);
			dataComodo = new GregorianCalendar(anno, mese, 1);

			if(key.equals(CostantiPreferenze.DATA_INIZIO)) {
				long dataInizioLong = sharedPreferences.getLong(CostantiPreferenze.DATA_INIZIO, dataComodo.getTimeInMillis());
				dataInizio.setTimeInMillis(dataInizioLong);
			}
			else if(key.equals(CostantiPreferenze.DATA_FINE)) {
				dataComodo.add(GregorianCalendar.MONTH, 1);
				dataComodo.add(GregorianCalendar.DATE, -1);
				long dataFineLong = sharedPreferences.getLong(CostantiPreferenze.DATA_FINE, dataComodo.getTimeInMillis());
				dataFine.setTimeInMillis(dataFineLong);
			}
		}
	}


	//metodo richiamato dall'Activity madre per aggiornare i dati quando cambia il periodo selezionato
	public void aggiornaCursor() {
		new RefreshBalanceTask().execute((Object[]) null);
	}


	//AsyncTask per caricare i totali entrate, spese e saldo
	private class RefreshBalanceTask extends AsyncTask<Object, Void, Double[]> {

		protected Double[] doInBackground(Object... params) {
			//ricavo il totale entrate
			while(true) {
				if(dbcEntrateIncassate != null)
					break;
			}

			String trasf = getString(R.string.voce_giroconto);
			Cursor cursor = null;
			long adesso = System.currentTimeMillis();
			Double speseTotali = 0.0;
			Double entrateTotali = 0.0;
			Double saldo = 0.0;

			dbcEntrateIncassate.openLettura();
			dbcSpeseSostenute.openLettura();

			// Entrate e spese passate.
			if(adesso > dataInizio.getTimeInMillis()) {
				long tempoFineMillis = dataFine.getTimeInMillis() > adesso ? adesso : dataFine.getTimeInMillis();

				if (MainPersonalBudget.conto.equals("%")) {
					cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), tempoFineMillis, trasf);
				} else {
					cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotale(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), tempoFineMillis);
				}
				cursor.moveToFirst();
				entrateTotali = cursor.getDouble(cursor.getColumnIndex("totale_entrata"));

				//ricavo il totale spese
				cursor = null;
				if (MainPersonalBudget.conto.equals("%")) {
					cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), tempoFineMillis, trasf);
				} else {
					cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotale(MainPersonalBudget.conto, dataInizio.getTimeInMillis(), tempoFineMillis);
				}
				cursor.moveToFirst();
				speseTotali = cursor.getDouble(cursor.getColumnIndex("totale_spesa"));

				//ricavo il saldo
				saldo = entrateTotali - speseTotali;
			}

			// Entrate e spese future.
			Double entrateFuture = 0.0;
			Double speseFuture = 0.0;
			Double saldoPrevisto;
			if(adesso < dataFine.getTimeInMillis()) {
				long tempoInizioMillis = (adesso < dataFine.getTimeInMillis()) && (adesso > dataInizio.getTimeInMillis()) ? adesso : dataInizio.getTimeInMillis();

				cursor = null;
				if(MainPersonalBudget.conto.equals("%")) {
					cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf(MainPersonalBudget.conto, tempoInizioMillis, dataFine.getTimeInMillis(), trasf);
				}
				else {
					cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotale(MainPersonalBudget.conto, tempoInizioMillis, dataFine.getTimeInMillis());
				}
				cursor.moveToFirst();
				entrateFuture = cursor.getDouble(cursor.getColumnIndex("totale_entrata"));

				//ricavo il totale spese
				cursor = null;
				dbcSpeseSostenute.openLettura();
				if(MainPersonalBudget.conto.equals("%")) {
					cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf(MainPersonalBudget.conto, tempoInizioMillis, dataFine.getTimeInMillis(), trasf);
				}
				else {
					cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotale(MainPersonalBudget.conto, tempoInizioMillis, dataFine.getTimeInMillis());
				}
				cursor.moveToFirst();
				speseFuture = cursor.getDouble(cursor.getColumnIndex("totale_spesa"));
			}
			saldoPrevisto = saldo + entrateFuture - speseFuture;

			if(MainPersonalBudget.conto.equals("%")) {
				cursor.close();
				dbcEntrateIncassate.close();
				dbcSpeseSostenute.close();

				return new Double[] {entrateTotali, speseTotali, saldo, 0.0, entrateFuture, speseFuture, saldoPrevisto};
			}
			else {
				//saldo finale del conto
				DBCConti dbcConti = new DBCConti(getActivity());
				dbcConti.openLettura();
				cursor = dbcConti.getContoConNome(MainPersonalBudget.conto);
				double saldoInizConto = 0;
				long dataSaldoInizConto = 0;
				if(cursor.moveToFirst()) {
					saldoInizConto = cursor.getDouble(cursor.getColumnIndex("saldo"));
					dataSaldoInizConto = cursor.getLong(cursor.getColumnIndex("data_saldo"));
				}
				dbcConti.close();

				cursor = dbcEntrateIncassate.getEntrateIncassateIntervalloTotale(MainPersonalBudget.conto, dataSaldoInizConto, new Date().getTime());
				cursor.moveToFirst();
				Double entrateTotaliGen = cursor.getDouble(cursor.getColumnIndex("totale_entrata"));
				cursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotale(MainPersonalBudget.conto, dataSaldoInizConto, new Date().getTime());
				cursor.moveToFirst();
				Double speseTotaliGen = cursor.getDouble(cursor.getColumnIndex("totale_spesa"));
				double saldoFin = saldoInizConto + entrateTotaliGen - speseTotaliGen;

				cursor.close();
				dbcEntrateIncassate.close();
				dbcSpeseSostenute.close();
				dbcConti.close();

				return new Double[] {entrateTotali, speseTotali, saldo, saldoFin, entrateFuture, speseFuture, saldoPrevisto};
			}
		}

		protected void onPostExecute(Double[] risultati) {
			// Sezione saldo periodo.
            tvEntrate.setText(UtilityVarious.formatAmountNoCurrency(risultati[0]));
            tvSpese.setText(UtilityVarious.formatAmountNoCurrency(risultati[1]));
            tvEntrateFuture.setText(UtilityVarious.formatAmountNoCurrency(risultati[4]));
            tvSpeseFuture.setText(UtilityVarious.formatAmountNoCurrency(risultati[5]));
            tvSaldoPrevisto.setText(UtilityVarious.formatAmountColorNoCurrency(risultati[6], getActivity()));

			// Sezione saldo finale.
			if(MainPersonalBudget.conto.equals("%")) {
				tvSaldoGenerale.setVisibility(View.GONE);
			}
			else {
				tvSaldoGenerale.setVisibility(View.VISIBLE);
                tvSaldoGenerale.setText("(= " + UtilityVarious.formatAmountNoCurrency(risultati[3]) + ")");
            }

            // Pie chart.
            displayPieChart(risultati[0] + risultati[4], risultati[1] + risultati[5]);
        }
	}

    // Display pie chart of earnings/expenses.
    private void displayPieChart(double earnings, double expenses) {
		// aChart engine does not supporto all amounts equal to zero.
		if (earnings == 0 && expenses == 0) {
			earnings = 0.000001;
			expenses = 0.000001;
		}
		AmountAndLabel[] amountsAndLabels = new AmountAndLabel[2];
        amountsAndLabels[0] = new AmountAndLabel(getString(R.string.frag_balance_expenses), expenses);
        amountsAndLabels[1] = new AmountAndLabel(getString(R.string.frag_balance_earnings), earnings);

        PieChartBuilder pieChartBuilder = new BalancePieChartBuilder();
        pieChartBuilder.createNewPieChart(getActivity(), amountsAndLabels, "");
        GraphicalView pieChart = pieChartBuilder.getPieChart();
        llChart.removeAllViews();
        llChart.addView(pieChart);
    }


	//variabili di istanza
	private GregorianCalendar dataInizio = new GregorianCalendar();
	private GregorianCalendar dataFine = new GregorianCalendar();
	private DBCEntrateIncassate dbcEntrateIncassate;
	private DBCSpeseSostenute dbcSpeseSostenute;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);

	private TextView tvPeriodo;
	private TextView tvEntrate;
	private TextView tvSpese;
	private TextView tvEntrateFuture;
	private TextView tvSpeseFuture;
	private TextView tvSaldoPrevisto;
	private TextView tvSaldoGenerale;
    private LinearLayout llChart;
}
