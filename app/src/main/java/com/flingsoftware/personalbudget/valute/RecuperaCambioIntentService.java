package com.flingsoftware.personalbudget.valute;

import static com.flingsoftware.personalbudget.valute.DettaglioValuta.CostantiPubbliche.*;
import static com.flingsoftware.personalbudget.valute.ElencoValute.CostantiPubbliche.*;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.StringTokenizer;
import java.io.InputStreamReader;
import java.io.IOException;


public class RecuperaCambioIntentService extends IntentService {
	//costanti private
	private static final String NAME = "RecuperaCambioIntentService";
	
	//costanti pubbliche
	public interface CostantiPubbliche {	
		String AZIONE_RECUPERA_CAMBIO = "com.flingsoftware.personalbudget.RECUPERA_CAMBIO";
		String EXTRA_CAMBIO = "tassoCambio";
	}
	
	
	public RecuperaCambioIntentService() {
		super(NAME);
		setIntentRedelivery(false);
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		
		if(action.equals(CostantiPubbliche.AZIONE_RECUPERA_CAMBIO)) {
			String codiceValuta = intent.getStringExtra(ELENCO_VALUTE_VALUTA_CODICE);
			String codiceValutaDefault = intent.getStringExtra(ELENCO_VALUTE_VALUTADEFAULT_CODICE);
			recuperaCambio(codiceValuta, codiceValutaDefault);
		}
	}
		
	
	/*
	 * Recupero il cambio della valuta specificata utilizzando il servizio web Yahoo Finance.
	 */
	private void recuperaCambio(String orig, String dest) {
		float tassoCambio = -1f;
		
		//codice per il recupero del cambio da Yahoo Finance
		String indirizzo = "http://download.finance.yahoo.com/d/quotes.csv?s=" + orig + dest + "=X&f=sl1d1t1ba&e=.csv";
		try {
			//recupero il file csv dalla rete e lo salvo in una cartella temporanea
			URL url = new URL(indirizzo);
	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setUseCaches(true);
			urlConnection.connect();		
			
			if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		        InputStream inputStream = urlConnection.getInputStream();
		        
		        String risposta = leggiStreamInStringa(inputStream);
		        StringTokenizer st = new StringTokenizer(risposta, ",");
		        st.nextToken();
		        String cambioStr = st.nextToken();
		        tassoCambio = Float.parseFloat(cambioStr);
		        inputStream.close();
			}
		}
		catch(Exception exc) {
			tassoCambio = -1f;
			exc.printStackTrace();
		}
		
		//comunico il risultato alla Activity chiamante
		Intent broadcastIntent = new Intent(LOCAL_BROADCAST_RECUPERA_CAMBIO);
		broadcastIntent.putExtra(CostantiPubbliche.EXTRA_CAMBIO, tassoCambio);
		LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
		localBroadcastManager.sendBroadcast(broadcastIntent);	
	}
	
	
	private String leggiStreamInStringa (InputStream inputStream) throws IOException {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder stringBuilder = new StringBuilder();
			char[] buffer = new char[1024];
			int carLetti;
			while((carLetti = bufferedReader.read(buffer)) != -1) {
				stringBuilder.append(buffer, 0, carLetti);
			}
			
			return stringBuilder.toString();
		}
		finally {
			inputStream.close();
		}
	}
}
