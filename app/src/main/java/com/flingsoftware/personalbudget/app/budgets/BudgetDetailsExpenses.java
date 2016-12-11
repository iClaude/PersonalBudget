/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.content.Context;
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
import com.flingsoftware.personalbudget.oggetti.ExpenseEarning;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}
