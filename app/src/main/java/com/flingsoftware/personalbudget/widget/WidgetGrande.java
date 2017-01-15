/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget;
import com.flingsoftware.personalbudget.app.Preferiti;
import com.flingsoftware.personalbudget.app.SpeseAggiungi;
import com.flingsoftware.personalbudget.app.budgets.BudgetDetails;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;


public class WidgetGrande extends AppWidgetProvider {
	
	public static final String BUDGET_ACTION = "com.flingsoftware.personalbudget.BUDGET_ACTION";
	public static final String EXTRA_ITEM = "com.flingsoftware.personalbudget.EXTRA_ITEM";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {				
		for(int i=0; i<appWidgetIds.length; i++) {
            Intent intent = new Intent(context, WidgetGrandeService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_grande);
            rv.setRemoteAdapter(R.id.widget_grande_lvBudget, intent);
            rv.setEmptyView(R.id.widget_grande_lvBudget, R.id.widget_grande_tvNoData);
            
            //logo e simbolo spese cliccabili sul titolo del widget
            Intent intMain = new Intent(context, MainPersonalBudget.class);
            PendingIntent pintMain = PendingIntent.getActivity(context, 0, intMain, 0);
            rv.setOnClickPendingIntent(R.id.widget_grande_ivLogo, pintMain);
            
            Intent intSpese = new Intent(context, SpeseAggiungi.class);
            PendingIntent pintSpese = PendingIntent.getActivity(context, 0, intSpese, 0);
            rv.setOnClickPendingIntent(R.id.widget_grande_ivAggiungiSpesa, pintSpese);

            Intent intPreferiti = new Intent(context, Preferiti.class);
            PendingIntent pintPreferiti = PendingIntent.getActivity(context, 0, intPreferiti, 0);
            rv.setOnClickPendingIntent(R.id.ivPreferiti, pintPreferiti);
            
            //singoli elementi della ListView cliccabili
            Intent intBudget = new Intent(context, WidgetGrande.class);
            intBudget.setAction(BUDGET_ACTION);
            intBudget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intBudget.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pintBudget = PendingIntent.getBroadcast(context, 0, intBudget, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_grande_lvBudget, pintBudget);
            
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.widget_grande_lvBudget);
        }
		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	
	@Override
	public void onReceive(Context context, Intent intent) {				
		String action = intent.getAction();
	    if (action.equals(WIDGET_AGGIORNA) || action.equals(Intent.ACTION_DATE_CHANGED)) 
	    {
	        AppWidgetManager gm = AppWidgetManager.getInstance(context);
	        int[] ids = gm.getAppWidgetIds(new ComponentName(context, WidgetGrande.class));
	        onUpdate(context, gm, ids);
	        
	    }
	    else if(action.equals(BUDGET_ACTION)) {
	    	long budgetId = intent.getLongExtra(EXTRA_ITEM, 0);

            Intent intBudget = BudgetDetails.makeIntent(context, budgetId);
            intBudget.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intBudget);
        }
	    else 
	    {
	        super.onReceive(context, intent);
	    }
	}
	
	
	@Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }
	

    @Override
    public void onDisabled(Context context) 
    {
        super.onDisabled(context);
    }

    
    @Override
    public void onEnabled(Context context) 
    {
        super.onEnabled(context);
    }
}
