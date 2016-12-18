/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

/**
 * Fragment used to display the expenses included in the budget displayed in the BudgetDetails
 * Activity.
 */

public class BudgetDetailsExpenses extends Fragment {

    // Constants.
    private static final String TAG = "BudgetDetailsExpenses";
    private static final String BUDGET_ID = "BUDGET_ID";

    private final List<ExpensesWithTag> list = new ArrayList<>();
    private Budget budget;
    // Icons.
    private ListViewIconeVeloce iconeVeloci;
    private Bitmap mPlaceHolderBitmap;
    // Widgets.
    private RecyclerView recyclerView;
    private TextView tvLabel;


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

        long budgetId = getArguments().getLong(BUDGET_ID);
        //region Icons
        iconeVeloci = new ListViewIconeVeloce(getActivity());
        new PlaceHolderWorkerTask().execute(R.drawable.tag_0);
        //endregion
        new GetExpensesTask().execute(budgetId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.budget_expenses, container, false);

        tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        // Set up ExpandableRecyclerView.
        recyclerView = (RecyclerView) view.findViewById(R.id.rvExpenses);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        return view;
    }

    public class MyExpandableRecyclerViewAdapter extends ExpandableRecyclerAdapter<MyExpandableRecyclerViewAdapter.ExpensesWithTagViewHolder, MyExpandableRecyclerViewAdapter.ExpenseViewHolder> {

        private final LayoutInflater mInflator;

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
            private final ImageView ivIcon;
            private final TextView tvTag;
            private final TextView tvTotal;
            private final TextView tvCount;
            private final TextView tvStat;
            private ImageButton ibExpand;

            public ExpensesWithTagViewHolder(View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
                tvTag = (TextView) itemView.findViewById(R.id.tvTag);
                tvTotal = (TextView) itemView.findViewById(R.id.tvTotal);
                tvCount = (TextView) itemView.findViewById(R.id.tvCount);
                tvStat = (TextView) itemView.findViewById(R.id.tvStat);
                ibExpand = (ImageButton) itemView.findViewById(R.id.ibExpand);

                ibExpand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isExpanded()) {
                            collapseView();
                        } else {
                            expandView();
                        }
                    }
                });
            }

            public void bind(ExpensesWithTag expensesWithTag) {
                // Diplay details of expenses grouped by tag.
                iconeVeloci.loadBitmap(expensesWithTag.getIconId(), ivIcon, mPlaceHolderBitmap, 40, 40);
                tvTag.setText(expensesWithTag.getTag());
                tvTotal.setText(UtilityVarious.getFormattedAmount(expensesWithTag.getTotal(), getActivity()));
                tvCount.setText(getResources().getQuantityString(R.plurals.budgets_num_expenses, expensesWithTag.getNumExpenses(), expensesWithTag.getNumExpenses()));
                tvStat.setText(getString(R.string.budgets_maxmin, UtilityVarious.getFormattedAmount(expensesWithTag.getMaxExpense(), getActivity()), UtilityVarious.getFormattedAmount(expensesWithTag.getMinExpense(), getActivity())));
                // Set correct rotation of ibExpand according to the expanded state.
                float rotation = isExpanded() ? 180.0f : 0.0f;
                ibExpand.setRotation(rotation);
            }

            @Override
            public boolean shouldItemViewClickToggleExpansion() {
                return false;
            }

            @Override
            public void onExpansionToggled(boolean expanded) {
                super.onExpansionToggled(expanded);

                // Rotate the arrow (ImageButton) with an animation.
                float startAngle = expanded ? 180.0f : 0.0f;
                float endAngle = expanded ? 360.0f : 180.0f;
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ibExpand, "rotation", startAngle, endAngle);
                objectAnimator.setDuration(300);
                objectAnimator.start();
            }
        }

        // Child ViewHolder.
        public class ExpenseViewHolder extends ChildViewHolder {
            private final TextView tvDate;
            private final TextView tvDesc;
            private final TextView tvAccount;
            private final TextView tvAmount;

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

    // Retrieve budget expenses in a separate thread.
    private class GetExpensesTask extends AsyncTask<Long, Object, Void> {
        private final DBCSpeseSostenute dbcSpeseSostenute = new DBCSpeseSostenute(getActivity());
        private List<String> tags; // list of the tags of this budget

        protected Void doInBackground(Long... params) {
            final DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(getActivity());
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();
            budget = Budget.makeBudgetFromCursor(cursor, getActivity());
            String tag = budget.getTag();
            cursor.close();
            dbcSpeseBudget.close();

            // Load data for the ExpandableRecyclerView.
            tags = UtilityVarious.createTagsList(tag);
            loadDataForERV();

            return null;
        }

        protected void onPostExecute(Void result) {
            if (list.size() == 0) {
                tvLabel.setText(getString(R.string.budgets_no_expenses));
            } else {
                tvLabel.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            RecyclerView.Adapter adapter = new MyExpandableRecyclerViewAdapter(getActivity(), list);
            recyclerView.setAdapter(adapter);
        }

        private void loadDataForERV() {
            String queryParent = createQueryForParent();
            dbcSpeseSostenute.openLettura();
            Cursor parentCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpeseXYZTotaliPerDataOVoce(queryParent, null);
            ((ArrayList) list).ensureCapacity(parentCursor.getCount());
            while (parentCursor.moveToNext()) {
                String tag = parentCursor.getString(parentCursor.getColumnIndex("voce"));
                double total = parentCursor.getDouble(parentCursor.getColumnIndex("totale_spesa"));
                int iconId = getIconId(tag);
                ExpensesWithTag expenseWithTag = new ExpensesWithTag(iconId, tag, total, null);
                expenseWithTag.setExpenses(getExpensesWithTag(tag, budget.getDateStart(), budget.getDateEnd()));
                list.add(expenseWithTag);
            }
            parentCursor.close();
            dbcSpeseSostenute.close();
        }

        private String createQueryForParent() {
            StringBuilder query = new StringBuilder("SELECT _id, voce, SUM(importo_valprin) AS totale_spesa FROM spese_sost WHERE data >= " + budget.getDateStart() + " AND data <= " + budget.getDateEnd() + " AND (");
            int size = tags.size();
            if (size == 1) {
                query.append("voce = '").append(tags.get(0)).append("') GROUP BY voce ORDER BY voce ASC");
            } else {
                query.append("voce = '").append(tags.get(0)).append("'");
                for (int i = 1; i < size; i++) {
                    query.
                            append(" OR voce = '").append(tags.get(i)).append("'");
                }
                query.append(") GROUP BY voce ORDER BY voce ASC");
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

        // Get a list of expenses with a given tag between two dates.
        private List<ExpenseEarning> getExpensesWithTag(String tag, long dateStart, long dateEnd) {
            Cursor childCursor = dbcSpeseSostenute.getSpeseSostenuteIntervalloSpesaXBudgetSpeseIncluse(dateStart, dateEnd, tag);
            ArrayList<ExpenseEarning> expenses = new ArrayList<>(childCursor.getCount());
            while (childCursor.moveToNext()) {
                ExpenseEarning expense = ExpenseEarning.makeExpenseEarning(childCursor);
                expenses.add(expense);
            }
            childCursor.close();

            return expenses;
        }
    }
}
