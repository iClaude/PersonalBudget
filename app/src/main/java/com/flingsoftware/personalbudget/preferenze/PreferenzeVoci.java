package com.flingsoftware.personalbudget.preferenze;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;
import static com.flingsoftware.personalbudget.database.StringheSQL.ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.FunzioniAggiornamento;
import com.flingsoftware.personalbudget.preferenze.ModificaVoce;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.database.MergeCursor;
import android.widget.AdapterView;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.app.ActivityOptions;

import java.util.StringTokenizer;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.List;


public class PreferenzeVoci extends ActionBarActivity {
	
	//costanti
	private static final int REQUEST_CODE_MODIFICA_ELIMINA_VOCE = 0;
	private static final int REQUEST_CODE_AGGIUNGI_VOCE = 1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
        }

		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferenze_voci);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		iconeVeloci = new ListViewIconeVeloce(this);
		new PlaceHolderWorkerTask().execute(R.drawable.tag_0, R.drawable.tag_1);
		
		//EditText per la ricerca
		((EditText) findViewById(R.id.preferenze_voci_etVoce)).addTextChangedListener(etVoceListener);
		
		//impostazione ListView
		lvVoci = (ListView) findViewById(R.id.preferenze_voci_lvVoci);
		lvVociAdapter = new MioAdapter(this, lstVoci);	
		lvVoci.setAdapter(lvVociAdapter);
		aggiornaCursor();
		
		lvVoci.setOnItemClickListener(new AdapterView.OnItemClickListener(){
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
	        	voceInteressata = lstVoci.get(position);
	        	String nomeVoce = voceInteressata.getVoce();
	        	if(nomeVoce.equals(getString(R.string.voce_giroconto))) {
	        		new MioToast(PreferenzeVoci.this, getString(R.string.preferenze_voci_modificaNonPermessa)).visualizza(Toast.LENGTH_SHORT);
	        	}
	        	else {
		        	Intent intent = new Intent(PreferenzeVoci.this, ModificaVoce.class);
		        	intent.putExtra(ModificaVoce.EXTRA_IS_SPESA, voceInteressata.getIsSpesa());
		        	intent.putExtra(ModificaVoce.EXTRA_VOCE, voceInteressata.getVoce());
		        	intent.putExtra(ModificaVoce.EXTRA_ICONA, voceInteressata.getIcona());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivityForResult(intent, REQUEST_CODE_MODIFICA_ELIMINA_VOCE, ActivityOptions.makeSceneTransitionAnimation(PreferenzeVoci.this).toBundle());
                    }
                    else {
                        startActivityForResult(intent, REQUEST_CODE_MODIFICA_ELIMINA_VOCE);
                    }
	        	}
	        }
		});
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_preferenzevoci, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
	        
	        return true;
		case R.id.menu_preferenzevoci_aggiungi:
			Intent intent = new Intent(PreferenzeVoci.this, AggiungiVoce.class);
			startActivityForResult(intent, 	REQUEST_CODE_AGGIUNGI_VOCE);

			return true;	
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	//aggiorna la ListView recuperando i dati dal database
	private void aggiornaCursor() {
		new RecuperaElencoVociTask().execute((Object[]) null);
	}
	
	
	private static class ViewHolder {
		ImageView ivIcona;
		TextView tvVoce;
        TextView tvTipoVoce;
	}
	
	
	private class MioAdapter extends ArrayAdapter<Voce> {
		private LayoutInflater inflater;
		private List<Voce> lstVoci;
		
		public MioAdapter(Context context, List<Voce> lstVoci) {
		    super(context, -1, lstVoci);
		    this.lstVoci = lstVoci;
		    inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.preferenze_voci_listview_item, parent, false);
				viewHolder = new ViewHolder();
				viewHolder.ivIcona = (ImageView) convertView.findViewById(R.id.preferenze_voci_listview_item_ivIcona);
				viewHolder.tvVoce = (TextView) convertView.findViewById(R.id.preferenze_voci_listview_item_tvVoce);
                viewHolder.tvTipoVoce = (TextView) convertView.findViewById(R.id.preferenze_voci_listview_item_tvTipoVoce);
                convertView.setTag(viewHolder);
			}
			else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			Voce voce = lstVoci.get(position);
			if(position < primoIndiceEntrate) {
				viewHolder.tvTipoVoce.setText(getString(R.string.tab_spese).toUpperCase());
                viewHolder.tvTipoVoce.setTextColor(getResources().getColor(R.color.rosso));
				iconeVeloci.loadBitmap(lstVoci.get(position).getIcona(), viewHolder.ivIcona, mPlaceHolderBitmapSpese, 50, 50);
			}
			else {
                viewHolder.tvTipoVoce.setText(getString(R.string.tab_entrate).toUpperCase());
                viewHolder.tvTipoVoce.setTextColor(getResources().getColor(R.color.colore_blu_scuro));
				iconeVeloci.loadBitmap(lstVoci.get(position).getIcona(), viewHolder.ivIcona, mPlaceHolderBitmapEntrate, 50, 50);
			}
			viewHolder.tvVoce.setText(voce.getVoce());
			
			return convertView;
		}
	}
	
	
	private class Voce {
		private long idVoce;
		private String voce;
		private int icona;
		private boolean isSpesa;
			
		
		Voce(boolean isSpesa, long idVoce, String voce, int icona) {
			setIsSpesa(isSpesa);
			setIdVoce(idVoce);
			setVoce(voce);
			setIcona(icona);
		}
		
		
		public void setIsSpesa(boolean isSpesa) {
			this.isSpesa = isSpesa;
		}
		
		
		public void setIdVoce(long idVoce) {
			this.idVoce = idVoce;
		}
		
		
		public void setVoce(String voce) {
			this.voce = voce;
		}
		
		
		public void setIcona(int icona) {
			this.icona = icona;
		}
		
		
		public boolean getIsSpesa() {
			return this.isSpesa;
		}
		
		public long getIdVoce() {
			return this.idVoce;
		}
		
		
		public String getVoce() {
			return this.voce;
		}
		
		
		public int getIcona() {
			return this.icona;
		}
	}
		

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == REQUEST_CODE_MODIFICA_ELIMINA_VOCE) {		
				String operazione = data.getExtras().getString(ModificaVoce.EXTRA_OPERAZIONE);
				
				if(operazione.equals("modifica")){
					String nuovaVoce = data.getExtras().getString(ModificaVoce.EXTRA_NUOVA_VOCE);		
					int nuovaIcona = data.getExtras().getInt(ModificaVoce.EXTRA_NUOVA_ICONA);
					if(voceInteressata.getIsSpesa()) {
						new AggiornaVoceSpesaTask().execute(voceInteressata.getIdVoce(), nuovaVoce, voceInteressata.getVoce(), nuovaIcona);
					}
					else {
						new AggiornaVoceEntrataTask().execute(voceInteressata.getIdVoce(), nuovaVoce, voceInteressata.getVoce(), nuovaIcona);
					}
				}
				else if(operazione.equals("elimina")) {
					if(voceInteressata.getIsSpesa()) {
						new EliminaTagSpesaTask().execute(voceInteressata.getIdVoce());
						//aggiorno la tabella spese_budget campo spesa_sost a seguito della eliminazione della/e spesa/e
						new AggiornaTabellaBudgetTask(ESTRAI_BUDGET_PER_AGGIUNTA_ELIMINAZIONE_SPESA, "%" + voceInteressata.getVoce() + "%", Long.valueOf(new GregorianCalendar(2076, 4, 22).getTimeInMillis()).toString(),  Long.valueOf(new GregorianCalendar(1976, 4, 22).getTimeInMillis()).toString()).execute((Object[]) null);
						setResult(Activity.RESULT_OK);
					}
					else {
						new EliminaTagEntrataTask().execute(voceInteressata.getIdVoce());
					}
				}
			}
			else if(requestCode == REQUEST_CODE_AGGIUNGI_VOCE) {
				aggiornaCursor();
			}
					
			setResult(Activity.RESULT_OK);
		}
	}
	
	
	//AsyncTask per recuperare l'elenco dei tag delle spese.
	private class RecuperaElencoVociTask extends AsyncTask<Object, Object, MergeCursor> {
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(PreferenzeVoci.this);
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(PreferenzeVoci.this);
		
		protected MergeCursor doInBackground(Object... params) {
			dbcSpeseVoci.openLettura();
			curVociSpese = dbcSpeseVoci.getTutteLeVociFiltrato(ricerca.toString());
			primoIndiceEntrate = curVociSpese.getCount();
			dbcEntrateVoci.openLettura();
			curVociEntrate = dbcEntrateVoci.getTutteLeVociFiltrato(ricerca.toString());
			MergeCursor mcVoci = new MergeCursor(new Cursor[] {curVociSpese, curVociEntrate});
			
			return mcVoci;
		}
		
		protected void onPostExecute(MergeCursor mcVoci) {
			lstVoci.clear();
			while(mcVoci.moveToNext()) {
				lstVoci.add(new Voce(mcVoci.getPosition() < primoIndiceEntrate, mcVoci.getLong(mcVoci.getColumnIndex("_id")), mcVoci.getString(mcVoci.getColumnIndex("voce")), mcVoci.getInt(mcVoci.getColumnIndex("icona"))));
			}
			mcVoci.close();
			dbcSpeseVoci.close();
			dbcEntrateVoci.close();
			
			lvVociAdapter.notifyDataSetChanged();
			
			dbcSpeseVoci.close();
			dbcEntrateVoci.close();
		}
	}
	
	
	//listener per la casella di testo di ricerca
	private TextWatcher etVoceListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			ricerca.delete(0, ricerca.length());
			ricerca.append(s);
			
			aggiornaCursor();
		}

	};
	
	

	
	
	class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {   
	    // Decode image in background.
	    @Override
	    protected Object doInBackground(Integer... params) {
	    	mPlaceHolderBitmapSpese = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 50, 50);
	    	mPlaceHolderBitmapEntrate = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[1], 50, 50);
	    	return null;
	    }
	}
	
	
	//AsyncTask per per aggiornare la voce di spesa
	private class AggiornaVoceSpesaTask extends AsyncTask<Object, Object, Object> {
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(PreferenzeVoci.this);
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(PreferenzeVoci.this);
		
		protected Object doInBackground(Object... params) {
			dbcSpeseVoci.openModifica();
			dbcSpeseVoci.aggiornaVoceSpesa((Long) params[0], (String) params[1], (Integer) params[3]);
			dbcSpeseVoci.close();
			
			dbcSpeseBudget.openModifica();
			Cursor budgetInteressati = dbcSpeseBudget.getSpeseBudgetElencoBudgetContenentiVoce((String) params[2]);
			while (budgetInteressati.moveToNext()) {
				long idVecchio = budgetInteressati.getLong(budgetInteressati.getColumnIndex("_id"));
				String voceVecchia = budgetInteressati.getString(budgetInteressati.getColumnIndex("voce"));
				String ripetizioneVecchia = budgetInteressati.getString(budgetInteressati.getColumnIndex("ripetizione"));
				double importoVecchio = budgetInteressati.getDouble(budgetInteressati.getColumnIndex("importo"));
				String valutaVecchia = budgetInteressati.getString(budgetInteressati.getColumnIndex("valuta"));
				double importoValprinVecchio = budgetInteressati.getDouble(budgetInteressati.getColumnIndex("importo_valprin"));
				long dataInizioVecchia = budgetInteressati.getLong(budgetInteressati.getColumnIndex("data_inizio"));
				long dataFineVecchia = budgetInteressati.getLong(budgetInteressati.getColumnIndex("data_fine"));
				int aggiungereRimanenzaVecchia = budgetInteressati.getInt(budgetInteressati.getColumnIndex("aggiungere_rimanenza"));
				double spesaSostVecchia = budgetInteressati.getDouble(budgetInteressati.getColumnIndex("spesa_sost"));
				double risparmioVecchio = budgetInteressati.getDouble(budgetInteressati.getColumnIndex("risparmio"));
				long budgetInizialeVecchio = budgetInteressati.getLong(budgetInteressati.getColumnIndex("budget_iniziale"));
				int ultimoAggiuntoVecchio = budgetInteressati.getInt(budgetInteressati.getColumnIndex("ultimo_aggiunto"));
				
				StringBuilder voceNuova = new StringBuilder();
				StringTokenizer stVoce = new StringTokenizer(voceVecchia, ",", true);
				while(stVoce.hasMoreTokens()) {
					String tag = stVoce.nextToken();
					if(tag.equals(params[2])) {
						voceNuova.append((String) params[1]);
					}
					else {
						voceNuova.append(tag);
					}
				}
				
				dbcSpeseBudget.aggiornaSpesaBudget(idVecchio, voceNuova.toString(), ripetizioneVecchia, importoVecchio, valutaVecchia, importoValprinVecchio, dataInizioVecchia, dataFineVecchia, aggiungereRimanenzaVecchia, spesaSostVecchia, risparmioVecchio, budgetInizialeVecchio, ultimoAggiuntoVecchio);			
			}
			
			dbcSpeseBudget.close();
			budgetInteressati.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			aggiornaCursor();
			
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
		}
	}
	
	
	//AsyncTask per per aggiornare la voce di entrata
	private class AggiornaVoceEntrataTask extends AsyncTask<Object, Object, Object> {
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(PreferenzeVoci.this);
		
		protected Object doInBackground(Object... params) {
			dbcEntrateVoci.openModifica();
			dbcEntrateVoci.aggiornaVoceEntrata((Long) params[0], (String) params[1], (Integer) params[3]);
			dbcEntrateVoci.close();
							
			return null;
		}
		
		protected void onPostExecute(Object result) {
			aggiornaCursor();
		}
	}
	
	
	//AsyncTask per eliminare un tag di spesa
	private class EliminaTagSpesaTask extends AsyncTask<Long, Object, Object> {
		DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(PreferenzeVoci.this);
		
		protected Object doInBackground(Long... params) {
			dbcSpeseVoci.openModifica();
			dbcSpeseVoci.eliminaVoceSpesa(params[0]);
			dbcSpeseVoci.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			aggiornaCursor();
		}
	}
	
	
	//AsyncTask per eliminare un tag di entrata
	private class EliminaTagEntrataTask extends AsyncTask<Long, Object, Object> {
		DBCEntrateVoci dbcEntrateVoci = new DBCEntrateVoci(PreferenzeVoci.this);
		
		protected Object doInBackground(Long... params) {
			dbcEntrateVoci.openModifica();
			dbcEntrateVoci.eliminaVoceEntrata(params[0]);
			dbcEntrateVoci.close();
			
			return null;
		}
		
		protected void onPostExecute(Object result) {
			aggiornaCursor();
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
			FunzioniAggiornamento aggBudget = new FunzioniAggiornamento(PreferenzeVoci.this);
			int budgetAggiornati = aggBudget.aggiornaTabBudgetSpeseSost(query, args);
			
			DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(PreferenzeVoci.this);
			dbcSpeseBudget.openModifica();
			String voce = args[0].substring(1, args[0].length() - 1);
			dbcSpeseBudget.AggiornaBudgetPerEliminazioneVoce(voce);
			dbcSpeseBudget.close();

			return budgetAggiornati != -1;
		}
		
		protected void onPostExecute(Boolean result) {
			if(!result) {
				new MioToast(PreferenzeVoci.this, getString(R.string.toast_aggiornamentoDatabase_errore)).visualizza(Toast.LENGTH_SHORT);
			}
			
			final Intent intAggiornaWidget = new Intent (WIDGET_AGGIORNA);
			sendBroadcast(intAggiornaWidget);
		}
	}
	
	
	//variabili di istanza
	private StringBuilder ricerca = new StringBuilder(); //filtro per le ricerche
	private MioAdapter lvVociAdapter;
	private Cursor curVociSpese;
	private Cursor curVociEntrate;
	private int primoIndiceEntrate;
	
	private ListView lvVoci;
	private ArrayList<Voce> lstVoci = new ArrayList<Voce>();
	private Voce voceInteressata;
	
	private Bitmap mPlaceHolderBitmapSpese;
	private Bitmap mPlaceHolderBitmapEntrate;
	private ListViewIconeVeloce iconeVeloci;
}
