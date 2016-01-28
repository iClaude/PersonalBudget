package com.flingsoftware.personalbudget.app;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.*;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	
	public interface DialogFinishedListener {
		void onDialogFinished(int id, int year, int month, int day);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//recuperare la variabile id
		Bundle args = getArguments();
		if(args != null) {
			id = args.getInt(ID_DATEPICKER);
		}
		
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
	// Do something with the date chosen by the user
		DialogFinishedListener listener = (DialogFinishedListener) getActivity();
		listener.onDialogFinished(id, year, month, day);
	}
	
	
	//variabili di istanza
	private int id;
}
