package com.flingsoftware.personalbudget.preferenze;

import com.flingsoftware.personalbudget.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.support.v7.app.ActionBarActivity;


public class ModificaVoce extends ActionBarActivity {
	
	//costanti
	public static final String EXTRA_VOCE= "voce";
	public static final String EXTRA_IS_SPESA = "isSpesa";
	public static final String EXTRA_ICONA = "icona";
	public static final String EXTRA_NUOVA_VOCE = "nuova_voce";
	public static final String EXTRA_NUOVA_ICONA = "nuova_icona";
	public static final String EXTRA_OPERAZIONE = "operazione";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edittag);

        // Toolbar per menu opzioni
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
		
		Bundle extras = getIntent().getExtras();
		voce = extras.getString(EXTRA_VOCE);
		boolean isSpesa = extras.getBoolean(EXTRA_IS_SPESA);
		icona = extras.getInt(EXTRA_ICONA);
		nuovaIcona = icona;

		TextView tvVoce = (TextView) findViewById(R.id.edittag_tvVoceVecchia);
		tvVoce.setText(voce);
		TextView tvCategoria = (TextView) findViewById(R.id.edittag_tvCategoria);
		if(isSpesa) {
			tvCategoria.setText(R.string.edittag_spese);
		}
		else {
			tvCategoria.setText(R.string.edittag_entrate);
		}
		etVoce = (EditText) findViewById(R.id.edittag_etVoce);	
		ImageButton ibIcona = (ImageButton) findViewById(R.id.edittag_ibIcon);
		ibIcona.setImageDrawable(getResources().getDrawable(arrIconeId[icona]));
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_preferenze_modificavoce, menu);
		
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
		switch(item.getItemId()) {
		case R.id.menu_modifica_voce_elimina:
			AlertDialog.Builder builder = new AlertDialog.Builder(ModificaVoce.this);
			builder.setIcon(R.drawable.ic_action_warning);
			builder.setTitle(R.string.preferenze_voci_conferma_elimina_titolo);
			builder.setMessage(R.string.preferenze_voci_conferma_elimina_messaggio);
			builder.setNegativeButton(R.string.cancella, null);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent();
					intent.putExtra(EXTRA_OPERAZIONE, "elimina");
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			});
			builder.setCancelable(true);
			AlertDialog confirmDialog = builder.create();
			confirmDialog.show();

			return true;
		case R.id.menu_modifica_voce_conferma:
			String nuovaVoce = etVoce.getText().toString();
			if(nuovaVoce == null || nuovaVoce.length() == 0) {
				nuovaVoce = voce;
			}
			
			if(nuovaVoce != voce || icona != nuovaIcona) {
				intent = new Intent();
				intent.putExtra(EXTRA_OPERAZIONE, "modifica");
				intent.putExtra(EXTRA_NUOVA_VOCE, nuovaVoce);
				intent.putExtra(EXTRA_NUOVA_ICONA, nuovaIcona);
				setResult(Activity.RESULT_OK, intent);
				finish();
			
				finish();
			}
			else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
	        
	        return true;
		case android.R.id.home:
			setResult(Activity.RESULT_CANCELED);
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
	
	
	public void selezionaIcona(View v) {
		Intent intent = new Intent(this, IconeSelezione.class);
		startActivityForResult(intent, 0);
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			nuovaIcona = data.getExtras().getInt(EXTRA_NUOVA_ICONA);
			((ImageButton) findViewById(R.id.edittag_ibIcon)).setImageDrawable(getResources().getDrawable(arrIconeId[nuovaIcona]));
		}
	}

	
	//variabili
	private EditText etVoce;
	private String voce;
	private int icona;
	private int nuovaIcona;
	private final static Integer[] arrIconeId = new Integer[] {R.drawable.tag_0, R.drawable.tag_1, R.drawable.tag_2, R.drawable.tag_3, R.drawable.tag_4, R.drawable.tag_5, R.drawable.tag_6, R.drawable.tag_7, R.drawable.tag_8, R.drawable.tag_9,
		R.drawable.tag_10, R.drawable.tag_11, R.drawable.tag_12, R.drawable.tag_13, R.drawable.tag_14, R.drawable.tag_15, R.drawable.tag_16, R.drawable.tag_17, R.drawable.tag_18, R.drawable.tag_19,
		R.drawable.tag_20, R.drawable.tag_21, R.drawable.tag_22, R.drawable.tag_23, R.drawable.tag_24, R.drawable.tag_25, R.drawable.tag_26, R.drawable.tag_27, R.drawable.tag_28, R.drawable.tag_29,
		R.drawable.tag_30, R.drawable.tag_31, R.drawable.tag_32, R.drawable.tag_33, R.drawable.tag_34, R.drawable.tag_35, R.drawable.tag_36, R.drawable.tag_37, R.drawable.tag_38, R.drawable.tag_39,
		R.drawable.tag_40, R.drawable.tag_41, R.drawable.tag_42, R.drawable.tag_43, R.drawable.tag_44, R.drawable.tag_45, R.drawable.tag_46, R.drawable.tag_47, R.drawable.tag_48, R.drawable.tag_49,
		R.drawable.tag_50, R.drawable.tag_51, R.drawable.tag_52, R.drawable.tag_53, R.drawable.tag_54, R.drawable.tag_55, R.drawable.tag_56, R.drawable.tag_57};
}
