package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.Statistiche.CostantiGrafici.*;
import static org.achartengine.chart.BarChart.Type.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.DialRenderer.Type;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import android.support.v7.app.ActionBarActivity;


public class StatisticheGrafico extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistiche_grafico);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String valuta = sharedPreferences.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(valuta);
		trasf = getString(R.string.voce_giroconto);
        moltTesto = getResources().getDisplayMetrics().density / 2;
		
		//recupero info sul grafico da lanciare
		Bundle extras = getIntent().getExtras();
		boolean spese = extras.getBoolean("spese");
		int tipoGrafico = extras.getInt("grafico");
		long dataInizio = extras.getLong("data_inizio");
		long dataFine = extras.getLong("data_fine");
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
		((TextView) findViewById(R.id.statistiche_grafico_tvPeriodo)).setText(sdf.format(new Date(dataInizio)) + " - " + sdf.format(new Date(dataFine)));
		
		//lancio il grafico
		String colonnaPerQuery;
		if(spese) {
			colonnaPerQuery = "totale_spesa";
		}
		else {
			colonnaPerQuery = "totale_entrata";
		}
		
		switch(tipoGrafico) {
		case GRAFICO_TORTA:
			new RecuperaPiechartTask(spese, colonnaPerQuery, dataInizio, dataFine).execute((Object[]) null);
			break;
		case GRAFICO_BARRE:
			new RecuperaBarchartTask(spese, colonnaPerQuery, dataInizio, dataFine).execute((Object[]) null);
			break;
		case GRAFICO_TEMPORALE:
			new RecuperaTimechartTask(spese, colonnaPerQuery, dataInizio, dataFine).execute((Object[]) null);
			break;
		case GRAFICO_TEMPORALE_TAG:
			new RecuperaTimechartConTagTask(spese, colonnaPerQuery, extras.getString("voce"), dataInizio, dataFine).execute((Object[]) null);
			break;
		case GRAFICO_DIAL:
			new RecuperaDialchartTask(spese, extras.getString("voce"), dataInizio, dataFine).execute((Object[]) null);
			break;
		case GRAFICO_RANGE:
			new RecuperaRangebarchartTask(spese, dataInizio, dataFine).execute((Object[]) null);
			break;
		case GRAFICO_DOUGHNUT:
			new RecuperaDoughnutchartTask(spese, colonnaPerQuery, dataInizio, dataFine, extras.getLong("data_inizio2"), extras.getLong("data_fine2")).execute((Object[]) null);
			break;
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	private class RecuperaPiechartTask extends AsyncTask<Object, Object, Integer> {
		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;
		String arrEtichette[];
		double arrImporti[];
		double arrPercentuali[];
		double importoTot = 0.0;
		
		boolean flagSpese;
		String colonnaPerQuery;
		long dataInizio;
		long dataFine;
		
		private RecuperaPiechartTask(boolean flagSpese, String colonnaPerQuery, long dataInizio, long dataFine) {
			this.flagSpese = flagSpese;
			this.colonnaPerQuery = colonnaPerQuery;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Integer doInBackground(Object... params) {
			int risultato = 0;
			Cursor cur;
			
			if(flagSpese) {
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);		
				dbcSpeseSostenute.openLettura();
				cur = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaliPerVoceOrdinatoPerImportoNoTrasf(dataInizio, dataFine, trasf);
			}
			else {
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);
				dbcEntrateIncassate.openLettura();
				cur = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaliPerVoceOrdinatoPerImportoNoTrasf("%", dataInizio, dataFine, trasf);//considero tutti i conti
			}
			
			if(cur == null || cur.getCount() == 0) {
				return risultato;
			}
			
			risultato = 1;
			//preparazione dati per il grafico
			int dimArr = cur.getCount() > 6 ? 6 : cur.getCount();
			arrEtichette = new String[dimArr];
			arrImporti = new double[dimArr];
			arrPercentuali = new double[dimArr];
			
			for(int i=0; cur.moveToNext(); i++) {
				String etichetta = cur.getString(cur.getColumnIndex("voce"));
				double importo = cur.getDouble(cur.getColumnIndex(colonnaPerQuery));
				importoTot += importo;
				if(i < 5) {
					arrEtichette[i] = etichetta;
					arrImporti[i] = importo;
				}
				else {
					arrEtichette[5] = "others";
					arrImporti[5] += importo;
				}
			}
			
			cur.close();		
			if(flagSpese) {
				dbcSpeseSostenute.close();
			}
			else {
				dbcEntrateIncassate.close();
			}
						
			return risultato;
		}
		
		protected void onPostExecute(Integer risultato) {
			if(risultato == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
			
			//creazione del grafico
			int colori[] = new int[arrEtichette.length];
			for(int i=0; i<colori.length; i++) {
				switch(i) {
				case 0:
					colori[i] = Color.RED;
					break;
				case 1:
					colori[i] = Color.YELLOW;
					break;
				case 2:
					colori[i] = Color.BLUE;
					break;
				case 3:
					colori[i] = Color.GREEN;
					break;
				case 4:
					colori[i] = Color.MAGENTA;
					break;
				case 5:
					colori[i] = Color.GRAY;
					break;
				}
			}
			
			CategorySeries distributionSeries = new CategorySeries("");
	        for(int i=0 ; i < arrEtichette.length; i++){
	        	arrPercentuali[i] = arrImporti[i] / importoTot;
	            distributionSeries.add(arrEtichette[i], arrPercentuali[i]);
	        }
	        
	        final DefaultRenderer defaultRenderer  = new DefaultRenderer();
	        for(int i = 0 ; i<arrEtichette.length; i++){
	            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
	            seriesRenderer.setColor(colori[i]);
	            
	            seriesRenderer.setDisplayChartValues(true);
	            NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
	            seriesRenderer.setChartValuesFormat(nf);
	            seriesRenderer.setChartValuesSpacing(5);

	            if(i == 0) {
	            	seriesRenderer.setHighlighted(true);
	            }

	            defaultRenderer.addSeriesRenderer(seriesRenderer);
	        }
	        
	        defaultRenderer.setZoomEnabled(true);
	        defaultRenderer.setZoomButtonsVisible(false);
	        defaultRenderer.setPanEnabled(true);
	        defaultRenderer.setApplyBackgroundColor(true);
            defaultRenderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
	        if(flagSpese) {
	        	defaultRenderer.setChartTitle(getString(R.string.statistiche_spese));
	        }
	        else {
	        	defaultRenderer.setChartTitle(getString(R.string.statistiche_entrate));
	        }
			defaultRenderer.setChartTitleTextSize(35 * moltTesto);
			defaultRenderer.setDisplayValues(true);       
	        defaultRenderer.setLabelsTextSize(30 * moltTesto);
	        defaultRenderer.setLabelsColor(Color.BLACK);
	        defaultRenderer.setLegendTextSize(25 * moltTesto);
		        
	        final GraphicalView mChart = ChartFactory.getPieChartView(StatisticheGrafico.this, distributionSeries , defaultRenderer);
	        defaultRenderer.setClickEnabled(true);
	        defaultRenderer.setSelectableBuffer(10);
	        mChart.setOnClickListener(new View.OnClickListener() {
				int evidPrec = 0;
				NumberFormat nfImporto = NumberFormat.getCurrencyInstance(Locale.getDefault());
				NumberFormat nfPerc = NumberFormat.getPercentInstance(Locale.getDefault());

				{
					nfImporto.setCurrency(currValuta);
					nfPerc.setMaximumFractionDigits(2);

				}
						
				@Override
				public void onClick(View v) {
					SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();
					if(seriesSelection != null) {
						int seriesIndex = seriesSelection.getPointIndex();
						
						defaultRenderer.getSeriesRendererAt(evidPrec).setHighlighted(false);
						defaultRenderer.getSeriesRendererAt(seriesIndex).setHighlighted(true);
						evidPrec = seriesIndex;
						mChart.repaint();
						
						Toast.makeText(StatisticheGrafico.this, getString(R.string.statistiche_voce) + ": " + arrEtichette[seriesIndex] + "\n" + getString(R.string.statistiche_importo) + ": " + nfImporto.format(arrImporti[seriesIndex]) + "\n" + getString(R.string.statistiche_percentuale) + ": " + nfPerc.format(arrPercentuali[seriesIndex]), Toast.LENGTH_SHORT).show();				
					}
				}
			});
	        
	        ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);   
		}
	}

	
	private class RecuperaBarchartTask extends AsyncTask<Object, Object, Integer> {
		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;
		String arrEtichette[];
		double arrImporti[];
		double importoTot = 0.0;
		
		boolean flagSpese;
		String colonnaPerQuery;
		long dataInizio;
		long dataFine;
		
		private RecuperaBarchartTask(boolean flagSpese, String colonnaPerQuery, long dataInizio, long dataFine) {
			this.flagSpese = flagSpese;
			this.colonnaPerQuery = colonnaPerQuery;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Integer doInBackground(Object... params) {
			int risultato = 0;
			Cursor cur;
			
			if(flagSpese) {
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);		
				dbcSpeseSostenute.openLettura();
				cur = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaliPerVoceOrdinatoPerImportoNoTrasf(dataInizio, dataFine, trasf); 
			}
			else {
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);
				dbcEntrateIncassate.openLettura();
				cur = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaliPerVoceOrdinatoPerImportoNoTrasf("%", dataInizio, dataFine, trasf); //utilizzo tutti i conti
			}
			
			if(cur == null || cur.getCount() == 0) {
				return risultato;
			}
			
			risultato = 1;
			//preparazione dati per il grafico
			int dimArr = cur.getCount() > 6 ? 6 : cur.getCount();
			arrEtichette = new String[dimArr];
			arrImporti = new double[dimArr];
			
			for(int i=0; cur.moveToNext(); i++) {
				String etichetta = cur.getString(cur.getColumnIndex("voce"));
				double importo = cur.getDouble(cur.getColumnIndex(colonnaPerQuery));
				importoTot += importo;
				if(i < 5) {
					arrEtichette[i] = etichetta;
					arrImporti[i] = importo;
				}
				else {
					arrEtichette[5] = "others";
					arrImporti[5] += importo;
				}
			}
			
			cur.close();		
			if(flagSpese) {
				dbcSpeseSostenute.close();
			}
			else {
				dbcEntrateIncassate.close();
			}
						
			return risultato;
		}

		protected void onPostExecute(Integer risultato) {
			if(risultato == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
					
			//creazione del grafico
			CategorySeries distributionSeries = new CategorySeries("");
	        for(int i=0 ; i < arrEtichette.length; i++){
	            distributionSeries.add(arrEtichette[i], arrImporti[i]);
	        }
	        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset(); 
	        dataSet.addSeries(distributionSeries.toXYSeries()); 
	        
	        XYSeriesRenderer renderer = new XYSeriesRenderer();
	        if(flagSpese) {
	        	renderer.setColor(Color.RED);
	        }
	        else {
	        	renderer.setColor(Color.BLUE);
	        }
	        renderer.setDisplayChartValues(false);
	        renderer.setLineWidth(10.5f);
	        
	        final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();   
		    mRenderer.addSeriesRenderer(renderer);	   
		    
		    mRenderer.setApplyBackgroundColor(true);
	        mRenderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
	        mRenderer.setMargins(new int[]{30, (int) (30 * moltTesto + 20), (int) (100 * moltTesto), 30});
	        mRenderer.setMarginsColor(Color.argb(255, 255, 255, 255));
	        mRenderer.setPanEnabled(true, true);
	        
		    if(flagSpese) {
		    	mRenderer.setChartTitle(getString(R.string.statistiche_spese));
		    }
		    else {
		    	mRenderer.setChartTitle(getString(R.string.statistiche_entrate));
		    }
	        mRenderer.setChartTitleTextSize(35 * moltTesto);
	        
	        mRenderer.setZoomButtonsVisible(false); 
	        mRenderer.setShowLegend(false);
	        mRenderer.setShowGridX(true);      
	        mRenderer.setShowGridY(false);
	        mRenderer.setGridColor(Color.argb(255, 210, 210, 210));
	        
	        mRenderer.setAntialiasing(true);
	        mRenderer.setBarSpacing(.5); 
	        mRenderer.setBarWidth(60f);
	        
	        mRenderer.setXAxisMin(0);
	        mRenderer.setYAxisMin(0);
	        mRenderer.setXAxisMax(7);
	        double max = Math.max(arrImporti[0], arrImporti[arrImporti.length - 1]);
	        max *= 1.1;
	        mRenderer.setYAxisMax(max);
	        
	        mRenderer.setLabelsColor(Color.BLACK);
	        mRenderer.setXLabels(0);
	        mRenderer.setLabelsTextSize(25 * moltTesto);
	        mRenderer.setXLabelsColor(Color.BLACK);
	        mRenderer.setXLabelsAngle(45f);
	        mRenderer.setXLabelsAlign(android.graphics.Paint.Align.LEFT);
	        mRenderer.setYLabelsColor(0, Color.BLACK);
	        mRenderer.setYLabelsAlign(android.graphics.Paint.Align.CENTER);        

	        for(int i=0; i<arrEtichette.length; i++) {
	        	mRenderer.addXTextLabel(i+1, arrEtichette[i]);
	        }
	         
	        final GraphicalView mChart = ChartFactory.getBarChartView(StatisticheGrafico.this, dataSet, mRenderer, DEFAULT);        
	        
	        mRenderer.setClickEnabled(true);
	        mRenderer.setSelectableBuffer(10);
	        mChart.setOnClickListener(new View.OnClickListener() {	
				NumberFormat nfImporto = NumberFormat.getCurrencyInstance(Locale.getDefault());
				NumberFormat nfPerc = NumberFormat.getPercentInstance(Locale.getDefault());

				{
					nfImporto.setCurrency(currValuta);
					nfPerc.setMaximumFractionDigits(2);

				}
				
				@Override
				public void onClick(View v) {
					SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();
					if(seriesSelection != null) {
						int seriesIndex = seriesSelection.getPointIndex();
							
						Toast.makeText(StatisticheGrafico.this, getString(R.string.statistiche_voce) + ": " + arrEtichette[seriesIndex] + "\n" + getString(R.string.statistiche_importo) + ": " + nfImporto.format(arrImporti[seriesIndex]) + "\n" + getString(R.string.statistiche_percentuale) + ": " + nfPerc.format(arrImporti[seriesIndex] / importoTot), Toast.LENGTH_SHORT).show();				
					}
				}
			});
	        
	        ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);
		}
	}
	
	
	private class RecuperaTimechartTask extends AsyncTask<Object, Object, Integer> {
		boolean flagSpese;
		String colonnaPerQuery;
		long dataInizio;
		long dataFine;

		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;	
		String arrEtichette[];
		double arrImporti[];
		double spesaMax;	
		
		private RecuperaTimechartTask(boolean flagSpese, String colonnaPerQuery, long dataInizio, long dataFine) {
			this.flagSpese = flagSpese;
			this.colonnaPerQuery = colonnaPerQuery;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Integer doInBackground(Object... params) {
			int risultato = 0;
			
			if(flagSpese) {
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);			
				dbcSpeseSostenute.openLettura();
			}
			else {
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);			
				dbcEntrateIncassate.openLettura();
			}
			
			GregorianCalendar dataComodo = new GregorianCalendar();
			GregorianCalendar dataFineComodo = new GregorianCalendar();
			dataComodo .setTimeInMillis(dataInizio);
			dataFineComodo.setTimeInMillis(dataFine);
			
			long diff = dataFine - dataInizio;
			int diffGiorni = (int) Math.ceil((diff / (24*60*60*1000)));
						
			if(diffGiorni <= 31 && diffGiorni>=0) { //visualizzazione giornaliera
				dataComodo.add(Calendar.DATE, -1);
				dataFineComodo.add(Calendar.DATE, +1);
				
				arrEtichette = new String[diffGiorni+4];
				arrImporti = new double[diffGiorni+4];
				
				Cursor curSpese;
				
				spesaMax = 0;
				SimpleDateFormat sdfMese = new SimpleDateFormat("MMM", miaLocale);
				for(int i=0; dataComodo.compareTo(dataFineComodo)<=0; i++, dataComodo.add(Calendar.DATE, +1)) {
					if(flagSpese) {
						curSpese = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataComodo.getTimeInMillis(), dataComodo.getTimeInMillis(), trasf);
					}
					else {
						curSpese = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataComodo.getTimeInMillis(), dataComodo.getTimeInMillis(), trasf);
					}
					
					if(curSpese.moveToFirst()) {
						arrImporti[i] = curSpese.getDouble(curSpese.getColumnIndex(colonnaPerQuery));
					}
					else {
						arrImporti[i] = 0.0;
					}

					arrEtichette[i] = Integer.valueOf(dataComodo.get(Calendar.DATE)).toString() + "\n" + sdfMese.format(new Date(dataComodo.getTimeInMillis()));
					
					if(arrImporti[i] > spesaMax) {
						spesaMax = arrImporti[i];
					}
					
					curSpese.close();
				}	
			}
			else if(diffGiorni>31 && diffGiorni<=365) { //visualizzazione mensile
				dataComodo.add(Calendar.MONTH, -1);
				dataFineComodo.add(Calendar.MONTH, +1);
				int diffAnni = dataFineComodo.get(GregorianCalendar.YEAR) - dataComodo.get(GregorianCalendar.YEAR);
				int diffMesi = dataFineComodo.get(GregorianCalendar.MONTH) - dataComodo.get(GregorianCalendar.MONTH);
				int mesi = (12 * diffAnni + diffMesi) + 1;
				
				arrEtichette = new String[mesi];
				arrImporti = new double[mesi];
				
				Cursor curSpese;
				
				spesaMax = 0;
				SimpleDateFormat sdfAnno = new SimpleDateFormat("yyyy", miaLocale);
				SimpleDateFormat sdfMese = new SimpleDateFormat("MMM", miaLocale);
				GregorianCalendar dataInizioMese = new GregorianCalendar(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
				GregorianCalendar dataFineMese = new GregorianCalendar(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
				for(int i=0; i<mesi; i++, dataComodo.add(Calendar.MONTH, +1)) {
					dataInizioMese.set(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
					dataFineMese.set(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
					dataFineMese.add(Calendar.MONTH, 1);
					dataFineMese.add(Calendar.DATE, -1);
					
					if(flagSpese) {
						curSpese = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataInizioMese.getTimeInMillis(), dataFineMese.getTimeInMillis(), trasf);
					}
					else {
						curSpese = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataInizioMese.getTimeInMillis(), dataFineMese.getTimeInMillis(), trasf);
					}
					if(curSpese.moveToFirst()) {
						arrImporti[i] = curSpese.getDouble(curSpese.getColumnIndex(colonnaPerQuery));
					}
					else {
						arrImporti[i] = 0.0;
					}
					
					arrEtichette[i] = sdfMese.format(new java.util.Date(dataComodo.getTimeInMillis())) + "\n" + sdfAnno.format(new Date(dataComodo.getTimeInMillis()));
					
					if(arrImporti[i] > spesaMax) {
						spesaMax = arrImporti[i];
					}
					
					curSpese.close();
				}
			}
			else if(diffGiorni>365) { //visualizzazione annuale
				dataComodo .add(Calendar.YEAR, -1);
				dataFineComodo.add(Calendar.YEAR, +1);
				int anni = dataFineComodo.get(GregorianCalendar.YEAR) - dataComodo.get(GregorianCalendar.YEAR) + 1;
				
				arrEtichette = new String[anni];
				arrImporti = new double[anni];
				
				Cursor curSpese;
				
				spesaMax = 0;
				SimpleDateFormat sdfAnno = new SimpleDateFormat("yyyy", miaLocale);
				GregorianCalendar dataInizioAnno = new GregorianCalendar(dataComodo.get(Calendar.YEAR), 0, 1);
				GregorianCalendar dataFineAnno = new GregorianCalendar(dataComodo.get(Calendar.YEAR), 11, 31);
				spesaMax = 0;
				for(int i=0; dataComodo.compareTo(dataFineComodo)<=0; i++, dataComodo.add(Calendar.YEAR, +1)) {
					dataInizioAnno.set(dataComodo.get(Calendar.YEAR), 0, 1);
					dataFineAnno.set(dataComodo.get(Calendar.YEAR), 11, 31);
					
					if(flagSpese) {
						curSpese = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataInizioAnno.getTimeInMillis(), dataFineAnno.getTimeInMillis(), trasf);
					}
					else {
						curSpese = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataInizioAnno.getTimeInMillis(), dataFineAnno.getTimeInMillis(), trasf);
					}
						
					if(curSpese.moveToFirst()) {
						arrImporti[i] = curSpese.getDouble(curSpese.getColumnIndex(colonnaPerQuery));
					}
					else {
						arrImporti[i] = 0.0;
					}
					
					arrEtichette[i] = sdfAnno.format(new java.util.Date(dataComodo.getTimeInMillis()));
					
					if(arrImporti[i] > spesaMax) {
						spesaMax = arrImporti[i];
					}
					
					curSpese.close();
				}
			}
			
			for(int i=0; i<arrImporti.length; i++) {
				if(arrImporti[i] != 0) {
					risultato = 1;
					break;
				}
			}
			
	        
	        if(flagSpese) {
	        	dbcSpeseSostenute.close();
	        }
	        else {
	        	dbcEntrateIncassate.close();
	        }
						
			return risultato;
		}
		
		protected void onPostExecute(Integer risultato) {
			if(risultato == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
			
			//creazione del grafico
			
			// Creating an  XYSeries for Income
	        XYSeries incomeSeries = new XYSeries(getString(R.string.statistiche_entrate));
	        // Creating an  XYSeries for Expense
	        XYSeries expenseSeries = new XYSeries(getString(R.string.statistiche_spese));
	        // Adding data to Income and Expense Series
	        for(int i=0;i<arrImporti.length;i++){
	            incomeSeries.add(i, arrImporti[i]);
	            expenseSeries.add(i,arrImporti[i]);
	        }
			
	        // Creating a dataset to hold each series
	        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	        // Adding Income Series to the dataset
	        dataset.addSeries(incomeSeries);
	        // Adding Expense Series to dataset
	        dataset.addSeries(expenseSeries);
			
	        // Creating XYSeriesRenderer to customize incomeSeries
	        XYSeriesRenderer incomeRenderer = new XYSeriesRenderer();
	        incomeRenderer.setColor(Color.GREEN);
	        incomeRenderer.setPointStyle(PointStyle.CIRCLE);
	        incomeRenderer.setFillPoints(true);
	        incomeRenderer.setLineWidth(2);
	        incomeRenderer.setDisplayChartValues(false);
	 
	        // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer expenseRenderer = new XYSeriesRenderer();
	        if(flagSpese) {
	        	expenseRenderer.setColor(Color.RED);
	        }
	        else {
	        	expenseRenderer.setColor(Color.BLUE);
	        }
	        expenseRenderer.setPointStyle(PointStyle.CIRCLE);
	        expenseRenderer.setFillPoints(true);
	        expenseRenderer.setLineWidth(2);
	        expenseRenderer.setDisplayChartValues(false);
			
	        // Creating a XYMultipleSeriesRenderer to customize the whole chart
	        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
	        multiRenderer.setXLabels(0);
	        if(flagSpese) {
	        	multiRenderer.setChartTitle(getString(R.string.statistiche_spese));
	        }
	        else {
	        	multiRenderer.setChartTitle(getString(R.string.statistiche_entrate));
	        }
	        multiRenderer.setZoomButtonsVisible(false);
	        multiRenderer.setBarSpacing(4);
	        
	        //mie ulteriori formattazioni
	        multiRenderer.setApplyBackgroundColor(true);
	        multiRenderer.setPanEnabled(true, false);
	        multiRenderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
	        multiRenderer.setMargins(new int[]{30, (int) (30 * moltTesto + 20), (int) (30 * moltTesto), 30});
	        multiRenderer.setMarginsColor(Color.argb(255, 255, 255, 255));
	        multiRenderer.setShowGridX(true);      
	        multiRenderer.setShowGridY(false);
	        multiRenderer.setGridColor(Color.argb(255, 210, 210, 210));
	        multiRenderer.setChartTitleTextSize(35 * moltTesto);
	        
	        multiRenderer.setXAxisMin(0);
	        multiRenderer.setXAxisMax(Math.min(6, arrEtichette.length));
	        multiRenderer.setYAxisMax(spesaMax * 1.1);        
	        
	        multiRenderer.setShowLegend(false);
	        multiRenderer.setAxisTitleTextSize(30);
	        multiRenderer.setLabelsTextSize(25 * moltTesto);
	        multiRenderer.setLabelsColor(Color.BLACK);
	        multiRenderer.setXLabelsColor(Color.BLACK);
	        multiRenderer.setYLabelsColor(0, Color.BLACK);
	        
	        for(int i=0;i<arrEtichette.length;i++){
	            multiRenderer.addXTextLabel(i,arrEtichette[i]);
	        }
	 
	        // Adding incomeRenderer and expenseRenderer to multipleRenderer
	        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
	        // should be same
	        multiRenderer.addSeriesRenderer(incomeRenderer);
	        multiRenderer.addSeriesRenderer(expenseRenderer);
			
	        // Specifying chart types to be drawn in the graph
	        // Number of data series and number of types should be same
	        // Order of data series and chart type will be same
	        String[] types = new String[] { LineChart.TYPE, BarChart.TYPE };
	 
	        // Creating a combined chart with the chart types specified in types array
	        final GraphicalView mChart = ChartFactory.getCombinedXYChartView(StatisticheGrafico.this, dataset, multiRenderer, types);
			
	        multiRenderer.setClickEnabled(true);
	        multiRenderer.setSelectableBuffer(10);
	        mChart.setOnClickListener(new View.OnClickListener() {
	        	NumberFormat nfImporto = NumberFormat.getCurrencyInstance(Locale.getDefault());
	        	
	        	{
	        		nfImporto.setCurrency(currValuta);
	        	}
	        	
	            @Override
	            public void onClick(View v) {
		            SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();
		 
		            if (seriesSelection != null) {
		                // Getting the clicked Month
		                String tempo = arrEtichette[(int)seriesSelection.getXValue()];
		                tempo = tempo.replace('\n', ' ');
		                // Getting the y value
		                double amount = seriesSelection.getValue();
	
						Toast.makeText(StatisticheGrafico.this, getString(R.string.statistiche_importo) + ": " + nfImporto.format(amount) + "\n" + getString(R.string.statistiche_tempo) + ": " + tempo, Toast.LENGTH_SHORT).show();				
		            }	
	            }
	        });
			        
	        ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);
		}
	}
		
	
	private class RecuperaTimechartConTagTask extends AsyncTask<Object, Object, Integer> {
		boolean flagSpese;
		String colonnaPerQuery;
		String voce;
		long dataInizio;
		long dataFine;

		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;
		String arrEtichette[];
		double arrImporti[];
		double spesaMax;
		
		private RecuperaTimechartConTagTask(boolean flagSpese, String colonnaPerQuery, String voce, long dataInizio, long dataFine) {
			this.flagSpese = flagSpese;
			this.colonnaPerQuery = colonnaPerQuery;
			this.voce = voce;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Integer doInBackground(Object... params) {
			int risultato = 0;
			
			if(flagSpese) {
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);			
				dbcSpeseSostenute.openLettura();
			}
			else {
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);			
				dbcEntrateIncassate.openLettura();
			}
			
			GregorianCalendar dataComodo = new GregorianCalendar();
			GregorianCalendar dataFineComodo = new GregorianCalendar();
			dataComodo .setTimeInMillis(dataInizio);
			dataFineComodo.setTimeInMillis(dataFine);
			
			long diff = dataFine - dataInizio;
			int diffGiorni = (int) Math.ceil((diff / (24*60*60*1000)));
						
			if(diffGiorni <= 31 && diffGiorni>=0) { //visualizzazione giornaliera
				dataComodo .add(Calendar.DATE, -1);
				dataFineComodo.add(Calendar.DATE, +1);
				
				arrEtichette = new String[diffGiorni+4];
				arrImporti = new double[diffGiorni+4];
				
				Cursor curSpese;
				
				spesaMax = 0;
				SimpleDateFormat sdfMese = new SimpleDateFormat("MMM", miaLocale);
				for(int i=0; dataComodo.compareTo(dataFineComodo)<=0; i++, dataComodo.add(Calendar.DATE, +1)) {
					if(flagSpese) {
						curSpese = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXTotale(dataComodo.getTimeInMillis(), dataComodo.getTimeInMillis(), voce);
					}
					else {
						curSpese = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXTotale("%", dataComodo.getTimeInMillis(), dataComodo.getTimeInMillis(), voce);
					}
					
					if(curSpese.moveToFirst()) {
						arrImporti[i] = curSpese.getDouble(curSpese.getColumnIndex(colonnaPerQuery));
					}
					else {
						arrImporti[i] = 0.0;
					}

					arrEtichette[i] = Integer.valueOf(dataComodo.get(Calendar.DATE)).toString() + "\n" + sdfMese.format(new Date(dataComodo.getTimeInMillis()));
					
					if(arrImporti[i] > spesaMax) {
						spesaMax = arrImporti[i];
					}
					
					curSpese.close();
				}	
			}
			else if(diffGiorni>31 && diffGiorni<=365) { //visualizzazione mensile
				dataComodo.add(Calendar.MONTH, -1);
				dataFineComodo.add(Calendar.MONTH, +1);
				int diffAnni = dataFineComodo.get(GregorianCalendar.YEAR) - dataComodo.get(GregorianCalendar.YEAR);
				int diffMesi = dataFineComodo.get(GregorianCalendar.MONTH) - dataComodo.get(GregorianCalendar.MONTH);
				int mesi = (12 * diffAnni + diffMesi) + 1;
				
				arrEtichette = new String[mesi];
				arrImporti = new double[mesi];
				
				Cursor curSpese;
				
				spesaMax = 0;
				SimpleDateFormat sdfAnno = new SimpleDateFormat("yyyy", miaLocale);
				SimpleDateFormat sdfMese = new SimpleDateFormat("MMM", miaLocale);
				GregorianCalendar dataInizioMese = new GregorianCalendar(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
				GregorianCalendar dataFineMese = new GregorianCalendar(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
				for(int i=0; i<mesi; i++, dataComodo.add(Calendar.MONTH, +1)) {
					dataInizioMese.set(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
					dataFineMese.set(dataComodo.get(Calendar.YEAR), dataComodo.get(Calendar.MONTH), 1);
					dataFineMese.add(Calendar.MONTH, 1);
					dataFineMese.add(Calendar.DATE, -1);
					
					if(flagSpese) {
						curSpese = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXTotale(dataInizioMese.getTimeInMillis(), dataFineMese.getTimeInMillis(), voce);
					}
					else {
						curSpese = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXTotale("%", dataInizioMese.getTimeInMillis(), dataFineMese.getTimeInMillis(), voce);
					}
					if(curSpese.moveToFirst()) {
						arrImporti[i] = curSpese.getDouble(curSpese.getColumnIndex(colonnaPerQuery));
					}
					else {
						arrImporti[i] = 0.0;
					}
					
					arrEtichette[i] = sdfMese.format(new java.util.Date(dataComodo.getTimeInMillis())) + "\n" + sdfAnno.format(new Date(dataComodo.getTimeInMillis()));
					
					if(arrImporti[i] > spesaMax) {
						spesaMax = arrImporti[i];
					}
					
					curSpese.close();
				}
			}
			else if(diffGiorni>365) { //visualizzazione annuale
				dataComodo .add(Calendar.YEAR, -1);
				dataFineComodo.add(Calendar.YEAR, +1);
				int anni = dataFineComodo.get(GregorianCalendar.YEAR) - dataComodo.get(GregorianCalendar.YEAR) + 1;
				
				arrEtichette = new String[anni];
				arrImporti = new double[anni];
				
				Cursor curSpese;
				
				spesaMax = 0;
				SimpleDateFormat sdfAnno = new SimpleDateFormat("yyyy", miaLocale);
				GregorianCalendar dataInizioAnno = new GregorianCalendar(dataComodo.get(Calendar.YEAR), 0, 1);
				GregorianCalendar dataFineAnno = new GregorianCalendar(dataComodo.get(Calendar.YEAR), 11, 31);
				for(int i=0; i<anni; i++, dataComodo.add(Calendar.YEAR, +1)) {
					dataInizioAnno.set(dataComodo.get(Calendar.YEAR), 0, 1);
					dataFineAnno.set(dataComodo.get(Calendar.YEAR), 11, 31);
					
					if(flagSpese) {
						curSpese = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXTotale(dataInizioAnno.getTimeInMillis(), dataFineAnno.getTimeInMillis(), voce);
					}
					else {
						curSpese = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXTotale("%", dataInizioAnno.getTimeInMillis(), dataFineAnno.getTimeInMillis(), voce);
					}
						
					if(curSpese.moveToFirst()) {
						arrImporti[i] = curSpese.getDouble(curSpese.getColumnIndex(colonnaPerQuery));
					}
					else {
						arrImporti[i] = 0.0;
					}
					
					arrEtichette[i] = sdfAnno.format(new java.util.Date(dataComodo.getTimeInMillis()));
					
					if(arrImporti[i] > spesaMax) {
						spesaMax = arrImporti[i];
					}
					
					curSpese.close();
				}
			}
			
			for(int i=0; i<arrImporti.length; i++) {
				if(arrImporti[i] != 0) {
					risultato = 1;
					break;
				}
			}

			if(flagSpese) {
	        		dbcSpeseSostenute.close();
	        	}
	        	else {
	        		dbcEntrateIncassate.close();
	        	}
						
			return risultato;
		}

		
		protected void onPostExecute(Integer risultato) {
			if(risultato == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
			
			//creazione del grafico
			
			// Creating an  XYSeries for Income
	        XYSeries incomeSeries = new XYSeries(getString(R.string.statistiche_entrate));
	        // Creating an  XYSeries for Expense
	        XYSeries expenseSeries = new XYSeries(getString(R.string.statistiche_spese));
	        // Adding data to Income and Expense Series
	        for(int i=0;i<arrImporti.length;i++){
	            incomeSeries.add(i, arrImporti[i]);
	            expenseSeries.add(i,arrImporti[i]);
	        }
			
	        // Creating a dataset to hold each series
	        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	        // Adding Income Series to the dataset
	        dataset.addSeries(incomeSeries);
	        // Adding Expense Series to dataset
	        dataset.addSeries(expenseSeries);
			
	        // Creating XYSeriesRenderer to customize incomeSeries
	        XYSeriesRenderer incomeRenderer = new XYSeriesRenderer();
	        incomeRenderer.setColor(Color.GREEN);
	        incomeRenderer.setPointStyle(PointStyle.CIRCLE);
	        incomeRenderer.setFillPoints(true);
	        incomeRenderer.setLineWidth(2);
	        incomeRenderer.setDisplayChartValues(false);
	 
	        // Creating XYSeriesRenderer to customize expenseSeries
	        XYSeriesRenderer expenseRenderer = new XYSeriesRenderer();
	        if(flagSpese) {
	        	expenseRenderer.setColor(Color.RED);
	        }
	        else {
	        	expenseRenderer.setColor(Color.BLUE);
	        }
	        expenseRenderer.setPointStyle(PointStyle.CIRCLE);
	        expenseRenderer.setFillPoints(true);
	        expenseRenderer.setLineWidth(2);
	        expenseRenderer.setDisplayChartValues(false);
			
	        // Creating a XYMultipleSeriesRenderer to customize the whole chart
	        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
	        multiRenderer.setXLabels(0);
	        if(flagSpese) {
	        	multiRenderer.setChartTitle(getString(R.string.statistiche_spese) + ": " + voce);
	        }
	        else {
	        	multiRenderer.setChartTitle(getString(R.string.statistiche_entrate) + ": " + voce);
	        }
	        multiRenderer.setZoomButtonsVisible(false);
	        multiRenderer.setBarSpacing(4);
	        
	        //ulteriori mie ulteriori formattazioni
		multiRenderer.setApplyBackgroundColor(true);
	        multiRenderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
	        multiRenderer.setMargins(new int[]{30, (int) (30 * moltTesto + 20), (int) (30 * moltTesto), 30});
	        multiRenderer.setMarginsColor(Color.argb(255, 255, 255, 255));
		multiRenderer.setPanEnabled(true, false);
		multiRenderer.setChartTitleTextSize(35 * moltTesto);

		multiRenderer.setShowGridX(true);      
	        multiRenderer.setShowGridY(false);
	        multiRenderer.setGridColor(Color.argb(255, 210, 210, 210));
      
	        multiRenderer.setAxisTitleTextSize(30);
	        multiRenderer.setXAxisMin(0);
	        multiRenderer.setXAxisMax(Math.min(6, arrEtichette.length));
	        multiRenderer.setYAxisMax(spesaMax * 1.1);   

		multiRenderer.setLabelsTextSize(25 * moltTesto);
	        multiRenderer.setLabelsColor(Color.BLACK);
	        multiRenderer.setXLabelsColor(Color.BLACK);
	        multiRenderer.setYLabelsColor(0, Color.BLACK);

	        multiRenderer.setShowLegend(false);
	                     
	        for(int i=0;i<arrEtichette.length;i++){
	            multiRenderer.addXTextLabel(i,arrEtichette[i]);
	        }
	 
	        // Adding incomeRenderer and expenseRenderer to multipleRenderer
	        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
	        // should be same
	        multiRenderer.addSeriesRenderer(incomeRenderer);
	        multiRenderer.addSeriesRenderer(expenseRenderer);
			
	        // Specifying chart types to be drawn in the graph
	        // Number of data series and number of types should be same
	        // Order of data series and chart type will be same
	        String[] types = new String[] { LineChart.TYPE, BarChart.TYPE };
	 
	        // Creating a combined chart with the chart types specified in types array
	        final GraphicalView mChart = ChartFactory.getCombinedXYChartView(StatisticheGrafico.this, dataset, multiRenderer, types);
			
	        multiRenderer.setClickEnabled(true);
	        multiRenderer.setSelectableBuffer(10);
	        mChart.setOnClickListener(new View.OnClickListener() {
	 	    NumberFormat nfImporto = NumberFormat.getCurrencyInstance(Locale.getDefault());
		    
		    {
			nfImporto.setCurrency(currValuta);
		    }

	            @Override
	            public void onClick(View v) {
	 
	            	SeriesSelection seriesSelection = mChart.getCurrentSeriesAndPoint();
	 
	            	if (seriesSelection != null) {
	                	// Getting the clicked Month
	                	String tempo = arrEtichette[(int)seriesSelection.getXValue()];
	                	tempo = tempo.replace('\n', ' ');
	                	// Getting the y value
	                	double amount = seriesSelection.getValue();
	                
	                	Toast.makeText(StatisticheGrafico.this, getString(R.string.statistiche_importo) + ": " + nfImporto.format(amount) + "\n" + getString(R.string.statistiche_tempo) + ": " + tempo, Toast.LENGTH_SHORT).show();				
	            	}
	            }
	 
	        });
			   
	        ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);
		}
	}
	
	
	private class RecuperaDialchartTask extends AsyncTask<Object, Object, Object> {
		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;
		boolean flagSpese;
		long dataInizio;
		long dataFine;
		String voce;
		double importoMin = 0;
		double importoMax = 0;
		double importoAvg = 0;
		
		private RecuperaDialchartTask(boolean flagSpese, String voce, long dataInizio, long dataFine) {
			this.flagSpese = flagSpese;
			this.voce = voce;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Object doInBackground(Object... params) {		
			if(flagSpese) {
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);			
				dbcSpeseSostenute.openLettura();
				importoMin = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXMin(dataInizio, dataFine, voce);
				importoMax = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXMax(dataInizio, dataFine, voce);
				importoAvg = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXAvg(dataInizio, dataFine, voce);
				dbcSpeseSostenute.close();
			}
			else {
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);			
				dbcEntrateIncassate.openLettura();
				importoMin = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXMin(dataInizio, dataFine, voce);
				importoMax = dbcEntrateIncassate.getEntrataIncassateIntervalloEntrataXMax(dataInizio, dataFine, voce);
				importoAvg = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXAvg(dataInizio, dataFine, voce);
				dbcEntrateIncassate.close();
			}
		
			return null;
		}
		
		protected void onPostExecute(Object risultato) {
			if(importoMin == 0 && importoMax == 0 && importoAvg == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
			
			CategorySeries category = new CategorySeries("");
		    category.add(getString(R.string.statistiche_dialchart_avg), importoAvg);
		    category.add(getString(R.string.statistiche_dialchart_min), importoMin);
		    category.add(getString(R.string.statistiche_dialchart_max), importoMax);
		    
		    DialRenderer renderer = new DialRenderer();
		    renderer.setMargins(new int[] {20, 30, 15, 0});
		    
		    renderer.setChartTitleTextSize(35 * moltTesto);
		    renderer.setChartTitle(getString(R.string.statistiche_dialchart) + ": " + voce);
		    
		    renderer.setShowLabels(true);
		    renderer.setLabelsTextSize(30);
		    renderer.setLabelsColor(Color.BLACK);
		    renderer.setLegendTextSize(35 * moltTesto);
		    
		    renderer.setVisualTypes(new DialRenderer.Type[] {Type.ARROW, Type.NEEDLE, Type.NEEDLE});
		    renderer.setMinValue(importoMin);
		    renderer.setMaxValue(importoMax);
		    
		    SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		    r.setColor(Color.BLUE);
		    renderer.addSeriesRenderer(r);
		    r = new SimpleSeriesRenderer();
		    r.setColor(Color.rgb(0, 150, 0));
		    renderer.addSeriesRenderer(r);
		    r = new SimpleSeriesRenderer();
		    r.setColor(Color.GREEN);
		    renderer.addSeriesRenderer(r);
		    
		    final GraphicalView mChart = ChartFactory.getDialChartView(StatisticheGrafico.this, category, renderer);
		    ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);
		}
	}
	
	
	private class RecuperaRangebarchartTask extends AsyncTask<Object, Object, Integer> {
		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;
		boolean flagSpese;
		long dataInizio;
		long dataFine;
		String arrEtichette[];
		double minValues[];
		double maxValues[];
		double importoMin = Double.MAX_VALUE;
		double importoMax = Double.MIN_VALUE;

		private RecuperaRangebarchartTask(boolean flagSpese, long dataInizio, long dataFine) {
			this.flagSpese = flagSpese;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Integer doInBackground(Object... params) {
			int risultato = 0;

			Cursor cur;
			
			if(flagSpese) {
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);	
				dbcSpeseSostenute.openLettura();
				cur = dbcSpeseSostenute.getSpeseSostenuteIntervalloMinMaxPerVoce(dataInizio, dataFine, trasf);
			}
			else {
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);
				dbcEntrateIncassate.openLettura();
				cur = dbcEntrateIncassate.getEntrateIncassateIntervalloMinMaxPerVoce(dataInizio, dataFine, trasf);
			}

			if(cur!= null && cur.getCount() == 0) {
				return risultato;
			}

			int dimArr = cur.getCount();
			arrEtichette = new String[dimArr];
			minValues = new double[dimArr];
			maxValues = new double[dimArr];
			importoMin = Double.MAX_VALUE;
			importoMax = Double.MIN_VALUE;
			for(int i=0; cur.moveToNext(); i++) {
				arrEtichette[i] = cur.getString(cur.getColumnIndex("voce"));
				minValues[i] = cur.getDouble(cur.getColumnIndex("min"));
				maxValues[i] = cur.getDouble(cur.getColumnIndex("max"));
				
				if(minValues[i] < importoMin) {
					importoMin = minValues[i];
				}
				if(maxValues[i] > importoMax) {
					importoMax = maxValues[i];
				}
			}
					
			cur.close();
	        	if(flagSpese) {
	        		dbcSpeseSostenute.close();
	        	}
	        	else {
	        		dbcEntrateIncassate.close();
	        	}

			risultato = 1;
		
			return risultato;
		}

		protected void onPostExecute(Integer risultato) {
			if(risultato == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
			
			//creazione del grafico
			RangeCategorySeries distributionSeries = new RangeCategorySeries("");
        	for(int i=0 ; i < minValues.length; i++){
            		distributionSeries.add(minValues[i], maxValues[i]);
        	}
        	XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset(); 
        	dataSet.addSeries(distributionSeries.toXYSeries()); 
        
        	int[] colors = new int[] { Color.BLACK };
        	XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		
			int length = colors.length;    
        	for (int i = 0; i < length; i++) {
        		SimpleSeriesRenderer r = new SimpleSeriesRenderer();
        		r.setColor(colors[i]);
        		r.setDisplayChartValues(true);
        		r.setChartValuesTextAlign(android.graphics.Paint.Align.CENTER);
        		r.setChartValuesTextSize(20);
        		r.setChartValuesSpacing(3);
	       	 	r.setGradientEnabled(true);
	        	if(flagSpese) {
	        		r.setGradientStart(-20, Color.BLUE);
	        		r.setGradientStop(20, Color.RED);
	        	}
	        	else {
	        		r.setGradientStart(-20, Color.GREEN);
	        		r.setGradientStop(20, Color.BLUE);
	        	}
          
          	renderer.addSeriesRenderer(r);
        	}

			renderer.setMargins(new int[] {30, (int) (30 * moltTesto + 20), (int) (100 * moltTesto), 30});
			renderer.setApplyBackgroundColor(true);
        	renderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
        	renderer.setMarginsColor(Color.argb(255, 255, 255, 255));
			renderer.setAxesColor(Color.GRAY);
			renderer.setZoomRate(0.9f);
			
			renderer.setChartTitleTextSize(35 * moltTesto);
			if(flagSpese) {
        		renderer.setChartTitle(getString(R.string.statistiche_spese) + " " + getString(R.string.statistiche_rangebarchart));
        	}
        	else {
        		renderer.setChartTitle(getString(R.string.statistiche_entrate) + " " + getString(R.string.statistiche_rangebarchart));
        	}
	        
        	renderer.setLabelsTextSize(20 * moltTesto);
			renderer.setLabelsColor(Color.BLACK);
			renderer.setXLabels(0);
			renderer.setLabelsTextSize(25 * moltTesto);
        	renderer.setXLabelsColor(Color.BLACK);
        	renderer.setXLabelsAngle(45f);
        	renderer.setXLabelsAlign(android.graphics.Paint.Align.LEFT);
        	renderer.setYLabelsColor(0, Color.BLACK);
        	renderer.setYLabelsAlign(android.graphics.Paint.Align.CENTER);
        	for(int i=0; i<arrEtichette.length; i++) {
        		renderer.addXTextLabel(i+1, arrEtichette[i]);
        	}	 

        	renderer.setShowLegend(false);
			renderer.setShowGridX(true);
        	renderer.setGridColor(Color.argb(255, 210, 210, 210));
	        
        	renderer.setXAxisMin(0);
        	renderer.setXAxisMax(7);
        	renderer.setYAxisMin(importoMin - (importoMax - importoMin) * 0.05);
        	renderer.setYAxisMax(importoMax * 1.1);
        
        	renderer.setBarSpacing(0.5);
        	renderer.setBarWidth(60);

	        final GraphicalView mChart = ChartFactory.getRangeBarChartView(StatisticheGrafico.this, dataSet, renderer, DEFAULT);			
			
	        ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);        
		}
	}
	
	
	private class RecuperaDoughnutchartTask extends AsyncTask<Object, Object, Integer> {
		DBCSpeseVoci dbcSpeseVoci;
		DBCEntrateVoci dbcEntrateVoci;
		DBCSpeseSostenute dbcSpeseSostenute;
		DBCEntrateIncassate dbcEntrateIncassate;
		
		boolean flagSpese;
		String colonnaPerQuery;
		long dataInizio;
		long dataFine;
		long dataInizio2;
		long dataFine2;
		
		String arrEtichetteCompleto[];
		double arrImportiCompleto[];
		double arrPercentualiCompleto[];	
		String arrEtichetteCompleto2[];
		double arrImportiCompleto2[];
		double arrPercentualiCompleto2[];

		String arrEtichette[];
		double arrImporti[];
		double arrPercentuali[];
		String arrEtichette2[];
		double arrImporti2[];
		double arrPercentuali2[];
		
		private RecuperaDoughnutchartTask(boolean flagSpese, String colonnaPerQuery, long dataInizio, long dataFine, long dataInizio2, long dataFine2) {
			this.flagSpese = flagSpese;
			this.colonnaPerQuery = colonnaPerQuery;
			this.dataInizio = dataInizio;
			this.dataFine = dataFine;
			this.dataInizio2 = dataInizio2;
			this.dataFine2 = dataFine2;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_caricamento));
			LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
			llContenitoreGrafico.setVisibility(View.GONE);
		}

		protected Integer doInBackground(Object... params) {
			int risultato = 0;
			
			if(flagSpese) {
				dbcSpeseVoci = new DBCSpeseVoci(StatisticheGrafico.this);
				dbcSpeseSostenute = new DBCSpeseSostenute(StatisticheGrafico.this);
				dbcSpeseVoci.openLettura();
				dbcSpeseSostenute.openLettura();
				
				Cursor curVoci = dbcSpeseVoci.getTutteLeVociNoTrasf(trasf);
				if(curVoci != null && curVoci.getCount() > 0) {
					int dimArr = curVoci.getCount();
					arrEtichetteCompleto = new String[dimArr];
					arrEtichetteCompleto2 = new String[dimArr];
					arrImportiCompleto = new double[dimArr];
					arrImportiCompleto2 = new double[dimArr];
					arrPercentualiCompleto = new double[dimArr];
					arrPercentualiCompleto2 = new double[dimArr];
					
					Cursor curTot = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataInizio, dataFine, trasf);
					curTot.moveToFirst();
					double importoTot = curTot.getDouble(curTot.getColumnIndex(colonnaPerQuery));
					
					curTot = dbcSpeseSostenute.getSpeseSostenuteIntervalloTotaleNoTrasf("%", dataInizio2, dataFine2, trasf);
					curTot.moveToFirst();
					double importoTot2 = curTot.getDouble(curTot.getColumnIndex(colonnaPerQuery));
					curTot.close();
					
					int valoriPositivi = 0;
					for(int i=0; curVoci.moveToNext(); i++) {
						String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
						
						Cursor curTotVoce = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXTotale(dataInizio, dataFine, voce);
						curTotVoce.moveToFirst();
						arrEtichetteCompleto[i] = voce;
						arrImportiCompleto[i] = curTotVoce.getDouble(curTotVoce.getColumnIndex(colonnaPerQuery));
						arrPercentualiCompleto[i] = arrImportiCompleto[i] / importoTot;
						
						curTotVoce = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXTotale(dataInizio2, dataFine2, voce);
						curTotVoce.moveToFirst();
						arrEtichetteCompleto2[i] = voce;
						arrImportiCompleto2[i] = curTotVoce.getDouble(curTotVoce.getColumnIndex(colonnaPerQuery));
						arrPercentualiCompleto2[i] = arrImportiCompleto2[i] / importoTot2;
						
						if(arrImportiCompleto[i] > 0 || arrImportiCompleto2[i] > 0) {
							valoriPositivi++;
						}
						
						curTotVoce.close();
					}
					
					curVoci.close();
					dbcSpeseVoci.close();
					dbcSpeseSostenute.close();
					
					if(valoriPositivi > 0) {
						arrEtichette = new String[valoriPositivi];
						arrEtichette2 = new String[valoriPositivi];
						arrImporti = new double[valoriPositivi];
						arrImporti2 = new double[valoriPositivi];
						arrPercentuali = new double[valoriPositivi];
						arrPercentuali2 = new double[valoriPositivi];
						
						for(int i=0, j=0; i<arrEtichetteCompleto.length; i++) {
							if(arrImportiCompleto[i] > 0 || arrImportiCompleto2[i] > 0) {
								arrEtichette[j] = arrEtichetteCompleto[i];
								arrEtichette2[j] = arrEtichetteCompleto2[i];
								arrImporti[j] = arrImportiCompleto[i];
								arrImporti2[j] = arrImportiCompleto2[i];
								arrPercentuali[j] = arrPercentualiCompleto[i];
								arrPercentuali2[j] = arrPercentualiCompleto2[i];
								
								j++;
							}
						}
						
						risultato = 1;
					}
					else {
						risultato = 0;
					}
				}	
			}
			else {
				dbcEntrateVoci = new DBCEntrateVoci(StatisticheGrafico.this);
				dbcEntrateIncassate = new DBCEntrateIncassate(StatisticheGrafico.this);
				dbcEntrateVoci.openLettura();
				dbcEntrateIncassate.openLettura();
				
				Cursor curVoci = dbcEntrateVoci.getTutteLeVociNoTrasf(trasf);
				if(curVoci != null && curVoci.getCount() > 0) {
					int dimArr = curVoci.getCount();
					arrEtichetteCompleto = new String[dimArr];
					arrEtichetteCompleto2 = new String[dimArr];
					arrImportiCompleto = new double[dimArr];
					arrImportiCompleto2 = new double[dimArr];
					arrPercentualiCompleto = new double[dimArr];
					arrPercentualiCompleto2 = new double[dimArr];
					
					Cursor curTot = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataInizio, dataFine, trasf);
					curTot.moveToFirst();
					double importoTot = curTot.getDouble(curTot.getColumnIndex(colonnaPerQuery));
					
					curTot = dbcEntrateIncassate.getEntrateIncassateIntervalloTotaleNoTrasf("%", dataInizio2, dataFine2, trasf);
					curTot.moveToFirst();
					double importoTot2 = curTot.getDouble(curTot.getColumnIndex(colonnaPerQuery));
					curTot.close();
					
					int valoriPositivi = 0;
					for(int i=0; curVoci.moveToNext(); i++) {
						String voce = curVoci.getString(curVoci.getColumnIndex("voce"));
						
						Cursor curTotVoce = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXTotale("%", dataInizio, dataFine, voce);
						curTotVoce.moveToFirst();
						arrEtichetteCompleto[i] = voce;
						arrImportiCompleto[i] = curTotVoce.getDouble(curTotVoce.getColumnIndex(colonnaPerQuery));
						arrPercentualiCompleto[i] = arrImportiCompleto[i] / importoTot;
						
						curTotVoce = dbcEntrateIncassate.getEntrateIncassateIntervalloEntrataXTotale("%", dataInizio2, dataFine2, voce);
						curTotVoce.moveToFirst();
						arrEtichetteCompleto2[i] = voce;
						arrImportiCompleto2[i] = curTotVoce.getDouble(curTotVoce.getColumnIndex(colonnaPerQuery));
						arrPercentualiCompleto2[i] = arrImportiCompleto2[i] / importoTot2;
						
						if(arrImportiCompleto[i] != 0 || arrImportiCompleto2[i] != 0) {
							valoriPositivi++;
						}
						
						curTotVoce.close();
					}
					
					curVoci.close();
					dbcEntrateVoci.close();
					dbcEntrateIncassate.close();
					
					if(valoriPositivi > 0) {
						arrEtichette = new String[valoriPositivi];
						arrEtichette2 = new String[valoriPositivi];
						arrImporti = new double[valoriPositivi];
						arrImporti2 = new double[valoriPositivi];
						arrPercentuali = new double[valoriPositivi];
						arrPercentuali2 = new double[valoriPositivi];
						
						for(int i=0, j=0; i<arrEtichetteCompleto.length; i++) {
							if(arrImportiCompleto[i] != 0 || arrImportiCompleto2[i] != 0) {
								arrEtichette[j] = arrEtichetteCompleto[i];
								arrEtichette2[j] = arrEtichetteCompleto2[i];
								arrImporti[j] = arrImportiCompleto[i];
								arrImporti2[j] = arrImportiCompleto2[i];
								arrPercentuali[j] = arrPercentualiCompleto[i];
								arrPercentuali2[j] = arrPercentualiCompleto2[i];
								
								j++;
							}
						}
						
						risultato = 1;
					}
					else {
						risultato = 0;
					}
				}		
			}
			

			return risultato;
		}
		
		protected void onPostExecute(Integer risultato) {
			if(risultato == 0) {
				findViewById(R.id.statistiche_grafico_tvNoVoci).setVisibility(View.VISIBLE);
				findViewById(R.id.statistiche_grafico_tvG).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_errore_noElementi));
				
				return;
			}
			
			//creazione del grafico
			int colori[] = new int[arrEtichette.length];		
			for(int i=0; i<colori.length; i++) {
				switch(i) {
				case 0:
					colori[i] = Color.RED;
					break;
				case 1:
					colori[i] = Color.YELLOW;
					break;
				case 2:
					colori[i] = Color.BLUE;
					break;
				case 3:
					colori[i] = Color.GREEN;
					break;
				case 4:
					colori[i] = Color.MAGENTA;
					break;
				case 5:
					colori[i] = Color.CYAN;
					break;
				case 6:
					colori[i] = Color.BLACK;
					break;
				case 7:
					colori[i] = Color.GRAY;
					break;
				case 8:
					colori[i] = Color.WHITE;
					break;
				default:
					colori[i] = Color.rgb((int) Math.random() * 256, (int) Math.random() * 256, (int) Math.random() * 256); 
					break;				
				}
			}
			
			List<double[]> values = new ArrayList<double[]>();
		    values.add(arrPercentuali);
		    values.add(arrPercentuali2);
		    List<String[]> titles = new ArrayList<String[]>();
		    titles.add(arrEtichette);
		    titles.add(arrEtichette2);
			
		    DefaultRenderer renderer = new DefaultRenderer();
 		    renderer.setMargins(new int[] { 20, 30, 15, 0 });
		    renderer.setApplyBackgroundColor(true);
		    renderer.setBackgroundColor(Color.argb(255, 255, 255, 255));

		    renderer.setDisplayValues(true);
		    renderer.setZoomEnabled(true);
		    renderer.setZoomButtonsVisible(false);
		    renderer.setPanEnabled(true);

		    if(flagSpese) {
	        	renderer.setChartTitle(getString(R.string.statistiche_spese));
	            }
	            else {
	        	renderer.setChartTitle(getString(R.string.statistiche_entrate));
	            }
	            renderer.setChartTitleTextSize(35 * moltTesto);
		    renderer.setLabelsTextSize(30 * moltTesto);
		    renderer.setLabelsColor(Color.BLACK);
		    renderer.setLegendTextSize(30 * moltTesto);
		    
	        for(int i = 0 ; i<arrEtichette.length; i++){
	            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
	            seriesRenderer.setColor(colori[i]);
	            
	            renderer.addSeriesRenderer(seriesRenderer);
	        }

		    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", miaLocale);
		    MultipleCategorySeries series = new MultipleCategorySeries("Doughnut chart");
		    series.add(getString(R.string.statistiche_esterno) + ": " + sdf.format(new Date(dataInizio)) + " - "  + sdf.format(new Date(dataFine)), titles.get(0), values.get(0));
		    series.add(getString(R.string.statistiche_interno) + ": " + sdf.format(new Date(dataInizio2)) + " - "  + sdf.format(new Date(dataFine2)), titles.get(1), values.get(1));
		    
		    final GraphicalView mChart = ChartFactory.getDoughnutChartView(StatisticheGrafico.this, series, renderer);
    
	        ((TextView) findViewById(R.id.statistiche_grafico_tvIndicazioni)).setText(getString(R.string.statistiche_indicazioni));
	        LinearLayout llContenitoreGrafico = (LinearLayout) findViewById(R.id.statistiche_grafico_llContenitoreGrafico);
	        llContenitoreGrafico.setVisibility(View.VISIBLE);
	        llContenitoreGrafico.addView(mChart);
		}
	}
	
	
	//variabili di istanza
	private Currency currValuta;	
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	private String trasf; // voce per i trasferimenti nella lingua corrente
    private float moltTesto;
}
