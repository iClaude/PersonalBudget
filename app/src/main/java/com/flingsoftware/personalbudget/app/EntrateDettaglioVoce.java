package com.flingsoftware.personalbudget.app;
import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiSuoni;
import com.flingsoftware.personalbudget.app.SpeseDettaglioVoce.CostantiPubbliche;
import com.flingsoftware.personalbudget.database.*;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.utilita.UtilitaVarie;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;
import static com.flingsoftware.personalbudget.app.SpeseEntrateEliminaVociRipetute.*;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.TIPO_OPERAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_ELIMINAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_MODIFICA;
 



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
import android.graphics.Bitmap;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;


public class EntrateDettaglioVoce extends AppCompatActivity implements SpeseEntrateEliminaVociRipetute.EliminaVociRipetuteListener {
	
	//costanti 
	public static final String VOCE_ID = "id";
	public static final String VOCE_IMPORTO = "importo";
	public static final String VOCE_TAG = "tag";
	public static final String VOCE_DATA = "data";
	public static final String VOCE_DESCRIZIONE = "descrizione";
	public static final String VOCE_RIPETIZIONE_ID = "ripetizione_id";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spese_entrate_dettaglio_voce);
		Log.i(getClass().getSimpleName(), "test log");

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
		id = extras.getLong(VOCE_ID);
		importo = extras.getDouble(VOCE_IMPORTO);
		tag = extras.getString(VOCE_TAG);
		data = extras.getLong(VOCE_DATA);
		descrizione = extras.getString(VOCE_DESCRIZIONE);
		ripetizione_id = extras.getLong(VOCE_RIPETIZIONE_ID);
		importoValprin = extras.getDouble(CostantiPubbliche.VOCE_IMPORTO_VALPRIN);
		valuta = extras.getString(CostantiPubbliche.VOCE_VALUTA);
		conto = extras.getString(CostantiPubbliche.VOCE_CONTO);
		preferito = extras.getInt(CostantiPubbliche.VOCE_FAVORITE);

		//visualizzo i valori recuperati nel layout
		CollapsingToolbarLayout collapsingToolbar =
				(CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
		collapsingToolbar.setTitle(tag);
		
		//visualizzo i valori recuperati nel layout
		new CaricaIconaTask().execute(tag);

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

		//recupero i dettagli delle entrate ripetute in un thread separato
		dbcEntrateIncassate = new DBCEntrateIncassate(EntrateDettaglioVoce.this);
		dbcEntrateRipetute = new DBCEntrateRipetute(EntrateDettaglioVoce.this);
		if(ripetizione_id != 1) {
			new ImpostaDettagliEntrataRipetuta().execute(ripetizione_id);
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
		UtilitaVarie.visualizzaDialogOKAnnulla(EntrateDettaglioVoce.this,
				getString(R.string.dettagli_voce_conferma_duplica_titolo),
				getString(R.string.dettagli_voce_conferma_duplica_msg),
				getString(R.string.ok), true, getString(R.string.cancella), 0,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						confermaDuplica = true;
						new DuplicaVoce().execute((Object[]) null);
					}
				});
	}

	
	//elimina 1 o pi� entrate ripetute
	private void eliminaVoce() {
		if(ripetizione_id == 1) {
			UtilitaVarie.visualizzaDialogOKAnnulla(EntrateDettaglioVoce.this,
					getString(R.string.dettagli_voce_conferma_elimina_titolo),
					getString(R.string.dettagli_voce_conferma_elimina_msg),
					getString(R.string.ok),
					true, getString(R.string.cancella),
					0,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							confermaElimina = true;
							new EliminaQuestaEntrataTask().execute(id);
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

	//modifica questa voce
	private void modificaVoce() {
		Intent modificaEntrata = new Intent(EntrateDettaglioVoce.this, EntrateAggiungi.class);
		modificaEntrata.putExtra(VOCE_ID, id);
		modificaEntrata.putExtra(VOCE_TAG, tag);
		modificaEntrata.putExtra(VOCE_IMPORTO, importo);
		modificaEntrata.putExtra(CostantiPubbliche.VOCE_VALUTA, valuta);
		modificaEntrata.putExtra(CostantiPubbliche.VOCE_IMPORTO_VALPRIN, importoValprin);
		modificaEntrata.putExtra(VOCE_DATA, data);
		modificaEntrata.putExtra(VOCE_DESCRIZIONE, descrizione);
		modificaEntrata.putExtra(VOCE_RIPETIZIONE_ID, ripetizione_id);
		modificaEntrata.putExtra(CostantiPubbliche.VOCE_CONTO, conto);
		modificaEntrata.putExtra(CostantiPubbliche.VOCE_FAVORITE, preferito);
		startActivityForResult(modificaEntrata, 0);
	}
	
	
	//AsyncTask per recuperare i dettagli delle entrate ripetute.
	private class ImpostaDettagliEntrataRipetuta extends AsyncTask<Long, Object, Cursor> {
		
		protected Cursor doInBackground(Long... params) {
			dbcEntrateRipetute.openLettura();
			Cursor curEntrataRipetuta = dbcEntrateRipetute.getEntrataRipetuta(params[0]);
			
			return curEntrataRipetuta;
		}

		protected void onPostExecute(Cursor curEntrataRipetuta) {
			curEntrataRipetuta.moveToFirst();
			String ripetizione = curEntrataRipetuta.getString(curEntrataRipetuta.getColumnIndex("ripetizione"));
			long dataFine = curEntrataRipetuta.getLong(curEntrataRipetuta.getColumnIndex("data_fine"));

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
			curEntrataRipetuta.close();
			dbcEntrateRipetute.close();
		}
	}
	
	//implementazione di EliminaVociRipetute.EliminaVociRipetuteListener: serve per specificare, per le voci ripetute, se eliminarne una o pi� di una
	@Override
	public void onDialogPositiveClick(int sceltaElimina) {
		confermaElimina = true;
		
		switch(sceltaElimina) {
		case ELIMINA_SOLO_QUESTA:
			new EliminaQuestaEntrataTask().execute(id);
			break;
		case ELIMINA_TUTTE:
			new EliminaTutteEntrateRipetuteTask().execute(ripetizione_id);
			break;
		case ELIMINA_DA_OGGI:
			new EliminaEntrateRipetuteDaOggiTask().execute(ripetizione_id);
			break;
		}
	}
	
	
	//AsyncTask per caricare la HashMap con i suoni dell'app
	private class CaricaSuoniTask extends AsyncTask<Object, Object, Boolean> {
			
		protected Boolean doInBackground(Object... params) {		
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(EntrateDettaglioVoce.this);
			boolean abilitazioneSuoni = pref.getBoolean(CostantiPreferenze.SUONI_ABILITATI, false);
			if(abilitazioneSuoni) {
				soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
				mappaSuoni = new SparseIntArray(1);
				mappaSuoni.put(CostantiSuoni.SUONO_CANCELLAZIONE, soundPool.load(EntrateDettaglioVoce.this, R.raw.cancellazione, 1));
				mappaSuoni.put(CostantiSuoni.SUONO_AGGIUNGI_SPESA_ENTRATA, soundPool.load(EntrateDettaglioVoce.this, R.raw.spese_entrate_budget_aggiunta, 1));
			}
			
			return abilitazioneSuoni;
		}
			
		protected void onPostExecute(Boolean result) {
			//una volta caricati i suoni nella Map l'app � pronta ad utilizzarli, non prima
			suoniAbilitati = result;
		}
	}
	
	
	//AsyncTask per caricare icona entrata
	private class CaricaIconaTask extends AsyncTask<String, Object, Bitmap> {
		@Override
		protected Bitmap doInBackground(String... params) {
			DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(EntrateDettaglioVoce.this);
			dbcEntrateVoci.openLettura();
			Cursor curVoci = dbcEntrateVoci.getTutteLeVociFiltrato(params[0]);
			
			int iconaId = R.drawable.tag_1;
			if(curVoci.moveToFirst()) {
				int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
				iconaId = ListViewIconeVeloce.arrIconeId[icona];
			}
			
			curVoci.close();
			dbcEntrateVoci.close();
			
			return ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaId, 70, 70);
		}
		
		@Override
		protected void onPostExecute(Bitmap miaBitmap) {
			((ImageView) findViewById(R.id.spese_entrate_dettaglio_voce_ivIcona)).setImageBitmap(miaBitmap);
		}
	}


	/*
    Duplicazione voce: AsyncTask x duplicare la voce in un thread separato. NB: duplicazione =
    aggiungere nuova voce con data oggi e con gli stessi dati.
 */
	private class DuplicaVoce extends AsyncTask<Object, Void, Void> {
		@Override
		protected Void doInBackground(Object... params) {
			dbcEntrateIncassate.openModifica();
			dbcEntrateIncassate.inserisciEntrataIncassata(FunzioniComuni.getDataAttuale(), tag, importo, valuta, importoValprin, descrizione, ripetizione_id, conto, 0);
			dbcEntrateIncassate.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			String msg = getResources().getString(R.string.dettagli_voce_entrata_duplicata);
			new MioToast(EntrateDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);

			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	
	//AsyncTask per eliminare solo la entrata selezionata
	private class EliminaQuestaEntrataTask extends AsyncTask<Long, Object, Object> {
		@Override
		protected Object doInBackground(Long... params) {
			dbcEntrateIncassate.openModifica();
			dbcEntrateIncassate.eliminaEntrataIncassata(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			dbcEntrateIncassate.close();
			
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, 1);
			new MioToast(EntrateDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			
			final Intent intAggiornaWidget = new Intent (WIDGET_PICCOLO_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
			
			finish();
		}
	}

	/*AsyncTask x eliminare tutte le entrate ripetute. Elimina sia dalla tabella entrate_inc che dalla 
	 * tabella entrate_ripet (se si eliminano tutte le ripetizioni il record in entrate_ripet non serve pi�).
	 * Nel campo Long passo la ripetizione_id
	 */
	private class EliminaTutteEntrateRipetuteTask extends AsyncTask<Long, Object, Integer> {
		@Override
		protected Integer doInBackground(Long... params) {
			dbcEntrateIncassate.openModifica();
			int entrateCancellate = dbcEntrateIncassate.eliminaEntrateRipetute(params[0]);
			
			dbcEntrateRipetute.openModifica();
			dbcEntrateRipetute.eliminaEntrataRipetuta(params[0]);
			
			return entrateCancellate;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			dbcEntrateIncassate.close();
			dbcEntrateRipetute.close();
			
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, result, result);
			new MioToast(EntrateDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			
			final Intent intAggiornaWidget = new Intent (WIDGET_PICCOLO_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
			
			finish();
		}
	}
	
	/*AsyncTask x eliminare tutte le entrate ripetute da oggi in poi. Elimina solo dalla tabella 
	 * entrate_inc.
	 * Nel campo Long passo la ripetizione_id
	 */
	private class EliminaEntrateRipetuteDaOggiTask extends AsyncTask<Long, Object, Integer> {
		@Override
		protected Integer doInBackground(Long... params) {
			GregorianCalendar oggi = new GregorianCalendar();
			int giorno = oggi.get(GregorianCalendar.DATE);
			int mese = oggi.get(GregorianCalendar.MONTH);
			int anno = oggi.get(GregorianCalendar.YEAR);
			oggi = new GregorianCalendar(anno, mese, giorno);
			
			dbcEntrateIncassate.openModifica();
			int entrateCancellate = dbcEntrateIncassate.eliminaEntrateRipetuteDallaData(params[0], oggi.getTimeInMillis());
			
			dbcEntrateRipetute.openModifica();
			Cursor curEntrateRipetute = dbcEntrateRipetute.getEntrataRipetuta(params[0]);
			curEntrateRipetute.moveToFirst();
			dbcEntrateRipetute.aggiornaEntrataRipetuta(params[0], curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("voce")), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("ripetizione")), curEntrateRipetute.getDouble(curEntrateRipetute.getColumnIndex("importo")), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("valuta")), curEntrateRipetute.getDouble(curEntrateRipetute.getColumnIndex("importo_valprin")), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("descrizione")), curEntrateRipetute.getLong(curEntrateRipetute.getColumnIndex("data_inizio")), 1, oggi.getTimeInMillis(), oggi.getTimeInMillis(), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("conto")));		
			curEntrateRipetute.close();		
			
			return entrateCancellate;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			dbcEntrateIncassate.close();
			dbcEntrateRipetute.close();
			
			String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, result, result);
			new MioToast(EntrateDettaglioVoce.this, msg).visualizza(Toast.LENGTH_SHORT);
			
			Intent intRitorno = new Intent();
			intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
			setResult(Activity.RESULT_OK, intRitorno);
			
			final Intent intAggiornaWidget = new Intent (WIDGET_PICCOLO_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
			
			finish();
		}
	}
	
	//ritorno dall'Activity EntrateAggiungi per modificare la entrata
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
	private String valuta;
	private double importoValprin;
	private DBCEntrateIncassate dbcEntrateIncassate;
	private DBCEntrateRipetute dbcEntrateRipetute;
	private Currency currValuta;
	Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);

	//gestione suoni
	private SoundPool soundPool;
	private SparseIntArray mappaSuoni;
	private boolean suoniAbilitati;
	private boolean confermaElimina;
	private boolean confermaDuplica;
}
