/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.DBCSpeseSostenute;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.oggetti.Budget;
import com.flingsoftware.personalbudget.oggetti.ExpenseEarning;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Fragment used to display the expenses included in the budget displayed in the BudgetDetails
 * Activity.
 */

public class BudgetDetailsExpenses extends Fragment {

    // Constants.
    private static final String BUDGET_ID = "BUDGET_ID";

    // Variables.
    // Data.
    private long budgetId;
    private List<ExpensesWithTag> list = new ArrayList<>(1);
    private Budget budget;
    // Icons.
    private ListViewIconeVeloce iconeVeloci;
    private Bitmap mPlaceHolderBitmap;
    // Widgets.
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;


    // Add possible variables to pass to the Fragment.
    public static BudgetDetailsExpenses newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(BUDGET_ID, id);
        BudgetDetailsExpenses budgetDetailsExpenses = new BudgetDetailsExpenses();
        budgetDetailsExpenses.setArguments(args);
        return budgetDetailsExpenses;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        budgetId = getArguments().getLong(BUDGET_ID);
        //region Icons
        iconeVeloci = new ListViewIconeVeloce(getActivity());
        new PlaceHolderWorkerTask().execute(R.drawable.tag_0);
        //endregion
        new GetBudgetDetailsTask().execute(budgetId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_details_expenses, container, false);

        // Set up ExpandableRecyclerView.
        recyclerView = (RecyclerView) view.findViewById(R.id.rvExpenses);
        adapter = new MyExpandableRecyclerViewAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    public class MyExpandableRecyclerViewAdapter extends ExpandableRecyclerAdapter<MyExpandableRecyclerViewAdapter.ExpensesWithTagViewHolder, MyExpandableRecyclerViewAdapter.ExpenseViewHolder> {

        private LayoutInflater mInflator;

        public MyExpandableRecyclerViewAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
            super(parentItemList);
            mInflator = LayoutInflater.from(context);
        }

        // onCreate ...
        @Override
        public ExpensesWithTagViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
            View parentView = mInflator.inflate(R.layout.budget_expenses_item_parent, parentViewGroup, false);
            return new ExpensesWithTagViewHolder(parentView);
        }

        @Override
        public ExpenseViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
            View childView = mInflator.inflate(R.layout.budget_expenses_item_child, childViewGroup, false);
            return new ExpenseViewHolder(childView);
        }

        // onBind ...
        @Override
        public void onBindParentViewHolder(ExpensesWithTagViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
            ExpensesWithTag expensesWithTag = (ExpensesWithTag) parentListItem;
            parentViewHolder.bind(expensesWithTag);
        }

        @Override
        public void onBindChildViewHolder(ExpenseViewHolder childViewHolder, int position, Object childListItem) {
            ExpenseEarning expense = (ExpenseEarning) childListItem;
            childViewHolder.bind(expense);
        }

        // ViewHolder.
        // Parent ViewHolder.
        public class ExpensesWithTagViewHolder extends ParentViewHolder {
            private ImageView ivIcon;
            private TextView tvTag;
            private TextView tvTotal;

            public ExpensesWithTagViewHolder(View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
                tvTag = (TextView) itemView.findViewById(R.id.tvTag);
                tvTotal = (TextView) itemView.findViewById(R.id.tvTotal);
            }

            public void bind(ExpensesWithTag expensesWithTag) {
                iconeVeloci.loadBitmap(expensesWithTag.getIconId(), ivIcon, mPlaceHolderBitmap, 40, 40);
                tvTag.setText(expensesWithTag.getTag());
                tvTotal.setText(UtilityVarious.getFormattedAmount(expensesWithTag.getTotal(), getActivity()));
            }
        }

        // Child ViewHolder.
        public class ExpenseViewHolder extends ChildViewHolder {
            private TextView tvDate;
            private TextView tvDesc;
            private TextView tvAccount;
            private TextView tvAmount;

            public ExpenseViewHolder(View view) {
                super(view);
                tvDate = (TextView) view.findViewById(R.id.tvDate);
                tvDesc = (TextView) view.findViewById(R.id.tvDesc);
                tvAccount = (TextView) view.findViewById(R.id.tvAccount);
                tvAmount = (TextView) view.findViewById(R.id.tvAmount);
            }

            public void bind(ExpenseEarning expense) {
                DateFormat dateFormat = UtilityVarious.getDateFormatShort();
                tvDate.setText(dateFormat.format(new Date(expense.getData())));
                tvDesc.setText(expense.getDescrizione());
                tvAccount.setText(expense.getConto());
                tvAmount.setText(UtilityVarious.getFormattedAmount(expense.getImportoValprin(), getActivity()));
            }
        }
    }

    // Load icons placeholder in a separate thread.
    private class PlaceHolderWorkerTask extends AsyncTask<Integer, Void, Object> {
        // Decode image in background.
        @Override
        protected Object doInBackground(Integer... params) {
            mPlaceHolderBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), params[0], 50, 50);
            return null;
        }
    }

    // Retrieve budget details in a separate thread.
    // TODO: 12/12/2016 rivedere con design patterns
    private class GetBudgetDetailsTask extends AsyncTask<Long, Object, Void> {
        private final DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(getActivity());
        private DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(getActivity());
        private List<String> tags = new ArrayList<>(); // list of the tags of this budget

        protected Void doInBackground(Long... params) {
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();

            budget = Budget.makeBudgetFromCursor(cursor, getActivity());
            String tag = budget.getTag();
            cursor.close();
            dbcSpeseBudget.close();

            // Load data for the ExpandableRecyclerView.
            createTagsList(tag);
            loadDataForERV();
            return null;
        }

        protected void onPostExecute(Void result) {

        }

        private void createTagsList(String tag) {
            if (tag.indexOf(',') == -1) {
                tags.add(tag);
            } else {
                StringTokenizer st = new StringTokenizer(tag, ",");
                while (st.hasMoreTokens()) {
                    tags.add(st.nextToken());
                }
            }
        }

        private void loadDataForERV() {
            String queryParent = createQueryForParent();
            dbcSpeseSostenute.openLettura();
            Cursor parentCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpeseXYZTotaliPerDataOVoce(queryParent, null);
            int numParents = parentCursor.getCount();
            ((ArrayList) list).ensureCapacity(numParents);
            while (parentCursor.moveToNext()) {
                String tag = parentCursor.getString(parentCursor.getColumnIndex("voce"));
                double total = parentCursor.getDouble(parentCursor.getColumnIndex("totale_spesa"));
                int iconId = getIconId(tag);
                ExpensesWithTag expenseWithTag = new ExpensesWithTag(iconId, tag, total, null);
                addChildren(expenseWithTag);
                ((ArrayList) list).add(expenseWithTag);
            }
            parentCursor.close();
            dbcSpeseSostenute.close();
        }

        private String createQueryForParent() {
            StringBuilder query = new StringBuilder("SELECT _id, voce, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= " + budget.getDateStart() + " AND data <= " + budget.getDateEnd() + " AND (");
            int size = tags.size();
            if (size == 1) {
                query.append("voce = '" + tags.get(0) + "') GROUP BY voce ORDER BY voce ASC");
            } else {
                query.append("voce = '" + tags.get(0) + "'");
                for (int i = 1; i < (size - 1); i++) {
                    query.append(" OR voce = '" + tags.get(i) + "'");
                }
                query.append(" GROUP BY voce ORDER BY voce ASC");
            }

            return query.toString();
        }

        private int getIconId(String tag) {
            DBCSpeseVoci dbcSpeseVoci = new DBCSpeseVoci(getActivity());
            dbcSpeseVoci.openLettura();
            Cursor curTag = dbcSpeseVoci.getVociContenentiStringa(tag);
            curTag.moveToFirst();
            int iconId = curTag.getInt(curTag.getColumnIndex("icona"));
            curTag.close();
            dbcSpeseVoci.close();

            return iconId;
        }

        private void addChildren(ExpensesWithTag expenseWithTag) {
            Cursor childCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXBudgetSpeseIncluse(budget.getDateStart(), budget.getDateEnd(), expenseWithTag.getTag());
            int numChildren = childCursor.getCount();
            ArrayList<ExpenseEarning> expenses = new ArrayList<>(numChildren);
            while (childCursor.moveToNext()) {
                ExpenseEarning expense = ExpenseEarning.makeExpenseEarning(childCursor);
                expenses.add(expense);
            }
            expenseWithTag.setExpenses(expenses);
            childCursor.close();
        }
    }
}
