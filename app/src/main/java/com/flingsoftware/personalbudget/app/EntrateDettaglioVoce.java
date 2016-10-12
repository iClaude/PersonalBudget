/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCEntrateIncassate;
import com.flingsoftware.personalbudget.database.DBCEntrateRipetute;
import com.flingsoftware.personalbudget.database.DBCEntrateVoci;
import com.flingsoftware.personalbudget.utilita.BlurBuilder;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;

import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_ELIMINAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_MODIFICA;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.TIPO_OPERAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_PICCOLO_AGGIORNA;


public class EntrateDettaglioVoce extends ExpenseEarningDetails implements SpeseEntrateEliminaVociRipetute.EliminaVociRipetuteListener {

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


	/// Loading images (icon and header) in a separate thread.
	private class LoadImagesTask extends AsyncTask<String, Object, Bitmap> {
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
			final int iconaIdDef = iconaId;
			
			curVoci.close();
			dbcEntrateVoci.close();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new LoadHeaderImageTask().execute(iconaIdDef);
				}
			});
			
			return ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaId, 90, 90);
		}
		
		@Override
		protected void onPostExecute(Bitmap miaBitmap) {
			ivIcona.setImageBitmap(miaBitmap);
			ivIconToolbar.setImageBitmap(miaBitmap);
		}
	}


	// Load the header image and blur it in a separate thread. Takes iconId as parameter.
	private class LoadHeaderImageTask extends AsyncTask<Integer, Object, Bitmap> {
		int backgroundColor = R.color.primary_light;

		@Override
		protected Bitmap doInBackground(Integer... params) {
			int iconaId = params[0];

			Bitmap origBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaId, 128, 128);
			Bitmap blurredBitmap = BlurBuilder.blur(EntrateDettaglioVoce.this, origBitmap);

			// Get a suitable color for image background.
			Palette palette = Palette.from(origBitmap).generate();
			backgroundColor = palette.getMutedColor(ContextCompat.getColor(EntrateDettaglioVoce.this, R.color.primary_light));

			return blurredBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap miaBitmap) {
            ImageView ivHeader = (ImageView) findViewById(R.id.main_imageview_placeholder2);
            ivHeader.setImageBitmap(miaBitmap);
			ivHeader.setBackgroundColor(Color.rgb(Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)));

            // Show the header image with a fadein-fadeout animation.
            ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.vsHeader);
            viewSwitcher.showNext();
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
			dbcEntrateIncassate.insertElement(FunzioniComuni.getDataAttuale(), tag, importo, valuta, importoValprin, descrizione, ripetizione_id, conto, 0);
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
			Cursor curEntrateRipetute = dbcEntrateRipetute.getItemRepeated(params[0]);
			curEntrateRipetute.moveToFirst();
			dbcEntrateRipetute.updateElement(params[0], curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("voce")), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("ripetizione")), curEntrateRipetute.getDouble(curEntrateRipetute.getColumnIndex("importo")), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("valuta")), curEntrateRipetute.getDouble(curEntrateRipetute.getColumnIndex("importo_valprin")), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("descrizione")), curEntrateRipetute.getLong(curEntrateRipetute.getColumnIndex("data_inizio")), 1, oggi.getTimeInMillis(), oggi.getTimeInMillis(), curEntrateRipetute.getString(curEntrateRipetute.getColumnIndex("conto")));
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


	// Animations related to AppBar.

	// Generic alpha animation.
	private void startAlphaAnimation(View view, long duration, int visibility) {
		AlphaAnimation alphaAnimation = (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);
		alphaAnimation.setDuration(duration);
		alphaAnimation.setFillAfter(true);
		view.startAnimation(alphaAnimation);
	}


	// Hide/show title in collapsed app bar.
	private void handleToolbarTitleVisibility(float percentage) {
		if(percentage == 1) {
			ivIconToolbar.setVisibility(View.VISIBLE);
			isIconToolbarVisible = true;
		}
		else if(isIconToolbarVisible) {
			ivIconToolbar.setVisibility(View.INVISIBLE);
			isIconToolbarVisible = false;
		}

		if(percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
			if (!isToolbarTitleVisible) {
				tvToolbarTitle.setVisibility(View.VISIBLE);
				startAlphaAnimation(tvToolbarTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
				isToolbarTitleVisible = true;
			}
		}
		else {
			if(isToolbarTitleVisible) {
				tvToolbarTitle.setVisibility(View.INVISIBLE);
				startAlphaAnimation(tvToolbarTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
				isToolbarTitleVisible = false;
			}
		}
	}


	// Hide/show title in expanded app bar.
	private void handleExpandedTitleVisibility(float percentage) {
		if(percentage <= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
			if (!isExpandedTitleVisible) {
				startAlphaAnimation(llExpandedTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
				isExpandedTitleVisible = true;
			}
		}
		else {
			if(isExpandedTitleVisible) {
				startAlphaAnimation(llExpandedTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
				isExpandedTitleVisible = false;
			}
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
	private ImageView ivIcona;
	private ImageView ivIconToolbar;
	private FloatingActionButton fabAlto;
	private TextView tvToolbarTitle;
	private LinearLayout llExpandedTitle;
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
	private boolean isToolbarTitleVisible = false;
	private boolean isExpandedTitleVisible = true;
	private boolean isIconToolbarVisible = false;

	//gestione suoni
	private SoundPool soundPool;
	private SparseIntArray mappaSuoni;
	private boolean suoniAbilitati;
	private boolean confermaElimina;
	private boolean confermaDuplica;
}
