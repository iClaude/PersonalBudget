/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.ricvoc;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.FunzioniComuni;
import com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_CORRENTE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA;


public class RiconoscimentoVocale extends ActionBarActivity {
	
	//variabili di istanza
	private Voce[] arrVociSpesa;
	private Voce[] arrVociEntrata;
	private SparseIntArray mappaSuoni;
	private TextToSpeech mTts;
	private Locale miaLocale;
	private DatiVoce datiVoce;
	private HashMap<String, String> ttsParams;
	String arrDomande[];
	private int domandaCorrente;
	private int tentativi;
	private final static Integer[] arrIconeId = new Integer[] {R.drawable.tag_0, R.drawable.tag_1, R.drawable.tag_2, R.drawable.tag_3, R.drawable.tag_4, R.drawable.tag_5, R.drawable.tag_6, R.drawable.tag_7, R.drawable.tag_8, R.drawable.tag_9,
		R.drawable.tag_10, R.drawable.tag_11, R.drawable.tag_12, R.drawable.tag_13, R.drawable.tag_14, R.drawable.tag_15, R.drawable.tag_16, R.drawable.tag_17, R.drawable.tag_18, R.drawable.tag_19,
		R.drawable.tag_20, R.drawable.tag_21, R.drawable.tag_22, R.drawable.tag_23, R.drawable.tag_24, R.drawable.tag_25, R.drawable.tag_26, R.drawable.tag_27, R.drawable.tag_28, R.drawable.tag_29,
		R.drawable.tag_30, R.drawable.tag_31, R.drawable.tag_32, R.drawable.tag_33, R.drawable.tag_34, R.drawable.tag_35, R.drawable.tag_36, R.drawable.tag_37, R.drawable.tag_38, R.drawable.tag_39,
		R.drawable.tag_40, R.drawable.tag_41, R.drawable.tag_42, R.drawable.tag_43, R.drawable.tag_44, R.drawable.tag_45, R.drawable.tag_46, R.drawable.tag_47, R.drawable.tag_48, R.drawable.tag_49,
		R.drawable.tag_50, R.drawable.tag_51, R.drawable.tag_52, R.drawable.tag_53, R.drawable.tag_54, R.drawable.tag_55, R.drawable.tag_56, R.drawable.tag_57};
	String contoDefault;
    private SoundEffectsManager soundEffectsManager = SoundEffectsManager.getInstance();

    //reference ai componenti
	private TextView tvCosa;
	private TextView tvVoce;
	private TextView tvImporto;
	private TextView tvIndicazioni;
	private ImageView ivIconaCosa;
	private ImageView ivIconaVoce;
	
	//costanti
	private final int DOMANDA_1 = 1;
	private final int DOMANDA_2 = 2;
	private final int DOMANDA_3 = 3;
	private final int DOMANDA_4 = 4;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.riconoscimento_vocale);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		tvCosa = (TextView) findViewById(R.id.rv_tvCosa);
		tvVoce = (TextView) findViewById(R.id.rv_tvVoce);
		tvImporto = (TextView) findViewById(R.id.rv_tvImporto);
		tvIndicazioni = (TextView) findViewById(R.id.rv_tvIndicazioni);
		ivIconaCosa = (ImageView) findViewById(R.id.rv_ivIconaCosa); 
		ivIconaVoce = (ImageView) findViewById(R.id.rv_ivIconaVoce); 
		
		//testo scorrevole sulla voce
		tvVoce.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		tvVoce.setSingleLine(true);
		tvVoce.setMarqueeRepeatLimit(5);
		tvVoce.setSelected(true);
		
		datiVoce = new DatiVoce();
		arrDomande = getResources().getStringArray(R.array.ricvoc_domande);
		
		//conto
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		contoDefault = sharedPreferences.getString(CostantiPreferenze.CONTO_UTILIZZATO, "default");
		if(contoDefault.equals("%")) {
			contoDefault = "default";
		}
		
		new RecuperaVociTask().execute((Object[]) null);
		verificaTtsELingua();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_riconoscimento_vocale, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case android.R.id.home:
			finish();
	        
	        return true;
		case R.id.menu_riconoscimento_vocale_cancel:
			finish();
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	//thread separato: recupero voci di spesa/entrata e li metto in un due array
	private class RecuperaVociTask extends AsyncTask<Object, Object, Object> {
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(RiconoscimentoVocale.this);
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(RiconoscimentoVocale.this);
		
		protected Object doInBackground(Object... params) {
			//voci spesa
			dbcSpeseVoci.openLettura();
			Cursor curVociSpesa = dbcSpeseVoci.getTutteLeVoci();
			
			if(curVociSpesa.getCount() == 0) {
				return null;
			}
			
			arrVociSpesa = new Voce[curVociSpesa.getCount()];
			for(int i=0; curVociSpesa.moveToNext(); i++) {
				arrVociSpesa[i] = new Voce(curVociSpesa.getString(curVociSpesa.getColumnIndex("voce")), curVociSpesa.getInt(curVociSpesa.getColumnIndex("icona")));
			}
			
			curVociSpesa.close();
			dbcSpeseVoci.close();
			
			//voci entrata
			dbcEntrateVoci.openLettura();
			Cursor curVociEntrata = dbcEntrateVoci.getTutteLeVoci();
			
			if(curVociEntrata.getCount() == 0) {
				return null;
			}
			
			arrVociEntrata = new Voce[curVociEntrata.getCount()];
			for(int i=0; curVociEntrata.moveToNext(); i++) {
				arrVociEntrata[i] = new Voce(curVociEntrata.getString(curVociEntrata.getColumnIndex("voce")), curVociEntrata.getInt(curVociEntrata.getColumnIndex("icona")));
			}
			
			curVociEntrata.close();
			dbcEntrateVoci.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {

		}
	}
	

	//verifico presenza TTS e lingue disponibili
	private void verificaTtsELingua() {
		Intent intent = new Intent();
		intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(intent, 0);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == 0) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            //successo, creo l'oggetto TTS
	            mTts = new TextToSpeech(this, new OnInitListener()
	            {
	            	@Override
	            	public void onInit(int status) {
	    	            //verifico presenza lingua: o italiano o inglese
	    	            miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
	            		int lingDisp = mTts.isLanguageAvailable(miaLocale);
	    	            if(lingDisp == TextToSpeech.LANG_MISSING_DATA || lingDisp == TextToSpeech.LANG_NOT_SUPPORTED) {
	    	            	tvIndicazioni.setVisibility(View.VISIBLE);
	    	            	tvIndicazioni.setText(R.string.ricvoc_lingua_non_disponibile);
                            soundEffectsManager.playSound(SoundEffectsManager.SOUND_ERROR);
                        }
	    	            else {
	    	            	mTts.setLanguage(miaLocale);
	    	            	mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

								@Override
								public void onDone(String arg0) {
									if(arg0.equals("uscita")) {
	    	            				setResult(Activity.RESULT_OK);
	    	            				finish();
	    	            			}
	    	            			else {
	    	            				valutaRisposta(arg0);
	    	            			}				
								}

								@Override
								public void onError(String utteranceId) {
								}

								@Override
								public void onStart(String utteranceId) {
								}
	    	            	});
	    	            	
	    	            	/*
	    	            	mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
	    	            	{
	    	            		@Override
	    	            		public void onUtteranceCompleted(String str) {
	    	            			if(str.equals("uscita")) {
	    	            				setResult(Activity.RESULT_OK);
	    	            				finish();
	    	            			}
	    	            			else {
	    	            				valutaRisposta(str);
	    	            			}
	    	            		}
	    	            	});*/
	    	            	
	    	            	primaDomanda();
	    	            }
	            	}
	            }
	            );            
	        } else {
	            //manca il TTS: installazione
	            Intent installaIntent = new Intent();
	            installaIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installaIntent);
	        }
	    }
	    else if(requestCode == DOMANDA_1 && resultCode == RESULT_OK) {
	    	ArrayList<String> alRisposte = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	    	String arrSpesa[] = getResources().getStringArray(R.array.ricvoc_arrSpesa);
	    	String arrEntrata[] = getResources().getStringArray(R.array.ricvoc_arrEntrata);
	    	String risultato = null;
	    	
	    	uscita:
	    	for(String risposta : alRisposte) {
	    		for(String rispostaPossibile : arrSpesa) {
	    			if(risposta.equalsIgnoreCase(rispostaPossibile)) {
	    				risultato = arrSpesa[0];
	    				break uscita;
	    			}
	    		}
	    		
	    		for(String rispostaPossibile : arrEntrata) {
	    			if(risposta.equalsIgnoreCase(rispostaPossibile)) {
	    				risultato = arrEntrata[0];
	    				break uscita;
	    			}
	    		}

	    	}
	    	
	    	if(risultato != null) {
	    		if(risultato.equalsIgnoreCase(arrSpesa[0])) {
	    			datiVoce.setIsEntrata(false);
	    		}
	    		else {
	    			datiVoce.setIsEntrata(true);
	    			ivIconaCosa.setImageResource(R.drawable.tag_1);
	    			ivIconaVoce.setImageResource(R.drawable.tag_1);
	    		}
	    		findViewById(R.id.rv_ivSpunta0).setVisibility(View.VISIBLE);
	    		findViewById(R.id.card_view_voce).setVisibility(View.VISIBLE);
	    		tvCosa.setTextColor(getResources().getColor(R.color.colore_blu_scuro));
	    		tvCosa.setText(risultato);
	    		
	    		domandaCorrente = 1;
	    		tentativi = 1;
	    		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "seconda");
	    		mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    	}
	    	else {
	    		tentativi++;
	    		if(tentativi <= 3) {
	    			mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    		}
	    		else {
	    			nonHoCapito();
	    			return;
	    		}
	    	}
	    }
	    else if(requestCode == DOMANDA_2 && resultCode == RESULT_OK) {
	    	ArrayList<String> alRisposte = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	    	Voce arrPossibiliRisposte[];
	    	if(datiVoce.getIsEntrata()) {
	    		arrPossibiliRisposte = arrVociEntrata;
	    	}
	    	else {
	    		arrPossibiliRisposte = arrVociSpesa;
	    	}
	    	
	    	String risultato = null;
	    	int icona = 0;
	    	uscita:
	    	for(String risposta : alRisposte) {
	    		for(int i=0; i<arrPossibiliRisposte.length; i++) {
	    			if(risposta.equalsIgnoreCase(arrPossibiliRisposte[i].getVoce())) {
	    				risultato = risposta;
	    				icona = arrPossibiliRisposte[i].getIcona();
	    				break uscita;
	    			}
	    		}
	    	}
	    	
	    	if(risultato != null) {
	    		datiVoce.setVoce(risultato);
	    		
	    		findViewById(R.id.rv_ivSpunta2).setVisibility(View.VISIBLE);
	    		findViewById(R.id.card_view_importo).setVisibility(View.VISIBLE);
	    		tvVoce.setTextColor(getResources().getColor(R.color.colore_blu_scuro));
	    		tvVoce.setText(risultato);
	    		ivIconaVoce.setImageResource(arrIconeId[icona]);
	    		
	    		domandaCorrente = 2;
	    		tentativi = 1;
	    		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "terza");
	    		mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    	}
	    	else if(risultato == null && alRisposte.get(0).length()>0) {
	    		risultato = alRisposte.get(0);	
	    		datiVoce.setVoce(risultato);
	    		
	    		findViewById(R.id.rv_ivSpunta2).setVisibility(View.VISIBLE);
	    		findViewById(R.id.card_view_importo).setVisibility(View.VISIBLE);
	    		tvVoce.setTextColor(getResources().getColor(R.color.colore_blu_scuro));
	    		tvVoce.setText(risultato);
	    		
	    		domandaCorrente = 2;
	    		tentativi = 1;
	    		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "terza");
	    		mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    	}
	    	else {
	    		tentativi++;
	    		if(tentativi <= 3) {
	    			mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    		}
	    		else {
	    			nonHoCapito();
	    			return;
	    		}
	    	}    	
	    }
	    else if(requestCode == DOMANDA_3 && resultCode == RESULT_OK) {
	    	ArrayList<String> alRisposte = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	    	
	    	double importo = 0.0;
	    	uscita:
	    	for(int i=0; i<alRisposte.size(); i++) {
	    		try {
	    			String rispostaNorm = alRisposte.get(i);
	    			rispostaNorm = rispostaNorm.replaceAll(" ", "");
	    			rispostaNorm = rispostaNorm.replace(',', '.');
	    			importo = Double.parseDouble(rispostaNorm);
	    		}
	    		catch(NumberFormatException exc) {
	    			importo = 0.0;
	    		}
	    		
	    		if(importo != 0) break uscita;
	    	}
	    	
	    	if(importo != 0) {
	    		datiVoce.setImporto(importo);
	    		
	    		findViewById(R.id.rv_ivSpunta3).setVisibility(View.VISIBLE);
	    		tvImporto.setTextColor(getResources().getColor(R.color.colore_blu_scuro));
	    		tvImporto.setText(String.valueOf(importo));
	    		
	    		findViewById(R.id.card_view_conferma).setVisibility(View.VISIBLE);
	    		domandaCorrente = 3;
	    		tentativi = 1;
	    		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "quarta");
	    		mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    	}
	    	else {
	    		tentativi++;
	    		if(tentativi <= 3) {
	    			mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    		}
	    		else {
	    			nonHoCapito();
	    			return;
	    		}
	    	} 
	    }
	    else if(requestCode == DOMANDA_4 && resultCode == RESULT_OK) {
	    	ArrayList<String> alRisposte = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	    	
	    	String ok = getResources().getString(R.string.ricvoc_ok);
	    	String no = getResources().getString(R.string.ricvoc_annulla);
	    	
	    	String risultato = null;
	    	uscita:
	    	for(String risposta : alRisposte) {
	    		if(risposta.equalsIgnoreCase(ok) || risposta.equalsIgnoreCase(no)) {
	    			risultato = risposta;
	    			break uscita;
	    		}
	    	}
	    	
	    	if(risultato != null) {
	    		if(risultato.equalsIgnoreCase(no)) {
	    			finish();
	    		}
	    		else {
	    			if(datiVoce.getIsEntrata()) {
	    				new AggiungiEntrataIncassataTask().execute((Object[]) null);
	    			}
	    			else {
	    				new AggiungiSpesaSostenutaTask().execute((Object[]) null);
	    			}
	    		}
	    	}
	    	else {
	    		tentativi++;
	    		if(tentativi <= 3) {
	    			mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	    		}
	    		else {
	    			nonHoCapito();
	    			return;
	    		}
	    	} 
	    }
	}
	
	//inizio l'inserimento dei valori tramite il riconoscimento vocale
	private void primaDomanda() {	
		domandaCorrente = 0;
		tentativi = 1;
		ttsParams = new HashMap<String,String>();
		ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "prima");
		mTts.speak(arrDomande[domandaCorrente], TextToSpeech.QUEUE_FLUSH, ttsParams);
	}
	
	//chiamato da onUtteranceCompleted quando la domanda � stata pronunciata: bisogna valutarla
	private void valutaRisposta(String str) {
		String domanda = ttsParams.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
		
		if(domanda.equals("prima")) {
			ascolta(DOMANDA_1);
		}
		else if(domanda.equals("seconda")) {
			ascolta(DOMANDA_2);
		}
		else if(domanda.equals("terza")) {
			ascolta(DOMANDA_3);
		}
		else if(domanda.equals("quarta")) {
			ascolta(DOMANDA_4);
		}
	}
	
	//lancio lo speech recognition per valutare le risposte
	private void ascolta(int domanda) {
		Intent speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, miaLocale.getLanguage());
		try {
			startActivityForResult(speechRecognitionIntent, domanda);
		}
		catch(ActivityNotFoundException exc) {
			tvIndicazioni.setVisibility(View.VISIBLE);
        	tvIndicazioni.setText(R.string.ricvoc_sr_non_presente);
            soundEffectsManager.playSound(SoundEffectsManager.SOUND_ERROR);
        }
	}

	//messaggio di errore quando non capisce la risposta dopo il terzo tentativo
	private void nonHoCapito() {
		String msg = getResources().getString(R.string.ricvoc_non_ho_capito);
		
		tvIndicazioni.setVisibility(View.VISIBLE);
    	tvIndicazioni.setText(msg);

        soundEffectsManager.playSound(SoundEffectsManager.SOUND_ERROR);
        ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "errore");
		mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, ttsParams);
	}
	
	//thread separato: aggiungo la spesa
	private class AggiungiSpesaSostenutaTask extends AsyncTask<Object, Object, Boolean> {
		DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(RiconoscimentoVocale.this);
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(RiconoscimentoVocale.this);
		
		protected Boolean doInBackground(Object... params) {
			//tento inserimento del tag nella tabella spese_voci: se � gi� presente meglio
			dbcSpeseVoci.openModifica();
			try {
				dbcSpeseVoci.inserisciVoceSpesa(datiVoce.getVoce(), 0);
			}
			catch (Exception exc) {
				//implementazione vuota
			}
			
			long oggi = FunzioniComuni.getDataAttuale();
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(RiconoscimentoVocale.this);
			String valutaCorrente = pref.getString(VALUTA_CORRENTE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			
			dbcSpeseSostenute.openModifica();
			try {
				dbcSpeseSostenute.insertElement(oggi, datiVoce.getVoce(), datiVoce.getImporto(), valutaCorrente, datiVoce.getImporto(), "", 1, contoDefault, 0);
			}
			catch (Exception exc) {
				return false;
			}
				
			return true;
		}
	
		protected void onPostExecute(Boolean result) {
			if(!result) {
				tvIndicazioni.setVisibility(View.VISIBLE);
				tvIndicazioni.setText(R.string.toast_errore_inserimento);

                soundEffectsManager.playSound(SoundEffectsManager.SOUND_ERROR);
                ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "errore");
				mTts.speak(getResources().getString(R.string.toast_errore_inserimento), TextToSpeech.QUEUE_FLUSH, ttsParams);
			}
			else {
				new MioToast(RiconoscimentoVocale.this, getString(R.string.toast_spesa_aggiunta)).visualizza(Toast.LENGTH_SHORT);
				//aggiorno i budget a seguito dell'inserimento della spesa
                soundEffectsManager.playSound(SoundEffectsManager.SOUND_ADDED);
                new AggiornaTabellaBudgetTask().execute((Object[]) null);
			}
			dbcSpeseVoci.close();
			dbcSpeseSostenute.close();
		}
	}
	
	//thread separato: aggiorno la tabella dei budget a seguito dell'inserimento di una spesa
	private class AggiornaTabellaBudgetTask extends AsyncTask<Object, Object, Boolean> {
		
		protected Boolean doInBackground(Object... params) {
			int budgetAggiornati = 0;
			
			FunzioniAggiornamento aggBudget = new FunzioniAggiornamento(RiconoscimentoVocale.this);
			budgetAggiornati = aggBudget.aggiornaTabBudgetSpeseSost(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + datiVoce.getVoce() + "%", Long.valueOf(FunzioniComuni.getDataAttuale()).toString(), Long.valueOf(FunzioniComuni.getDataAttuale()).toString());

			return budgetAggiornati != -1;
		}
		
		protected void onPostExecute(Boolean result) {
			if(!result) {
				new MioToast(RiconoscimentoVocale.this, getString(R.string.toast_aggiornamentoDatabase_errore)).visualizza(Toast.LENGTH_SHORT);
			}
			
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
			
			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "uscita");
			mTts.speak(getResources().getString(R.string.ricvoc_operazione_completata), TextToSpeech.QUEUE_FLUSH, ttsParams);
		}
	}
	
	//thread separato: aggiungo la entrata
	private class AggiungiEntrataIncassataTask extends AsyncTask<Object, Object, Boolean> {
		DBCEntrateIncassate dbcEntrateIncassate = new DBCEntrateIncassate(RiconoscimentoVocale.this);
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(RiconoscimentoVocale.this);
		
		protected Boolean doInBackground(Object... params) {
			//tento inserimento del tag nella tabella entrate_voci: se � gi� presente meglio
			dbcEntrateVoci.openModifica();
			try {
				dbcEntrateVoci.inserisciVoceEntrata(datiVoce.getVoce(), 1);
			}
			catch (Exception exc) {
				//implementazione vuota
			}
			
			long oggi = FunzioniComuni.getDataAttuale();
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(RiconoscimentoVocale.this);
			String valutaCorrente = pref.getString(VALUTA_CORRENTE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
			
			dbcEntrateIncassate.openModifica();
			try {
				dbcEntrateIncassate.insertElement(oggi, datiVoce.getVoce(), datiVoce.getImporto(), valutaCorrente, datiVoce.getImporto(), "", 1, contoDefault, 0);
			}
			catch (Exception exc) {
				return false;
			}
				
			return true;
		}
	
		protected void onPostExecute(Boolean result) {
			if(!result) {
                soundEffectsManager.playSound(SoundEffectsManager.SOUND_ERROR);
                ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "errore");
				mTts.speak(getResources().getString(R.string.toast_errore_inserimento), TextToSpeech.QUEUE_FLUSH, ttsParams);
				
				tvIndicazioni.setVisibility(View.VISIBLE);
				tvIndicazioni.setText(R.string.toast_errore_inserimento);
			}
			else {
				new MioToast(RiconoscimentoVocale.this, getString(R.string.toast_entrata_aggiunta)).visualizza(Toast.LENGTH_SHORT);
                soundEffectsManager.playSound(SoundEffectsManager.SOUND_ADDED);

                final Intent intAggiornaWidget = new Intent (WIDGET_PICCOLO_AGGIORNA);
				sendBroadcast(intAggiornaWidget);
				
				ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "uscita");
				mTts.speak(getResources().getString(R.string.ricvoc_operazione_completata), TextToSpeech.QUEUE_FLUSH, ttsParams);
			}
			dbcEntrateVoci.close();
			dbcEntrateIncassate.close();
		}
	}
	
	public class DatiVoce {
		private boolean isEntrata;
		private String voce;
		private double importo;
		
		public DatiVoce() {
			
		}
		
		public void setIsEntrata(boolean isEntrata) {
			this.isEntrata = isEntrata;
		}
		
		public boolean getIsEntrata() {
			return this.isEntrata;
		}
		
		public void setVoce(String voce) {
			this.voce = voce;
		}
		
		public String getVoce() {
			return this.voce;
		}
		
		public void setImporto(double importo) {
			this.importo = importo;
		}
		
		public double getImporto() {
			return this.importo;
		}
	}
	
	public class Voce {
		private String voce;
		private int icona;
		
		public Voce() {
			
		}
		
		public Voce(String voce, int icona) {
			setVoce(voce);
			setIcona(icona);
		}
		
		public void setVoce(String voce) {
			this.voce = voce;
		}
		
		public String getVoce() {
			return this.voce;
		}
		
		public void setIcona(int icona) {
			this.icona = icona;
		}
		
		public int getIcona() {
			return this.icona;
		}
	}
}
