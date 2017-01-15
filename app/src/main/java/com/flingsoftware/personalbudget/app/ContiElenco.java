/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCConti;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.CONTO_DEFAULT;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;


public class ContiElenco extends ActionBarActivity {
	
	// Costanti
	private static final int OP_AGGIUNGI = 0;
	private static final int OP_TRASFERISCI = 1;
	private static final int OP_VISUALIZZA = 2;
	public static final String EXTRA_ID = "id";
	public static final String EXTRA_CONTO = "conto";
	public static final String EXTRA_SALDO = "saldo";
	public static final String EXTRA_DATA_SALDO = "data_saldo";
	public static final String EXTRA_ENTRATE_INCASSATE = "entrate_incassate";
	public static final String EXTRA_SPESE_SOSTENUTE = "spese_sostenute";
	public static final String EXTRA_SALDO_FINALE = "saldo_finale";
	public static final String EXTRA_CONTO_DEFAULT = "conto_default";
	
	// Variabili
	private ArrayList<ContoDettaglio> alConti = new ArrayList<ContoDettaglio>();
	private Currency currValuta;
	private boolean valutaAlternativa;
	private NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
	DecimalFormat nfRidotto = new DecimalFormat("#,##0.00");
	private TextView tvTotale;
	private ListView lvConti;
	private MioAdapter lvContiAdapter;
	private String contoDefault;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }

        super.onCreate(savedInstanceState);
		setContentView(R.layout.conti_elenco);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		tvTotale = (TextView) findViewById(R.id.conti_tvTotale);
		//impostazione ListView
		lvConti = (ListView) findViewById(R.id.conti_lvConti);
		lvContiAdapter = new MioAdapter(this, alConti);	
		lvConti.setAdapter(lvContiAdapter);
		lvConti.setOnItemClickListener(lvContiOnItemClickListener);
		
		ricavaValuta();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		new RecuperaContiTask().execute((Object[]) null);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_conti_elenco, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
	        
	        return true;
		case R.id.menu_conti_elenco_aggiungi:
			Intent intIns = new Intent(this, ContiInserimento.class);
			startActivityForResult(intIns, OP_AGGIUNGI);
	        
	        return true;
		case R.id.menu_conti_elenco_trasferimenti:
			Intent intTrasf = new Intent(this, ContiTrasferimento.class);
			startActivityForResult(intTrasf, OP_TRASFERISCI);

			return true;	
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	private class RecuperaContiTask extends AsyncTask<Object, Object, Double> {
		DBCConti dbcConti = new DBCConti(ContiElenco.this);
		DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(ContiElenco.this);
		DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(ContiElenco.this);
		ArrayList<ContoDettaglio> alContiComodo = new ArrayList<ContoDettaglio>();
		
		protected Double doInBackground(Object... params) {
			// Ricavo il conto di default dalla preferenze.
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ContiElenco.this);
			contoDefault = pref.getString(CONTO_DEFAULT, "default");

			dbcConti.openLettura();
			dbcSpeseSostenute.openLettura();
			dbcEntrateIncassate.openLettura();
			
			Cursor curConti = dbcConti.getTuttiIConti();
			Cursor curCalcoli = null;
			double totale = 0;		
			while(curConti.moveToNext()) {
				long id = curConti.getLong(curConti.getColumnIndex("_id"));
				String conto = curConti.getString(curConti.getColumnIndex("conto"));
				double saldo = curConti.getDouble(curConti.getColumnIndex("saldo"));
				long dataSaldo = curConti.getLong(curConti.getColumnIndex("data_saldo"));
				
				curCalcoli = dbcEntrateIncassate.getEntrateIncassateIntervalloTotale(conto, dataSaldo, new Date().getTime());
				curCalcoli.moveToFirst();
				Double entrateIncassate = curCalcoli.getDouble(curCalcoli.getColumnIndex("totale_entrata"));
				
				curCalcoli = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotale(conto, dataSaldo, new Date().getTime());
				curCalcoli.moveToFirst();
				Double speseSostenute = curCalcoli.getDouble(curCalcoli.getColumnIndex("totale_spesa"));
				double saldoFin = saldo + entrateIncassate - speseSostenute;
				totale += saldoFin;

				boolean isContoDefault = false;
				if(conto.equals(contoDefault)) {
					isContoDefault = true;
				}
				ContoDettaglio mioConto = new ContoDettaglio(id, conto, saldo, dataSaldo, entrateIncassate, speseSostenute, saldoFin, isContoDefault);
				alContiComodo.add(mioConto);
			}
			
			curConti.close();
			curCalcoli.close();
			dbcConti.close();
			dbcSpeseSostenute.close();
			dbcEntrateIncassate.close();

			return totale;
		}
		
		protected void onPostExecute(Double result) {
			NumberFormat nfTotale = NumberFormat.getCurrencyInstance(Locale.getDefault());
			nfTotale.setCurrency(currValuta);
			if(result>=0) {
				tvTotale.setTextColor(Color.argb(255, 0, 128, 0));
			}
			else {
				tvTotale.setTextColor(Color.RED);
			}
			tvTotale.setText(nfTotale.format(result));
			
			alConti.clear();
			alConti.addAll(alContiComodo);
			lvContiAdapter.notifyDataSetChanged();
		}
	}
	
	
	private static class ViewHolder {
		TextView tvLetteraIniz;
		TextView tvConto;
		TextView tvImporto;
		TextView tvValuta;
	}
	
	
	private class MioAdapter extends ArrayAdapter<ContoDettaglio> {
		private LayoutInflater inflater;
		private List<ContoDettaglio> lstConti;
		
		public MioAdapter(Context context, List<ContoDettaglio> lstConti) {
		    super(context, -1, lstConti);
		    this.lstConti = lstConti;
		    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.conti_elenco_listviewitem, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.tvLetteraIniz = (TextView) convertView.findViewById(R.id.cli_tvLetteraIniz);
				viewHolder.tvConto = (TextView) convertView.findViewById(R.id.cli_tvConto);
				viewHolder.tvImporto = (TextView) convertView.findViewById(R.id.cli_tvImporto);
				viewHolder.tvValuta = (TextView) convertView.findViewById(R.id.cli_tvValuta);
				convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			ContoDettaglio contoDettaglio = lstConti.get(position);
			if(contoDettaglio.getContoDefault()) { // conto di default con label evidenziata
				viewHolder.tvLetteraIniz.setBackgroundResource(R.drawable.back_round_accent);
			}
			else {
				viewHolder.tvLetteraIniz.setBackgroundResource(R.drawable.back_round_primary);
			}
			viewHolder.tvLetteraIniz.setText(contoDettaglio.getConto().substring(0, 1).toUpperCase(Locale.getDefault()));
			viewHolder.tvConto.setText(contoDettaglio.getConto());
			if(!valutaAlternativa) {
				String importoFormattato = nf.format(contoDettaglio.getSaldoFinale());
				viewHolder.tvImporto.setText(importoFormattato);
			}
			else {
				String importoFormattato = nfRidotto.format(contoDettaglio.getSaldoFinale());
				viewHolder.tvImporto.setText(importoFormattato);
				viewHolder.tvValuta.setText("(" + currValuta.getSymbol() + ")");
			}
			
			return convertView;
		}
	}
	
	
	/*
	 * Ricavo la valuta principale e il conto di default salvati nelle preferenze.
	 */
	private void ricavaValuta() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String valuta = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(valuta);
		valutaAlternativa = !currValuta.getCurrencyCode().equals(Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == OP_AGGIUNGI && resultCode == Activity.RESULT_OK) {
			new RecuperaContiTask().execute((Object[]) null);
		}
		else if(requestCode == OP_TRASFERISCI && resultCode == Activity.RESULT_OK) {
			new RecuperaContiTask().execute((Object[]) null);
		}
		else if(requestCode == OP_VISUALIZZA) {
			new RecuperaContiTask().execute((Object[]) null);
		}
	}
	
	
	// Clicco sugli elementi della ListView (elenco conti) per visualizzare il conto
	OnItemClickListener lvContiOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			ContoDettaglio mioConto = alConti.get(arg2);
			Intent intent = new Intent(ContiElenco.this, ContiVisualizza.class);
			intent.putExtra(EXTRA_ID, mioConto.getId());
			intent.putExtra(EXTRA_CONTO, mioConto.getConto());
			intent.putExtra(EXTRA_SALDO, mioConto.getSaldo());
			intent.putExtra(EXTRA_DATA_SALDO, mioConto.getDataSaldo());
			intent.putExtra(EXTRA_ENTRATE_INCASSATE, mioConto.getEntrateIncassate());
			intent.putExtra(EXTRA_SPESE_SOSTENUTE, mioConto.getSpeseSostenute());		
			intent.putExtra(EXTRA_SALDO_FINALE, mioConto.getSaldoFinale());
			intent.putExtra(EXTRA_CONTO_DEFAULT, mioConto.getContoDefault());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivityForResult(intent, OP_VISUALIZZA, ActivityOptions.makeSceneTransitionAnimation(ContiElenco.this).toBundle());
            }
            else {
                startActivityForResult(intent, OP_VISUALIZZA);
            }
		}
		
	};
	
	
	// Classe di utilit� per rappresentare tutte le info relative ad un dato conto
	public static class ContoDettaglio {
		// Variabili
		private long id;
		private String conto;
		private double saldo;
		private long dataSaldo;
		private double entrateIncassate;
		private double speseSostenute;
		private double saldoFinale;
		private boolean contoDefault;
			
		public ContoDettaglio() {
			
		}
		
		public ContoDettaglio(long id, String conto, double saldo,
				long dataSaldo, double entrateIncassate, double speseSostenute,
				double saldoFinale, boolean contoDefault) {
			super();
			this.id = id;
			this.conto = conto;
			this.saldo = saldo;
			this.dataSaldo = dataSaldo;
			this.entrateIncassate = entrateIncassate;
			this.speseSostenute = speseSostenute;
			this.saldoFinale = saldoFinale;
			this.contoDefault = contoDefault;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getConto() {
			return conto;
		}

		public void setConto(String conto) {
			this.conto = conto;
		}

		public double getSaldo() {
			return saldo;
		}

		public void setSaldo(double saldo) {
			this.saldo = saldo;
		}

		public long getDataSaldo() {
			return dataSaldo;
		}

		public void setDataSaldo(long dataSaldo) {
			this.dataSaldo = dataSaldo;
		}

		public double getEntrateIncassate() {
			return entrateIncassate;
		}

		public void setEntrateIncassate(double entrateIncassate) {
			this.entrateIncassate = entrateIncassate;
		}

		public double getSpeseSostenute() {
			return speseSostenute;
		}

		public void setSpeseSostenute(double speseSostenute) {
			this.speseSostenute = speseSostenute;
		}

		public double getSaldoFinale() {
			return saldoFinale;
		}

		public void setSaldoFinale(double saldoFinale) {
			this.saldoFinale = saldoFinale;
		}

		public boolean getContoDefault() {
			return contoDefault;
		}

		public void setContoDefault(boolean contoDefault) {
			this.contoDefault = contoDefault;
		}
	}
}
