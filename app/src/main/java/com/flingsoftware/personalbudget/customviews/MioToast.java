package com.flingsoftware.personalbudget.customviews;

import com.flingsoftware.personalbudget.R;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;


public class MioToast {

	public MioToast(Activity miaActivity, String msg) {//resId è la risorsa String da visualizzare
		this.miaActivity = miaActivity;
		this.msg = msg;
	}
	
	
	public void visualizza(int durata) {//durata Toast.LENGTH_SHORT o Toast.LENGHT_LONG
		LayoutInflater inflater = miaActivity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast, (ViewGroup) miaActivity.findViewById(R.id.toast_llPrincipale));
		TextView text = (TextView) layout.findViewById(R.id.toast_tvTesto);
		text.setText(msg);
		Toast toast = new Toast(miaActivity);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(durata);
		toast.setView(layout);
		toast.show();
	}
	
	
	//variabili
	private String msg;
	private Activity miaActivity;
}
