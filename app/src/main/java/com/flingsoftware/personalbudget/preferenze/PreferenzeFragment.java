/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.preferenze;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;


public class PreferenzeFragment extends PreferenceFragment implements  OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource�
		addPreferencesFromResource(R.xml.preferences);
        getActivity().setTheme(R.style.PreferenceActivityTheme);
		
		Preference prefTags = findPreference("pref_generale_tags");
		prefTags.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intentModificaVoci = new Intent(getActivity(), PreferenzeVoci.class);
				startActivityForResult(intentModificaVoci, 0);
										
				return true;
			}
		});
		
		impostaSommarioPassword();
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("pref_sicurezza_password")) {
			String password = sharedPreferences.getString("pref_sicurezza_password", "");
			Log.e("MIA APP", password);
			
			StringBuilder sommario = new StringBuilder();
			if(password.length() == 0) {
				sommario.append(getActivity().getString(R.string.preferenze_sicurezza_password_noPasswordSet));
			}
			else {
				for(int i=0; i<password.length(); i++) {
					sommario.append("*");
				}
			}
			
			Preference passwordPref = findPreference("pref_sicurezza_password");
			passwordPref.setSummary(sommario.toString());
		} else if (key.equals(MainPersonalBudget.CostantiPreferenze.SUONI_ABILITATI)) {
			boolean suoniAbilitati = sharedPreferences.getBoolean(MainPersonalBudget.CostantiPreferenze.SUONI_ABILITATI, false);
			SoundEffectsManager soundEffectsManager = SoundEffectsManager.getInstance();
			if (suoniAbilitati) {
				soundEffectsManager.loadSounds(getActivity());
			} else {
				soundEffectsManager.release();
			}
		}
	}
	
	
	/*
	 * Se c'� una password, metto asterischi nel sommario della voce password delle preferenze.
	 */
	private void impostaSommarioPassword() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String password = sharedPreferences.getString("pref_sicurezza_password", "");
		
		StringBuilder sommario = new StringBuilder();
		if(password.length() == 0) {
			sommario.append(getActivity().getString(R.string.preferenze_sicurezza_password_noPasswordSet));
		}
		else {
			for(int i=0; i<password.length(); i++) {
				sommario.append("*");
			}
		}
		
		Preference passwordPref = findPreference("pref_sicurezza_password");
		passwordPref.setSummary(sommario.toString());
	}

	
	//se modifico un tag devo refreshare il layout della main activity
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK) {
			getActivity().setResult(Activity.RESULT_OK);
		}
	}
}
