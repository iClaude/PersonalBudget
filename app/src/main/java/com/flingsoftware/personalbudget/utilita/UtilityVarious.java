/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.utilita;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.view.Window;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.MainPersonalBudget;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class UtilityVarious {

    // ***********************************************************************************
    // User input.
    // ***********************************************************************************

    /*
        Visualizza un AlertDialog con pulsanti OK, con relativo listener che definisce le operazioni
        da eseguire, e Annulla (se la variabile boolean ? impostata su true), che in questo caso non
        fa nulla.
    */
    public static void visualizzaDialogOKAnnulla(Context context, String titolo, String msg, String OK, boolean bottAnnulla, String annulla, int idIcona, DialogInterface.OnClickListener OKListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titolo);
        builder.setMessage(msg);
        builder.setPositiveButton(OK, OKListener);
        if (bottAnnulla) {
            builder.setNegativeButton(annulla, null);
        }
        builder.setCancelable(true);
        if (idIcona != 0) {
            builder.setIcon(idIcona);
        }
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }


    // ***********************************************************************************
    // Multimedia and graphic effects.
    // ***********************************************************************************

    /**
     * Converte una bitmap passata come parametro in scala di grigi.
     * @param originalImage la bitmap da convertire in grigio
     */
    public static Bitmap filtroGrigio(Bitmap originalImage) {
        boolean hasTransparent = originalImage.hasAlpha();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Bitmap grayScaleImage =
                originalImage.copy(originalImage.getConfig(),
                        true);

        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            // Break out if we've been interrupted.
            if (Thread.interrupted())
                return null;

            for (int j = 0; j < width; ++j) {
                // Check if the pixel is transparent in the original
                // by checking if the alpha is 0.
                if (hasTransparent
                        && ((grayScaleImage.getPixel(j, i)
                        & 0xff000000) >> 24) == 0)
                    continue;

                // Convert the pixel to grayscale.
                int pixel = grayScaleImage.getPixel(j, i);
                int grayScale =
                        (int) (Color.red(pixel) * .299
                                + Color.green(pixel) * .587
                                + Color.blue(pixel) * .114);
                grayScaleImage.setPixel(j, i,
                        Color.rgb(grayScale,
                                grayScale,
                                grayScale));
            }
        }

        return grayScaleImage;
    }


    // Get the height of the action bar programmatically.
    public static int getActionBarHeight(Context mContext) {
        TypedArray a = mContext.getTheme().obtainStyledAttributes(new int[] {R.attr.actionBarSize});
        int actionBarHeight = a.getDimensionPixelSize(0, 0);
        a.recycle();

        return actionBarHeight;
    }

    // Get the status bar height.
    public static int getStatusBarHeight(Activity mActivity) {
        Rect rectangle = new Rect();
        Window window = mActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;

        return statusBarHeight;
    }


    // ***********************************************************************************
    // Shared preferences.
    // ***********************************************************************************

    // Get the current Currency from the preferences.
    public static Currency getPrefCurrency(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String currCode = pref.getString(Constants.PREF_CURRENCY, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        return Currency.getInstance(currCode);
    }

    // Are sounds effects enabled? Check the preferences.
    public static boolean soundsEnabled(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(MainPersonalBudget.CostantiPreferenze.SUONI_ABILITATI, false);
    }

    /*
        Given an amount returns a formatted String using the default Locale and the
        main currency saved in the preferences.
    */
    public static String getFormattedAmount(double amount, Context context) {
        // Get the main currency from the preferences.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String currCode = pref.getString(Constants.PREF_CURRENCY, Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        Currency prefCurrency = Currency.getInstance(currCode);
        // Create a formatter using the default Locale and the preferred currency.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(prefCurrency);
        String amountFormatted = nf.format(amount);

        return amountFormatted;
    }

    // ***********************************************************************************
    // Others
    // ***********************************************************************************

    /*
        Given the String representing the repetition of this budget, returns a String representing
        the repetition in the correct language (bad code: legacy approach).
     */
    public static String getBudgetType(Context context, String repetition) {
        String[] budgetTypes = context.getResources().getStringArray(R.array.ripetizioni_budget);
        String budgetType = null;
        if (repetition.equals("una_tantum")) {
            budgetType = budgetTypes[0];
        } else if (repetition.equals("giornaliero")) {
            budgetType = budgetTypes[1];
        } else if (repetition.equals("settimanale")) {
            budgetType = budgetTypes[2];
        } else if (repetition.equals("bisettimanale")) {
            budgetType = budgetTypes[3];
        } else if (repetition.equals("mensile")) {
            budgetType = budgetTypes[4];
        } else if (repetition.equals("annuale")) {
            budgetType = budgetTypes[5];
        }

        return budgetType;
    }
}
