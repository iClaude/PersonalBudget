package com.flingsoftware.personalbudget.preferenze;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.Password;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiNotifiche.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;



public class ReminderService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//creazione notifica per ricordarsi di aggiungere spese
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_notifica);
		builder.setContentTitle(getString(R.string.notifica_reminder_titolo));
		builder.setContentText(getString(R.string.notifica_reminder_testo));
		
		
		Intent notificationIntent = new Intent(this, Password.class);
		PendingIntent piAvvioApp = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		builder.setContentIntent(piAvvioApp);
		builder.setAutoCancel(true);
		
		Notification notification = builder.build();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICA_AVVIA_APP, notification);
		
		return START_NOT_STICKY;
	}
}
