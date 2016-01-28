package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.LOWNDES_TIPS_ABILITATI;

import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.CheckBox;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class LowndesTips extends DialogFragment {

	public static final String TITOLO = "titolo";
	public static final String MESSAGGIO = "messaggio";
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {	
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    View parentView = inflater.inflate(R.layout.lowndes_tips, null);
	    TextView tvTesto = (TextView) parentView.findViewById(R.id.lowndes_tips_tvTesto);
	    tvTesto.setText(getArguments().getString(MESSAGGIO));
	    cbDisabilitaTips = (CheckBox) parentView.findViewById(R.id.lowndes_tips_cbDisabilitaTips);
	    
	    builder.setView(parentView)
	    	.setTitle(getArguments().getString(TITOLO))
	        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	        	@Override
	        	public void onClick(DialogInterface dialog, int id) {
	        		if(cbDisabilitaTips.isChecked()) {
	        			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	        			SharedPreferences.Editor prefEditor = sharedPreferences.edit();
	        			prefEditor.putBoolean(LOWNDES_TIPS_ABILITATI, false);
	        			prefEditor.apply();
	        		}
	        		dismiss();
	        	}
	        });
   
	    return builder.create();
	}
	
	
	//variabili
	private CheckBox cbDisabilitaTips;
}
