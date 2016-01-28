package com.flingsoftware.personalbudget.app;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiActivity.*;
import static com.flingsoftware.personalbudget.app.FragmentBudget.ROW_ID;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.TIPO_OPERAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_ELIMINAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_MODIFICA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.*;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiSuoni;
import com.flingsoftware.personalbudget.customviews.MioToast;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseIntArray;
import 	android.view.animation.DecelerateInterpolator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.WindowManager;
import android.view.Window;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.support.v7.app.ActionBarActivity;


public class BudgetDettaglio extends ActionBarActivity {
	
	//costanti
	public interface CostantiPubbliche {
		String BUDGET_VOCE = "budget_voce";
		String BUDGET_RIPETIZIONE = "budget_ripetizione";
		String BUDGET_IMPORTO ="importo_valprin";
		String BUDGET_DATA_INIZIO = "budget_data_inizio";
		String BUDGET_DATA_FINE = "budget_data_fine";
		String BUDGET_AGGIUNGERE_RIMANENZA = "aggiungere_rimanenza";
		String BUDGET_BUDGET_INIZIALE = "budget_iniziale";
		String BUDGET_ULTIMO_AGGIUNTO = "ultimo_aggiunto";
		String BUDGET_ICONAID = "icona_id";
	}
	
	private static final String CHIAMANTE = "chiamante";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.budget_dettaglio);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		Window w = getWindow();
	    w.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		ricavaValuta();
		
		//recupero il campo row_id passato dall'Activity chiamante
		Bundle extras = getIntent().getExtras();
		id = extras.getLong(ROW_ID);
		chiamante = extras.getInt(CHIAMANTE);
		
		tipiBudget = getResources().getStringArray(R.array.ripetizioni_budget);
		
		//ottengo reference ai bottoni e imposto i listener
		ImageButton bSpeseIncluse = (ImageButton) findViewById(R.id.budget_dettaglio_bSpeseIncluse);
		bSpeseIncluse.setOnClickListener(bSpeseIncluseListener);
		ImageButton bRiepilogoBudget = (ImageButton) findViewById(R.id.budget_dettaglio_bRiepilogoBudget);
		bRiepilogoBudget.setOnClickListener(bRiepilogoBudgetListener);
			
		//recupero i dettagli del budget da un thread separato
		new RecuperaDettagliBudgetTask().execute(id);
		
		new CaricaSuoniTask().execute();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public void onBackPressed() {
		if(modifiche) {
			setResult(Activity.RESULT_OK);
		}
		
		super.onBackPressed();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(suoniAbilitati && conferma) {
			soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_CANCELLAZIONE), 1, 1, 1, 0, 1f);
		}
	}


	// ricavo la valuta principale dal file delle preferenze
	private void ricavaValuta() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String valuta = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValutaPrinc = Currency.getInstance(valuta);
	}


	//AsyncTask per caricare la HashMap con i suoni dell'app
	private class CaricaSuoniTask extends AsyncTask<Object, Object, Boolean> {
			
		protected Boolean doInBackground(Object... params) {		
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(BudgetDettaglio.this);
			boolean abilitazioneSuoni = pref.getBoolean(CostantiPreferenze.SUONI_ABILITATI, false);
			if(abilitazioneSuoni) {
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				mappaSuoni = new SparseIntArray(1);
				mappaSuoni.put(CostantiSuoni.SUONO_CANCELLAZIONE, soundPool.load(BudgetDettaglio.this, R.raw.cancellazione, 1));
			}
			
			return abilitazioneSuoni;
		}
			
		protected void onPostExecute(Boolean result) {
			//una volta caricati i suoni nella Map l'app � pronta ad utilizzarli, non prima
			suoniAbilitati = result;
		}
	}
		
		
	//AsyncTask per recuperare i dettagli del budget da visualizzare
	private class RecuperaDettagliBudgetTask extends AsyncTask<Long, Object, Cursor> {
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDettaglio.this);
		
		protected Cursor doInBackground(Long... params) {
			dbcSpeseBudget.openLettura();
			
			return dbcSpeseBudget.getSpesaBudget(params[0]);
		}
		
		protected void onPostExecute(Cursor curBudget) {
			curBudget.moveToFirst();
			
			//recupero i campi da visualizzare dal database
			voceBudget = curBudget.getString(curBudget.getColumnIndex("voce"));
			if(voceBudget.endsWith(",")) {
				voceBudget = voceBudget.substring(0, voceBudget.length() - 1);
			}
			
			ripetizione = curBudget.getString(curBudget.getColumnIndex("ripetizione"));
			importoBudget = curBudget.getDouble(curBudget.getColumnIndex("importo_valprin"));
			dataInizioBudgetLong = curBudget.getLong(curBudget.getColumnIndex("data_inizio"));
			dataFineBudgetLong = curBudget.getLong(curBudget.getColumnIndex("data_fine"));
			aggiungereRimanenza = curBudget.getInt(curBudget.getColumnIndex("aggiungere_rimanenza"));
			spesaSost = curBudget.getDouble(curBudget.getColumnIndex("spesa_sost"));
			double risparmio = curBudget.getDouble(curBudget.getColumnIndex("risparmio"));
			budgetIniziale = curBudget.getLong(curBudget.getColumnIndex("budget_iniziale"));
			ultimoAggiunto = curBudget.getInt(curBudget.getColumnIndex("ultimo_aggiunto"));
			
			//aggiorno la UI
			String tipoBudget = new String();
			if(ripetizione.equals("una_tantum")) {
				tipoBudget = tipiBudget[0];
			}
			else if(ripetizione.equals("giornaliero")) {
				tipoBudget = tipiBudget[1];
			}
			else if(ripetizione.equals("settimanale")) {
				tipoBudget = tipiBudget[2];
			}
			else if(ripetizione.equals("bisettimanale")) {
				tipoBudget = tipiBudget[3];
			}
			else if(ripetizione.equals("mensile")) {
				tipoBudget = tipiBudget[4];
			}
			else if(ripetizione.equals("annuale")) {
				tipoBudget = tipiBudget[5];
			}
			
			
			NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
			nf.setCurrency(currValutaPrinc);
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);
			NumberFormat nfRidotto = NumberFormat.getInstance(Locale.getDefault());
			nfRidotto.setMaximumFractionDigits(2);
			
			((TextView) findViewById(R.id.budget_dettaglio_tvTipoBudget)).setText(tipoBudget);
			((TextView) findViewById(R.id.budget_dettaglio_tvImporto)).setText(nf.format(importoBudget));
			((TextView) findViewById(R.id.budget_dettaglio_tvDataScadenza)).setText(df.format(new Date(dataFineBudgetLong)));
			((TextView) findViewById(R.id.budget_dettaglio_tvDescrizione)).setText(voceBudget);
			
			// testo scorrevole
			((TextView) findViewById(R.id.budget_dettaglio_tvDescrizione)).setEllipsize(TextUtils.TruncateAt.MARQUEE);
			((TextView) findViewById(R.id.budget_dettaglio_tvDescrizione)).setSingleLine(true);
			((TextView) findViewById(R.id.budget_dettaglio_tvDescrizione)).setMarqueeRepeatLimit(5);
			findViewById(R.id.budget_dettaglio_tvDescrizione).setSelected(true);
			
			((TextView) findViewById(R.id.budget_dettaglio_tvUtilizzato)).setText(nfRidotto.format(spesaSost) + " " + getString(R.string.budget_dettaglio_utilizzato));
			((TextView) findViewById(R.id.budget_dettaglio_tvRimanente)).setText(nfRidotto.format(risparmio) + " " + getString(R.string.budget_dettaglio_rimanente));
			((TextView) findViewById(R.id.budget_dettaglio_tvDataInizio)).setText(df.format(new Date(dataInizioBudgetLong)));
			((TextView) findViewById(R.id.budget_dettaglio_tvDataFine)).setText(df.format(new Date(dataFineBudgetLong)));
			
			final ProgressBar pbBudget = (ProgressBar) findViewById(R.id.budget_dettaglio_pbProgresso);
			pbBudget.setMax((int) importoBudget);
            if(spesaSost>=importoBudget) {
                pbBudget.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_accent));
            }
            else {
                pbBudget.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar_standard));
            }
			
			if(aggiungereRimanenza == 0) {
				((ToggleButton) findViewById(R.id.budget_dettaglio_tbAggRim)).setChecked(false);
			}
			else {
				((ToggleButton) findViewById(R.id.budget_dettaglio_tbAggRim)).setChecked(true);
			}
			
			if(ripetizione.equals("una_tantum")) {
				findViewById(R.id.budget_dettaglio_bRiepilogoBudget).setEnabled(false);
			}
			
			//chiudo il connector
			dbcSpeseBudget.close();
			
			// riempio la progressbar_standard con un animazione fluida
			ObjectAnimator animation = ObjectAnimator.ofInt(pbBudget, "progress", 0, (int) spesaSost);
			animation.setDuration(1500); 
		    animation.setInterpolator(new DecelerateInterpolator());
		    animation.start();
		    
		    //carico icona spesa
		    new CaricaIconaTask().execute((Object[]) null);
		}
	}
	
	
	//AsyncTask per caricare la bitmap dell'icona (dimensioni esatte)
	private class CaricaIconaTask extends AsyncTask<Object, Object, Object> {
		
		protected Object doInBackground(Object... params) {	    	
			DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(BudgetDettaglio.this);
			dbcSpeseVoci.openLettura();
			
			Bitmap miaBitmap;
			Cursor curVoci = dbcSpeseVoci.getVociContenentiStringa(voceBudget);
			if(curVoci.moveToFirst()) {
				Integer icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
				iconaId = ListViewIconeVeloce.arrIconeId[icona];
				miaBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaId, 50, 50);
			}
			else {
				iconaId = R.drawable.img_budget;
				miaBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaId, 50, 50);
			}
			
			curVoci.close();
			dbcSpeseVoci.close();
			
			return miaBitmap;
		}
		
		protected void onPostExecute(Object result) {
			Bitmap miaBitmap = (Bitmap) result;
			
			((ImageView) findViewById(R.id.budget_dettaglio_ivIcona)).setImageBitmap(miaBitmap);
		}
	}
		
	    
	//listener per il bottone bSpeseIncluse
	public OnClickListener bSpeseIncluseListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent speseIncluse = new Intent(BudgetDettaglio.this, BudgetSpeseIncluse.class);
			speseIncluse.putExtra(ROW_ID, id);
			speseIncluse.putExtra(CostantiPubbliche.BUDGET_VOCE, voceBudget);
			speseIncluse.putExtra(CostantiPubbliche.BUDGET_DATA_INIZIO, dataInizioBudgetLong);
			speseIncluse.putExtra(CostantiPubbliche.BUDGET_DATA_FINE, dataFineBudgetLong);
			
			startActivityForResult(speseIncluse, ACTIVITY_BUDGET_SPESEINCLUSE);

		}
	};
	
	
	//listener per il bottone bRiepilogoBudget
	public OnClickListener bRiepilogoBudgetListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent budgetAnaloghi = new Intent(BudgetDettaglio.this, BudgetBudgetAnaloghi.class);
			budgetAnaloghi.putExtra(CostantiPubbliche.BUDGET_BUDGET_INIZIALE, budgetIniziale);
			budgetAnaloghi.putExtra(CostantiPubbliche.BUDGET_ICONAID, iconaId);
			
			startActivityForResult(budgetAnaloghi, ACTIVITY_BUDGET_BUDGETANALOGHI);
		}
	};

	
	//AsyncTask per eliminare il budget corrente
	private class EliminaBudgetTask extends AsyncTask<Long, Object, Integer> {
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDettaglio.this);
		
		@Override
		protected Integer doInBackground(Long... params) {
			dbcSpeseBudget.openModifica();
			int budgetEliminati;
			if(params[1] == 0) {
				budgetEliminati = dbcSpeseBudget.eliminaSpesaBudget(params[0]);
			}
			else {
				budgetEliminati = dbcSpeseBudget.eliminaBudgetAnaloghi(budgetIniziale);
			}
			dbcSpeseBudget.close();
			
			return budgetEliminati;
		}
		
		
		@Override
		protected void onPostExecute(Integer result) {	
			String msg = getResources().getString(R.string.toast_budget_eliminato, result);
			new MioToast(BudgetDettaglio.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			finish();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,  resultCode, data);
		
		switch(requestCode) {
		case ACTIVITY_BUDGET_SPESEINCLUSE:
			if(resultCode == Activity.RESULT_OK) {
				modifiche = true;
				
				//recupero i dettagli del budget da un thread separato
				new RecuperaDettagliBudgetTask().execute(id);
			}
			break;
		case ACTIVITY_BUDGET_BUDGETANALOGHI:
			if(resultCode == Activity.RESULT_OK) {
				modifiche = true;
				
				//recupero i dettagli del budget da un thread separato
				new RecuperaDettagliBudgetTask().execute(id);
			}
			break;
		case ACTIVITY_BUDGET_MODIFICA:
			if(resultCode == Activity.RESULT_OK) {
				Intent intRitorno = new Intent();
				intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_MODIFICA);
				setResult(Activity.RESULT_OK, intRitorno);
				finish();
				
				/*
				modifiche = true;
				
				//recupero i dettagli del budget da un thread separato
				new RecuperaDettagliBudgetTask().execute(id);
				*/
			}
			break;
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_budgetdettaglio, menu);
		
		//se provengo dall'elenco dei budget analoghi disabilito i pulsanti budget analoghi e edit
		if(chiamante == ACTIVITY_BUDGET_BUDGETANALOGHI) {
			MenuItem item = menu.findItem(R.id.menu_budgetDettaglio_cancella);
			item.setEnabled(false);
			item = menu.findItem(R.id.menu_budgetDettaglio_modifica);
			item.setEnabled(false);
		}
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_budgetDettaglio_cancella:
			eliminaBudget();

			return true;	
		case R.id.menu_budgetDettaglio_modifica:
			modificaBudget();
			
			return true;
		case android.R.id.home:
            finish();
	        
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	// eliminazione budget
	private void eliminaBudget() {
		if(ripetizione.equals("una_tantum")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BudgetDettaglio.this);
			builder.setTitle(R.string.dettagli_voce_conferma_elimina_titolo);
			builder.setMessage(R.string.dettagli_voce_conferma_elimina_msg);
			builder.setNegativeButton(R.string.cancella, null);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					conferma = true;
					new EliminaBudgetTask().execute(id, 0L);
					modifiche = true;
				}
			});
			builder.setCancelable(true);
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();
		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(BudgetDettaglio.this);
			builder.setTitle(R.string.dettagli_voce_conferma_elimina_titolo);
			builder.setMessage(R.string.budget_BudgetDettaglioVoce_eliminaRipetuti_msg);
			builder.setNegativeButton(R.string.cancella, null);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					conferma = true;
					new EliminaBudgetTask().execute(id, (long)which);
					modifiche=true;
				}
			});
			builder.setCancelable(true);
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();
		}
	}
	
	
	// modifica budget
	private void modificaBudget() {
		Intent modificaBudget = new Intent(BudgetDettaglio.this, BudgetModifica.class);
		modificaBudget.putExtra(ROW_ID, id);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_VOCE, voceBudget);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_RIPETIZIONE, ripetizione);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_IMPORTO, importoBudget);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_DATA_INIZIO, dataInizioBudgetLong);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_DATA_FINE, dataFineBudgetLong);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_AGGIUNGERE_RIMANENZA, aggiungereRimanenza);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_BUDGET_INIZIALE, budgetIniziale);
		modificaBudget.putExtra(CostantiPubbliche.BUDGET_ULTIMO_AGGIUNTO, ultimoAggiunto);
		
		startActivityForResult(modificaBudget, ACTIVITY_BUDGET_MODIFICA);
	}

	
	//variabili di istanza
	private int chiamante; // qual � l'Activity che chiama questa qua
	private long id;
	private String voceBudget;
	private String ripetizione;
	private double importoBudget;
	private long dataInizioBudgetLong;
	private long dataFineBudgetLong;
	private int aggiungereRimanenza;
	private double spesaSost;
	private long budgetIniziale;
	private int ultimoAggiunto;
	private int iconaId;
	private String[] tipiBudget;
	private boolean modifiche; // ho fatto qualche modifica al budget o alle spese?
	private Currency currValutaPrinc;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	
	//gestione suoni
	private SoundPool soundPool;
	private SparseIntArray mappaSuoni;
	private boolean suoniAbilitati;
	private boolean conferma;
}
