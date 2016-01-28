package com.flingsoftware.personalbudget.preferenze;

import com.flingsoftware.personalbudget.preferenze.ReminderService;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootUpReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	//attivo l'AlarmManager per il reminder (la notifica è creata nel Service ReminderService)
		Intent myIntent = new Intent(context , ReminderService.class);    
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, myIntent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent);  //set repeating every 24 hours		
    
		//attivo l'AlarmManager per il controllo dei budget (la notifica è creata nel Service BudgetStatusService)
		Intent myIntent2 = new Intent(context , BudgetStatusService.class);    
		PendingIntent pendingIntent2 = PendingIntent.getService(context, 0, myIntent2, 0);
		AlarmManager alarmManager2 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 24*60*60*1000 , pendingIntent2);  //set repeating every 24 hours		
        }
}
