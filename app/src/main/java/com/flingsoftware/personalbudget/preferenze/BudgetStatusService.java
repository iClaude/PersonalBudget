/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.preferenze;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.FunzioniComuni;
import com.flingsoftware.personalbudget.app.Password;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiNotifiche.NOTIFICA_AVVIA_APP;


public class BudgetStatusService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//recupero l'elenco dei budget scaduti in un thread separato
		new RecuperaBudgetScadutiTask().execute((Object[]) null);
		
		return START_NOT_STICKY;
	}
	
	
	//AsyncTask per recuperare l'elenco dei budget scaduti o quasi
	private class RecuperaBudgetScadutiTask extends AsyncTask<Object, Object, Cursor> {
		DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetStatusService.this);
		
		protected Cursor doInBackground(Object... params) {
			//recupero un Cursor con i budget esauriti o quasi
			dbcSpeseBudget.openLettura();
			Cursor curBudgetScaduti = dbcSpeseBudget.getSpeseBudgetElencoBudgetScadutiOQuasi(FunzioniComuni.getDataAttuale());
			
			return curBudgetScaduti;
		}
		
		protected void onPostExecute(Cursor curBudgetScaduti) {
			if(curBudgetScaduti.getCount() > 0) {
				//creazione notifica con info sui budget scaduti o quasi
				NotificationCompat.Builder builder = new NotificationCompat.Builder(BudgetStatusService.this);
				builder.setSmallIcon(R.drawable.ic_notifica);
				builder.setContentTitle(getResources().getString(R.string.notifica_budgetEsauriti_titolo));
				builder.setContentText(getResources().getString(R.string.notifica_budgetEsauriti_testo));
				
				String tipiBudget[] = getResources().getStringArray(R.array.ripetizioni_budget);
				
				NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
				while(curBudgetScaduti.moveToNext()) {
					String voce = curBudgetScaduti.getString(curBudgetScaduti.getColumnIndex("voce"));
					if(voce.endsWith(",")) {
						voce = voce.substring(0, voce.length() - 1);
					}
					String ripetizione = curBudgetScaduti.getString(curBudgetScaduti.getColumnIndex("ripetizione"));
					
					//conversione ripetizione budget nella lingua dell'app
					if(ripetizione.equals("una_tantum")) {
						ripetizione = tipiBudget[0];
					}
					else if(ripetizione.equals("giornaliero")) {
						ripetizione = tipiBudget[1];
					}
					else if(ripetizione.equals("settimanale")) {
						ripetizione = tipiBudget[2];
					}
					else if(ripetizione.equals("bisettimanale")) {
						ripetizione = tipiBudget[3];
					}
					else if(ripetizione.equals("mensile")) {
						ripetizione = tipiBudget[4];
					}
					else if(ripetizione.equals("annuale")) {
						ripetizione = tipiBudget[5];
					}
					
					inboxStyle.addLine(ripetizione + " " + voce);
				}
				inboxStyle.setSummaryText(getResources().getString(R.string.notifica_budgetEsauriti_testo));
				builder.setStyle(inboxStyle);
				builder.setContentInfo("" + curBudgetScaduti.getCount());
				
				Intent notificationIntent = new Intent(BudgetStatusService.this, Password.class);
				PendingIntent piAvvioApp = PendingIntent.getActivity(BudgetStatusService.this, 0, notificationIntent, 0);
				builder.setContentIntent(piAvvioApp);
				builder.setAutoCancel(true);
				
				Notification notification = builder.build();
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.notify(NOTIFICA_AVVIA_APP, notification);
			}
		
			dbcSpeseBudget.close();
			curBudgetScaduti.close();
		}
	}
}
