package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class Password extends Activity {
	
	//costanti
	public interface CostantiPassword {
		String PREFERENZE_PASSWORD_ATTIVATA = "pref_sicurezza_password_attivata";
		String PREFERENZE_PASSWORD = "pref_sicurezza_password";
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password);
		
		//reference ai componenti
		etPassword = (EditText) findViewById(R.id.password_etPassword);
		tvTentativi = (TextView) findViewById(R.id.spese_entrate_dettaglio_voce_tvImportoOrigLabel);
		
		tentativi = 3;
		tvTentativi.setText(getString(R.string.password_xTentativiRimasti, tentativi));
		
		//verifico se l'app è protetta da password
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean protetta = pref.getBoolean(CostantiPassword.PREFERENZE_PASSWORD_ATTIVATA, false);
		password = pref.getString(CostantiPassword.PREFERENZE_PASSWORD, "");
		
		if(!protetta || password.length() == 0) {
			Intent avviaApp = new Intent(this, MainPersonalBudget.class);
			startActivity(avviaApp);
			finish();
		}
	}
	
	public void Annulla(View v) {
		finish();
	}
	
	public void OK(View v) {
		String passwordInserita = etPassword.getText().toString();
		if(password.equals(passwordInserita)) {
			Intent avviaApp = new Intent(this, MainPersonalBudget.class);
			startActivity(avviaApp);
			finish();
		}
		else {
			tentativi--;
			if(tentativi == 0) finish();
			tvTentativi.setText(getString(R.string.password_xTentativiRimasti, tentativi));
			tvTentativi.setTextColor(Color.RED);
			etPassword.setText("");
		}
	}

	//variabili di istanza
	private int tentativi;
	private String password;
	
	private EditText etPassword;
	private TextView tvTentativi;
}
