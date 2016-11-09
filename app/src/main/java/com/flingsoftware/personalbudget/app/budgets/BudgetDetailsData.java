/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment used to display data in the Activity of budget details.
 */
public class BudgetDetailsData extends Fragment {

    // Variables.
    // Widgets' references.
    private TextView tvTag;
    private TextView tvAmount;
    private TextView tvBudgetType;
    private TextView tvDate;
    private ImageView ivAddSavings;


    // Add possible variables to pass to the Fragment.
    public static BudgetDetailsData newInstance() {
        Bundle args = new Bundle();
        BudgetDetailsData budgetDetailsData = new BudgetDetailsData();
        budgetDetailsData.setArguments(args);
        return budgetDetailsData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_details_data, container, false);

        // Get references of widgets.
        tvTag = (TextView) view.findViewById(R.id.tvTag);
        // Running text
        tvTag.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvTag.setSingleLine(true);
        tvTag.setMarqueeRepeatLimit(5);
        tvTag.setSelected(true);
        tvAmount = (TextView) view.findViewById(R.id.tvAmount);
        tvBudgetType = (TextView) view.findViewById(R.id.tvBudgetType);
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        ivAddSavings = (ImageView) view.findViewById(R.id.ivAddSavings);

        return view;
    }

    /*
        When the Activity has retrieved the budget details (in a separate thread) display them
        in this Fragment.
     */
    public void displayData(String tag, double amount, String budgetType, long date, int addSavings) {
        // Some useful formatters.
        final Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);
        final DateFormat dfDate = DateFormat.getDateInstance(DateFormat.SHORT, miaLocale);

        // Now display data.
        tvTag.setText(tag);
        tvAmount.setText(UtilityVarious.getFormattedAmount(amount, getActivity()));
        tvBudgetType.setText(budgetType);
        tvDate.setText(dfDate.format(new Date(date)));
        int imageId = addSavings == 0 ? R.drawable.cross : R.drawable.check;
        ivAddSavings.setImageResource(imageId);
    }
}
