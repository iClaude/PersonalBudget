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

    private static final String BUDGET_ID = "BUDGET_ID";


    // Add possible variables to pass to the Fragment.
    public static BudgetDetailsExpenses newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(BUDGET_ID, id);
        BudgetDetailsExpenses budgetDetailsExpenses = new BudgetDetailsExpenses();
        budgetDetailsExpenses.setArguments(args);
        return budgetDetailsExpenses;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_details_expenses, container, false);
        return view;
    }

}
