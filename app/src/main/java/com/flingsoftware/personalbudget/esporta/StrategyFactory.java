package com.flingsoftware.personalbudget.esporta;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiPreferenze.VALUTA_PRINCIPALE;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.AZIONE_PDF;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.AZIONE_CSV;
import static com.flingsoftware.personalbudget.esporta.MenuEsportaIntentService.CostantiIntentService.AZIONE_XLS;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.flingsoftware.personalbudget.R;

import java.util.Currency;
import java.util.Locale;

/**
 * This class creates and returns the ConcreteStrategy requested by clients to export
 * the database (pdf, csv, xls).
 * Implements the Abstract Factory Pattern.
 */
public class StrategyFactory {
    public static ExportStrategy getStrategy(Context context, String strategyType) {
        String transf = context.getString(R.string.voce_giroconto); // name for transfers

        // Retrieve the main currency from the preferences file.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String valuta = sharedPreferences.getString(VALUTA_PRINCIPALE, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        Currency mainCurrency = Currency.getInstance(valuta);

        ExportStrategy exportStrategy = null;
        switch(strategyType) {
            case AZIONE_PDF:
                exportStrategy = new ExportPDFStrategy(context, transf, mainCurrency);
                break;
            case AZIONE_CSV:
                exportStrategy = new ExportCSVStrategy(context, transf, mainCurrency);
                break;
            case AZIONE_XLS:
                exportStrategy = new ExportXLSStrategy(context, transf);
                break;
        }

        return exportStrategy;
    }
}
