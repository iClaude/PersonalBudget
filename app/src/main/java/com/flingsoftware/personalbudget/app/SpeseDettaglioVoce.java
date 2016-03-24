package com.flingsoftware.personalbudget.app;
import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.*;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiSuoni;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.utilita.UtilitaVarie;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.SpeseEntrateEliminaVociRipetute.*;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_ELIMINAZIONE_SPESE_RIPETUTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.*;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;


public class SpeseDettaglioVoce extends ActionBarActivity implements SpeseEntrateEliminaVociRipetute.EliminaVociRipetuteListener {
	
	//costanti pubbliche
	public interface CostantiPubbliche {
		String VOCE_ID = "id";
		String VOCE_IMPORTO = "importo";
		String VOCE_TAG = "tag";
		String VOCE_DATA = "data";
		String VOCE_DESCRIZIONE = "descrizione";
		String VOCE_RIPETIZIONE_ID = "ripetizione_id";
		String VOCE_IMPORTO_VALPRIN = "importoValprin";
		String VOCE_VALUTA = "valuta";
		String VOCE_CONTO = "conto";
		String VOCE_FAVORITE = "favorite";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spese_entrate_dettaglio_voce);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

		// Listener per Toolbar espansa o collassata: serve per visualizzare o nascondere il fab in basso
		AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
		appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener());
		
		//ottengo i reference ai vari componenti
		tvTag = (TextView) findViewById(R.id.tvVoce);
		tvImporto = (TextView) findViewById(R.id.tvImporto);
		tvConto = (TextView) findViewById(R.id.sedv_tvConto);
		tvData = (TextView) findViewById(R.id.tvData);
		tvDescrizione = (TextView) findViewById(R.id.tvDescrizione);
		tvRipetizione = (TextView) findViewById(R.id.dettagli_voce_tvRipetizione);
		tvFineRipetizione = (TextView) findViewById(R.id.dettagli_voce_tvFineRipetizione);
		fabBasso = (FloatingActionButton) findViewById(R.id.fabBasso);

		//recupero i dettagli della voce passati dall'Activity chiamante
		Bundle extras = getIntent().getExtras();
		id = extras.getLong(CostantiPubbliche.VOCE_ID);
		importo = extras.getDouble(CostantiPubbliche.VOCE_IMPORTO);
		tag = extras.getString(CostantiPubbliche.VOCE_TAG);
		data = extras.getLong(CostantiPubbliche.VOCE_DATA);
		descrizione = extras.getString(CostantiPubbliche.VOCE_DESCRIZIONE);
		ripetizione_id = extras.getLong(CostantiPubbliche.VOCE_RIPETIZIONE_ID);
		importoValprin = extras.getDouble(CostantiPubbliche.VOCE_IMPORTO_VALPRIN);
		valuta = extras.getString(CostantiPubbliche.VOCE_VALUTA);
		conto = extras.getString(CostantiPubbliche.VOCE_CONTO);
		preferito = extras.getInt(CostantiPubbliche.VOCE_FAVORITE);

		//visualizzo i valori recuperati nel layout
		CollapsingToolbarLayout collapsingToolbar =
				(CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		collapsingToolbar.setTitle(tag);

		new CaricaIconaTask().execute(tag);
		//tvTag.setText(R.string.dettagli_voce_dettagli_spesa);

		/*if(preferito == 1) {
			findViewById(R.id.ivPreferito).setVisibility(View.VISIBLE);
		}*/
		
		//ricavo la valuta di default
		ricavaValuta();
		
		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf.setCurrency(currValuta);
		String importoFormattato = nf.format(importoValprin);
		tvImporto.setText(importoFormattato);
		//tvTag.setText(tag);
		tvConto.setText(conto);
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
		tvData.setText(df.format(new Date(data)));
		if(descrizione.length() > 0) {
			tvDescrizione.setText(descrizione);
		}
		else {
			tvDescrizione.setVisibility(View.GONE);
			findViewById(R.id.tvDescrizioneTitolo).setVisibility(View.GONE);
		}
		if(!valuta.equals(currValuta.getCurrencyCode())) {
			NumberFormat nfValuta = NumberFormat.getInstance(Locale.getDefault());
			NumberFormat nfCambio = NumberFormat.getInstance();
			nfCambio.setMaximumFractionDigits(4);
			
			float cambio = (float) (importoValprin / importo);
			((TextView) findViewById(R.id.tvImportoOriginale)).setText(nfValuta.format(importo) + " " + Currency.getInstance(valuta).getSymbol());
			((TextView) findViewById(R.id.tvTassoCambio)).setText(nfCambio.format(cambio));
		}
		else {
			findViewById(R.id.card_view_importo).setVisibility(View.GONE);
		}

		//recupero i dettagli delle spese ripetute in un thread separato
		dbcSpeseSostenute = new DBCSpeseSostenute(SpeseDettaglioVoce.this);
		dbcSpeseRipetute = new DBCSpeseRipetute(SpeseDettaglioVoce.this);
		if(ripetizione_id != 1) {
			new ImpostaDettagliSpesaRipetuta().execute(ripetizione_id);
		} else {
            findViewById(R.id.card_view_ripetizione).setVisibility(View.GONE);
		}
		
		new CaricaSuoniTask().execute();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_speseentratedettagliovoce, menu);
		
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.menu_speseEntrateDettaglioVoce_cancella:
			eliminaVoce();

			return true;	
		case R.id.menu_speseEntrateDettaglioVoce_duplica:
			duplicaVoce(); // duplicazione voce

			return true;
		case android.R.id.home:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            }
            else {
                finish();
            }

	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(suoniAbilitati && confermaElimina) {
			soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_CANCELLAZIONE), 1, 1, 1, 0, 1f);
		}
		else if(suoniAbilitati && confermaDuplica) {
			soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_AGGIUNGI_SPESA_ENTRATA), 1, 1, 1, 0, 1f);
		}
	}

	/*
	Classe per intercettare quando la Toolbar è collassata o espansa, in modo tale da visualizzare
	il fab per la modifica della voce in basso a destra.
	 */
	private class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
		private final static int ESPANSO = 0;
		private final static int COLLASSATO = 1;
		private final static int INTERMEDIO = 2;
		private int statoCorrente = INTERMEDIO;
		private boolean fabVisibile = false;

		@Override
		public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
			if (i == 0) {
				if (statoCorrente != ESPANSO) {
					// non fare nulla
				}
				statoCorrente = ESPANSO;
			} else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
				if (statoCorrente != COLLASSATO) {
					fabBasso.show();
					fabVisibile = true;
				}
				statoCorrente = COLLASSATO;
			} else {
				if (statoCorrente != INTERMEDIO) {
					if(fabVisibile) {
						fabBasso.hide();
					}
					fabVisibile = false;
				}
				statoCorrente = INTERMEDIO;
			}
		}
	}

	/*
	 * Ricavo la valuta principale salvata nelle preferenze.
	 */
	private void ricavaValuta() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String codiceValutaDefault = pref.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
		currValuta = Currency.getInstance(codiceValutaDefault);
	}


	// Duplicazione voce: aggiungo una nuova voce con data oggi con gli stessi dati.
	private void duplicaVoce() {
		UtilitaVarie.visualizzaDialogOKAnnulla(SpeseDettaglioVoce.this,
				getString(R.string.dettagli_voce_conferma_duplica_titolo),
				getString(R.string.dettagli_voce_conferma_duplica_msg),
				getString(R.string.ok),
				true, getString(R.string.cancella),
				0,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						confermaDuplica = true;
						new DuplicaVoce().execute((Object[]) null);
					}
				});
	}


	// elimina 1 o pi� voci di spesa ripetute
	private void eliminaVoce() {
		if(ripetizione_id == 1) {
			UtilitaVarie.visualizzaDialogOKAnnulla(SpeseDettaglioVoce.this,
					getString(R.string.dettagli_voce_conferma_elimina_titolo),
					getString(R.string.dettagli_voce_conferma_elimina_msg),
					getString(R.string.ok),
					true, getString(R.string.cancella),
					0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							confermaElimina = true;
							new EliminaQuestaSpesaTask().execute(id);
							//aggiorno la tabella spese_budget campo spesa_sost a seguito della eliminazione della/e spesa/e
							new AggiornaTabellaBudgetTask(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + tag + "%", Long.valueOf(data).toString(), Long.valueOf(data).toString()).execute((Object[]) null);
						}
					});
		}
		else {
			DialogFragment dialogEliminaVociRipetute = new SpeseEntrateEliminaVociRipetute();
			dialogEliminaVociRipetute.show(getSupportFragmentManager(), "EliminaVociRipetute");
		}
	}


	// Azione associata alla pressione del FAB.
	public void fabPremuto(View v) {
		modificaVoce();
	}
	
	// modifica questa voce
	private void modificaVoce() {
		Intent modificaSpesa = new Intent(SpeseDettaglioVoce.this, SpeseAggiungi.class);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_ID, id);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_TAG, tag);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_IMPORTO, importo);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_VALUTA, valuta);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_IMPORTO_VALPRIN, importoValprin);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_DATA, data);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_DESCRIZIONE, descrizione);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_RIPETIZIONE_ID, ripetizione_id);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_CONTO, conto);
		modificaSpesa.putExtra(CostantiPubbliche.VOCE_FAVORITE, preferito);
		startActivityForResult(modificaSpesa, 0);

	}
	
	
	//AsyncTask per recuperare i dettagli delle spese ripetute.
	private class ImpostaDettagliSpesaRipetuta extends AsyncTask<Long, Object, Cursor> {
		
		protected Cursor doInBackground(Long... params) {
			dbcSpeseRipetute.openLettura();
			Cursor curSpesaRipetuta = dbcSpeseRipetute.getSpesaRipetuta(params[0]);

			return curSpesaRipetuta;
		}

		protected void onPostExecute(Cursor curSpesaRipetuta) {
			curSpesaRipetuta.moveToFirst();
			String ripetizione = curSpesaRipetuta.getString(curSpesaRipetuta.getColumnIndex("ripetizione"));
			dataFine = curSpesaRipetuta.getLong(curSpesaRipetuta.getColumnIndex("data_fine"));
			dataInizio = curSpesaRipetuta.getLong(curSpesaRipetuta.getColumnIndex("data_inizio"));
			
			String tipiBudget[] = getResources().getStringArray(R.array.ripetizioni);
			if(ripetizione.equals("nessuna")) {
				ripetizione = tipiBudget[0];
			}
			else if(ripetizione.equals("giornaliero")) {
				ripetizione = tipiBudget[1];
			}
			else if(ripetizione.equals("settimanale")) {
				ripetizione = tipiBudget[2];
			}
			else if(ripetizione.equals("bisettimanale")) {
				ripetizione = tipiBudget[3];
			}
			else if(ripetizione.equals("mensile")) {
				ripetizione = tipiBudget[4];
			}
			else if(ripetizione.equals("annuale")) {
				ripetizione = tipiBudget[5];
			}
			else if(ripetizione.equals("giorni_lavorativi")) {
				ripetizione = tipiBudget[6];
			}
			else if(ripetizione.equals("weekend")) {
				ripetizione = tipiBudget[7];
			}
			
			tvRipetizione.setText(ripetizione);
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);
			tvFineRipetizione.setText(df.format(new Date(dataFine)));
			curSpesaRipetuta.close();
			dbcSpeseRipetute.close();
		}
	}


	
	//implementazione di EliminaVociRipetute.EliminaVociRipetuteListener: serve per specificare, per le voci ripetute, se eliminarne una o pi� di una
	@Override
	public void onDialogPositiveClick(int sceltaElimina) {
		confermaElimina = true;
		Long oggi = FunzioniComuni.getDataAttuale();
		
		switch(sceltaElimina) {
		case ELIMINA_SOLO_QUESTA:
			new EliminaQuestaSpesaTask().execute(id);
			new AggiornaTabellaBudgetTask(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + tag + "%", Long.valueOf(data).toString(),  Long.valueOf(data).toString()).execute((Object[]) null);
			break;
		case ELIMINA_TUTTE:
			new EliminaTutteSpeseRipetuteTask().execute(ripetizione_id);
			new AggiornaTabellaBudgetTask(ESTRAI_BUDGET_PER_ELIMINAZIONE_SPESE_RIPETUTE, "%" + tag + "%", Long.valueOf(dataInizio).toString(),  Long.valueOf(dataInizio).toString(), Long.valueOf(dataInizio).toString(), oggi.toString(), oggi.toString(), oggi.toString(), Long.valueOf(dataInizio).toString(), oggi.toString()).execute((Object[]) null);
			break;
		case ELIMINA_DA_OGGI:
			new EliminaSpeseRipetuteDaOggiTask().execute(ripetizione_id);
			new AggiornaTabellaBudgetTask(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + tag + "%", oggi.toString(),  oggi.toString()).execute((Object[]) null);
			break;
		}
	}
	
	
	//AsyncTask per caricare la HashMap con i suoni dell'app
	private class CaricaSuoniTask extends AsyncTask<Object, Object, Boolean> {
			
		protected Boolean doInBackground(Object... params) {		
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SpeseDettaglioVoce.this);
			boolean abilitazioneSuoni = pref.getBoolean(CostantiPreferenze.SUONI_ABILITATI, false);
			if(abilitazioneSuoni) {
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				mappaSuoni = new SparseIntArray(1);
				mappaSuoni.put(CostantiSuoni.SUONO_CANCELLAZIONE, soundPool.load(SpeseDettaglioVoce.this, R.raw.cancellazione, 1));
				mappaSuoni.put(CostantiSuoni.SUONO_AGGIUNGI_SPESA_ENTRATA, soundPool.load(SpeseDettaglioVoce.this, R.raw.spese_entrate_budget_aggiunta, 1));
			}
			
			return abilitazioneSuoni;
		}
			
		protected void onPostExecute(Boolean result) {
			//una volta caricati i suoni nella Map l'app � pronta ad utilizzarli, non prima
			suoniAbilitati = result;
		}
	}
	

	//AsyncTask per caricare icona spesa
	private class CaricaIconaTask extends AsyncTask<String, Object, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(SpeseDettaglioVoce.this);
			dbcSpeseVoci.openLettura();
			Cursor curVoci = dbcSpeseVoci.getTutteLeVociFiltrato(params[0]);
			
			int iconaId = R.drawable.tag_0;
			if(curVoci.moveToFirst()) {
				int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
				iconaId = ListViewIconeVeloce.arrIconeId[icona];
			}
			
			curVoci.close();
			dbcSpeseVoci.close();

            return ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaId, 70, 70);
		}
		
		@Override
		protected void onPostExecute(Bitmap miaBitmap) {
			final ImageView ivIcona = (ImageView) findViewById(R.id.spese_entrate_dettaglio_voce_ivIcona);
			ivIcona.setImageBitmap(miaBitmap);
		}
	}


	/*
		Duplicazione voce: AsyncTask x duplicare la voce in un thread separato. NB: duplicazione =
		aggiungere nuova voce con data oggi e con gli stessi dati.
	 */
	private class DuplicaVoce extends AsyncTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			dbcSpeseSostenute.openModifica();
			dbcSpeseSostenute.inserisciSpesaSostenuta(FunzioniComuni.getDataAttuale(), tag, importo, valuta, importoValprin, descrizione, ripetizione_id, conto, 0);
			dbcSpeseSostenute.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			String msg = getResources().getString(R.string.dettagli_voce_spesa_duplicata);
			new MioToast(SpeseDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);

			setResult(Activity.RESULT_OK);
			finish();
		}
	}


	//AsyncTask per eliminare solo la spesa selezionata
	private class EliminaQuestaSpesaTask extends AsyncTask<Long, Object, Object> {
		@Override
		protected Object doInBackground(Long... params) {
			dbcSpeseSostenute.openModifica();
			dbcSpeseSostenute.eliminaSpesaSostenuta(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			dbcSpeseSostenute.close();
			
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, 1, 1);
			new MioToast(SpeseDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			finish();
		}
	}

	/*AsyncTask x eliminare tutte le spese ripetute. Elimina sia dalla tabella spese_sost che dalla 
	 * tabella spese_ripet (se si eliminano tutte le ripetizioni il record in spese_ripet non serve pi�).
	 * Nel campo Long passo la ripetizione_id
	 */
	private class EliminaTutteSpeseRipetuteTask extends AsyncTask<Long, Object, Integer> {
		@Override
		protected Integer doInBackground(Long... params) {
			dbcSpeseSostenute.openModifica();
			int speseCancellate = dbcSpeseSostenute.eliminaSpeseRipetute(params[0]);
			
			dbcSpeseRipetute.openModifica();
			dbcSpeseRipetute.eliminaSpesaRipetuta(params[0]);
			
			return speseCancellate;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			dbcSpeseSostenute.close();
			dbcSpeseRipetute.close();
			
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, result, result);
			new MioToast(SpeseDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			finish();
		}
	}
	
	/*AsyncTask x eliminare tutte le spese ripetute da oggi in poi. Elimina solo dalla tabella 
	 * spese_sost.
	 * Nel campo Long passo la ripetizione_id
	 */
	private class EliminaSpeseRipetuteDaOggiTask extends AsyncTask<Long, Object, Integer> {
		@Override
		protected Integer doInBackground(Long... params) {
			GregorianCalendar oggi = new GregorianCalendar();
			int giorno = oggi.get(GregorianCalendar.DATE);
			int mese = oggi.get(GregorianCalendar.MONTH);
			int anno = oggi.get(GregorianCalendar.YEAR);
			oggi = new GregorianCalendar(anno, mese, giorno);
			
			dbcSpeseSostenute.openModifica();
			int speseCancellate = dbcSpeseSostenute.eliminaSpeseRipetuteDallaData(params[0], oggi.getTimeInMillis());
			
			dbcSpeseRipetute.openModifica();
			Cursor curSpeseRipetute = dbcSpeseRipetute.getSpesaRipetuta(params[0]);
			curSpeseRipetute.moveToFirst();
			dbcSpeseRipetute.aggiornaSpesaRipetuta(params[0], curSpeseRipetute.getString(curSpeseRipetute.getColumnIndex("voce")), curSpeseRipetute.getString(curSpeseRipetute.getColumnIndex("ripetizione")), curSpeseRipetute.getDouble(curSpeseRipetute.getColumnIndex("importo")), curSpeseRipetute.getString(curSpeseRipetute.getColumnIndex("valuta")), curSpeseRipetute.getDouble(curSpeseRipetute.getColumnIndex("importo_valprin")), curSpeseRipetute.getString(curSpeseRipetute.getColumnIndex("descrizione")), curSpeseRipetute.getLong(curSpeseRipetute.getColumnIndex("data_inizio")), 1, oggi.getTimeInMillis(), oggi.getTimeInMillis(), curSpeseRipetute.getString(curSpeseRipetute.getColumnIndex("conto")));		
			curSpeseRipetute.close();		
			
			return speseCancellate;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			dbcSpeseSostenute.close();
			dbcSpeseRipetute.close();
			
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, result, result);
			new MioToast(SpeseDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			finish();
		}
	}
	
	//ritorno dall'Activity SpeseAggiungi per modificare la spesa
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_MODIFICA);
			setResult(Activity.RESULT_OK, intRitorno);
			finish();
		}
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
			if(!result) {
				new MioToast(SpeseDettaglioVoce.this, getString(R.string.toast_aggiornamentoDatabase_errore)).visualizza(Toast.LENGTH_SHORT);
			}
			
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
		}
	}


	//variabili di istanza
	private TextView tvTag;
	private TextView tvImporto;
	private TextView tvConto;
	private TextView tvData;
	private TextView tvDescrizione;
	private TextView tvRipetizione;
	private TextView tvFineRipetizione;
	private FloatingActionButton fabBasso;
	private long id;
	private long ripetizione_id;
	private double importo;
	private String conto;
	private int preferito;
	private String tag;
	private long data;
	private String descrizione;
	private long dataFine;
	private long dataInizio;
	private String valuta;
	private double importoValprin;
	private DBCSpeseSostenute dbcSpeseSostenute;
	private DBCSpeseRipetute dbcSpeseRipetute;
	private Currency currValuta;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	
	//gestione suoni
	private SoundPool soundPool;
	private SparseIntArray mappaSuoni;
	private boolean suoniAbilitati;
	private boolean confermaElimina;
	private boolean confermaDuplica;
}
