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
 * Fragment used to display the expenses included in the budget displayed in the BudgetDetails
 * Activity.
 */

public class BudgetDetailsExpenses extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_details_expenses, container, false);
        return view;
    }

}
