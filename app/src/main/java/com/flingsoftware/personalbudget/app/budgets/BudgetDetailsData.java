/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.oggetti.Budget;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.util.Date;

/**
 * Fragment used to display data in the Activity of budget details.
 */
public class BudgetDetailsData extends Fragment {

    // Constants.
    private static final String KEY_ID = "KEY_ID";

    // Variables.
    private Budget budget;
    // Widgets' references.
    private TextView tvTag;
    private TextView tvAmount;
    private TextView tvBudgetType;
    private TextView tvDate;
    private ImageView ivAddSavings;


    // Add possible variables to pass to the Fragment.
    public static BudgetDetailsData newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(KEY_ID, id);
        BudgetDetailsData budgetDetailsData = new BudgetDetailsData();
        budgetDetailsData.setArguments(args);
        return budgetDetailsData;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_data, container, false);

        long id = getArguments().getLong(KEY_ID);
        // Get references of widgets.
        tvTag = (TextView) view.findViewById(R.id.tvTag);
        tvAmount = (TextView) view.findViewById(R.id.tvAmount);
        tvBudgetType = (TextView) view.findViewById(R.id.tvBudgetType);
        tvDate = (TextView) view.findViewById(R.id.tvDate);
        ivAddSavings = (ImageView) view.findViewById(R.id.ivAddSavings);

        new GetBudgetDetailsTask().execute(id);

        return view;
    }

    // Retrieve budget details in a separate thread.
    private class GetBudgetDetailsTask extends AsyncTask<Long, Object, Void> {
        final DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(getActivity());

        protected Void doInBackground(Long... params) {
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();

            budget = Budget.makeBudgetFromCursor(cursor, getActivity());

            cursor.close();
            dbcSpeseBudget.close();
            return null;
        }

        protected void onPostExecute(Void result) {
            displayData();
        }
    }

    /*
        When the Activity has retrieved the budget details (in a separate thread) display them
        in this Fragment.
     */
    private void displayData() {
        // Now display data.
        tvTag.setText(budget.getTagWithoutComma());
        tvAmount.setText(UtilityVarious.getFormattedAmount(budget.getExpenses(), getActivity()) + " " + getString(R.string.di) + " " + UtilityVarious.getFormattedAmount(budget.getAmount(), getActivity()));
        tvBudgetType.setText(budget.getBudgetType(getActivity()));
        tvDate.setText(UtilityVarious.getDateFormatShort().format(new Date(budget.getDateEnd())));
        int imageId = budget.getAddRest() == 0 ? R.drawable.cross : R.drawable.check;
        ivAddSavings.setImageResource(imageId);
    }
}
