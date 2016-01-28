package com.flingsoftware.personalbudget.app;

import com.flingsoftware.personalbudget.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class SpeseEntrateEliminaVociRipetute extends DialogFragment {
	
	//costanti
	public static final int ELIMINA_SOLO_QUESTA = 0;
	public static final int ELIMINA_TUTTE = 1;
	public static final int ELIMINA_DA_OGGI = 2;
	
	//interfaccia per comunicare con l'activity chiamante
	private EliminaVociRipetuteListener mioListener;
	
	public interface EliminaVociRipetuteListener {
		void onDialogPositiveClick(int sceltaElimina);
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		sceltaElimina = ELIMINA_SOLO_QUESTA;
		
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.dettagli_voce_elimina_ripetute_titolo);
	    builder.setSingleChoiceItems(R.array.dettagli_voce_elimina_ripetute_scelte, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
				case 0:
					sceltaElimina = ELIMINA_SOLO_QUESTA;
					break;
				case 1:
					sceltaElimina = ELIMINA_TUTTE;
					break;
				case 2:      		
	        		sceltaElimina = ELIMINA_DA_OGGI;
	        		break;
				}	
			}
		});
	    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mioListener.onDialogPositiveClick(sceltaElimina);
				
			}
		});
	    builder.setNegativeButton(R.string.cancella, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});

	    return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
        try {
            mioListener = (EliminaVociRipetuteListener) activity;
        } 
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }


	//variabili di istanza
	private int sceltaElimina;
}
