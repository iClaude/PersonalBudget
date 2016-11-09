/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flingsoftware.personalbudget.R;

/**
 * Fragment used to display data in the Activity of budget details.
 */
public class BudgetDetailsData extends Fragment {

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
        return view;
    }
}
