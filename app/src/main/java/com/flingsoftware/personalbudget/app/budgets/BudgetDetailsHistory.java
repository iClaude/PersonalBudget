/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;

/**
 * Fragment used to display the history of budgets of the same type of this one.
 */

public class BudgetDetailsHistory extends Fragment {

    // Constants.
    private static final String BUDGET_ID = "BUDGET_ID";

    // Variables.
    private long budgetId;
    // Widgets and layout.
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;


    // Add possible variables to pass to the Fragment.
    public static BudgetDetailsHistory newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(BUDGET_ID, id);
        BudgetDetailsHistory budgetDetailsHistory = new BudgetDetailsHistory();
        budgetDetailsHistory.setArguments(args);
        return budgetDetailsHistory;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        budgetId = getArguments().getLong(BUDGET_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_details_history, container, false);

        // Set up RecyclerView.
        recyclerView = (RecyclerView) view.findViewById(R.id.rvBudgets);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);


        return view;
    }

    // Retrieve the history of budgets of the same type, passing the id of this budget.
    private class GetBudgetHistoryTask extends AsyncTask<Long, Object, Cursor> {

        protected Cursor doInBackground(Long... params) {
            // Get the id of the original budget.
            DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(getActivity());
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();
            long startBudget = cursor.getLong(cursor.getColumnIndex("budget_iniziale"));
            dbcSpeseBudget.close();

            // Get a cursor containing the history of budgets of the same type.
            cursor = dbcSpeseBudget.getSpeseBudgetElencoBudgetAnaloghi(startBudget);

            return cursor;
        }

        protected void onPostExecute(Cursor cursor) {
            // do something with the cursor
        }
    }

    // Adapter used by the RecyclerView to get its data.
    private class BudgetsAdapter extends RecyclerView.Adapter<BudgetsAdapter.BudgetsViewHolder> {

        // ViewHolder.
        private class BudgetsViewHolder extends RecyclerView.ViewHolder {
            private TextView tvPeriod;
            private TextView tvTag;
            private TextView tvSaved;
            private TextView tvAmount;
            private TextView tvSpent;
            private TextView tvBudgetType;
            private TextView tvEndDate;

            public BudgetsViewHolder(View view) {
                super(view);
                tvPeriod = (TextView) view.findViewById(R.id.tvPeriod);
                tvTag = (TextView) view.findViewById(R.id.tvTag);
                tvSaved = (TextView) view.findViewById(R.id.tvSaved);
                tvAmount = (TextView) view.findViewById(R.id.tvAmount);
                tvSpent = (TextView) view.findViewById(R.id.tvSpent);
                tvBudgetType = (TextView) view.findViewById(R.id.tvBudgetType);
                tvEndDate = (TextView) view.findViewById(R.id.tvEndDate);
            }
        }

        // BudgetsAdapter class.


        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public BudgetsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_history_item, parent, false);
            return new BudgetsViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(BudgetsViewHolder holder, int position) {

        }
    }
}
