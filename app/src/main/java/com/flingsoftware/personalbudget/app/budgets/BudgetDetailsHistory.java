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
 * Fragment used to display the history of budgets of the same type of this one.
 */

public class BudgetDetailsHistory extends Fragment {

    private static final String BUDGET_ID = "BUDGET_ID";


    // Add possible variables to pass to the Fragment.
    public static BudgetDetailsHistory newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(BUDGET_ID, id);
        BudgetDetailsHistory budgetDetailsHistory = new BudgetDetailsHistory();
        budgetDetailsHistory.setArguments(args);
        return budgetDetailsHistory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_details_history, container, false);
        return view;
    }
}
