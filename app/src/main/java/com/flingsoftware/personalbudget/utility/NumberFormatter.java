/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;

import com.flingsoftware.personalbudget.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import static com.flingsoftware.personalbudget.utility.UtilityVarious.getPrefCurrency;

/**
 * This is a utility class containing public static functions used to display numbers
 * in different formats.
 */

public class NumberFormatter {

    /**
     * Given an amount returns a formatted String using the default Locale and the
     * main currency saved in the preferences.
     *
     * @param amount  the number to forma
     * @param context a Context object
     * @return the amount formatted using the default Locale and the mani currency saved
     * in the preferences
     */
    public static String formatAmountMainCurrency(double amount, Context context) {
        // Get the main currency from the preferences.
        Currency prefCurrency = getPrefCurrency(context);
        // Create a formatter using the default Locale and the preferred currency.
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(prefCurrency);

        return nf.format(amount);
    }

    /**
     * Formats a number with 2 decimal digits and minus sign for negative numbers.
     *
     * @param amount the amount to format
     * @return the amount formatted
     */
    public static String formatAmountNoCurrency(double amount) {
        // Create a formatter using no decimal digits.
        DecimalFormat df = new DecimalFormat("#,##0.00; - #,##0.00");
        df.setMaximumFractionDigits(2);

        return df.format(amount);
    }

    /**
     * Formats a given amount using this styles:
     * - no decimal digits
     * - plus or minus sign
     * - green color for positive numbers, red for negative ones
     * - number is bold
     * - the main currency (from preferences) is superscript at the end
     *
     * @param amount  amout to format
     * @param context a Context object
     * @return the amount formatted
     */
    public static Spannable formatAmountColorCurrencySuperscript(double amount, Context context) {
        int color = amount > 0 ? R.color.green_dark : R.color.red_dark;
        // Get the main currency from the preferences.
        Currency prefCurrency = getPrefCurrency(context);
        // Create a formatter using no decimal digits.
        DecimalFormat df = new DecimalFormat("+ #,##0; - #,##0");
        df.setMaximumFractionDigits(0);

        String currSymbol = prefCurrency.getSymbol().substring(0, 1);
        String amountStr = df.format(amount) + currSymbol;

        Spannable spannable = new SpannableString(amountStr);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, color)), 0, spannable.length(), 0);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length() - 1, 0);
        spannable.setSpan(new SuperscriptSpan(), spannable.length() - currSymbol.length(), spannable.length(), 0);
        spannable.setSpan(new RelativeSizeSpan(0.9f), spannable.length() - currSymbol.length(), spannable.length(), 0);

        return spannable;
    }

    /**
     * Formats a given amount using this styles:
     * - 2 decimal digits
     * - plus or minus sign
     * - green color for positive numbers, red for negative ones
     * - no currency symbol (just the number)
     *
     * @param amount  amout to format
     * @param context a Context object
     * @return the amount formatted
     */
    public static Spannable formatAmountColorNoCurrency(double amount, Context context) {
        int color = amount > 0 ? R.color.green_dark : R.color.red_dark;
        // Create a formatter using no decimal digits.
        DecimalFormat df = new DecimalFormat("+ #,##0.00; - #,##0.00");
        df.setMaximumFractionDigits(0);
        String amountStr = df.format(amount);

        Spannable spannable = new SpannableString(amountStr);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, color)), 0, spannable.length(), 0);

        return spannable;
    }
}
