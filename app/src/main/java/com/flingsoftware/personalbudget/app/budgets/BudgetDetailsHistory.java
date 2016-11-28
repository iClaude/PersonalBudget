/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.oggetti.Budget;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment used to display the history of budgets of the same type of this one.
 */

public class BudgetDetailsHistory extends Fragment {

    // Constants.
    private static final String BUDGET_ID = "BUDGET_ID";

    // Variables.
    private long budgetId;
    // Icons (legacy approach).
    private ListViewIconeVeloce iconeVeloci;
    private Bitmap redPig;
    private Bitmap greenPig;

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

        // Get the budgets' history.
        new GetBudgetHistoryTask().execute(budgetId);

        // Set up RecyclerView.
        recyclerView = (RecyclerView) view.findViewById(R.id.rvBudgets);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BudgetAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Retrieve the history of budgets of the same type, passing the id of this budget.
    private class GetBudgetHistoryTask extends AsyncTask<Long, Object, Void> {
        private List<Budget> listBudget = new ArrayList<>(10);

        protected Void doInBackground(Long... params) {
            // Get the history of budgets.
            // Get the id of the original budget.
            DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(getActivity());
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();
            long startBudget = cursor.getLong(cursor.getColumnIndex("budget_iniziale"));
            // Get a cursor containing the history of budgets of the same type.
            cursor = dbcSpeseBudget.getSpeseBudgetElencoBudgetAnaloghi(startBudget);
            // Convert the cursor in a List.
            while (cursor.moveToNext()) {
                listBudget.add(Budget.makeBudgetFromCursor(cursor, getActivity()));
            }
            cursor.close();
            dbcSpeseBudget.close();

            // Get bitmaps for budgets (red pig or green pig).
            redPig = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), R.drawable.img_maiale_rosso, 256, 256);
            greenPig = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), R.drawable.img_maiale_verde, 256, 256);

            return null;
        }

        protected void onPostExecute(Void nothing) {
            ((BudgetAdapter) adapter).setList(listBudget);
        }
    }

    // Adapter used by the RecyclerView to get its data.
    public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

        // ViewHolder.
        public class BudgetViewHolder extends RecyclerView.ViewHolder {
            private TextView tvPeriod;
            private TextView tvTag;
            private TextView tvSaved;
            private ProgressBar pbBudget;
            private TextView tvAmount;
            private TextView tvSpent;
            private TextView tvBudgetType;
            private TextView tvEndDate;
            private ImageView ivIcon;
            private TextView tvShowHide;
            private ImageButton ibShowHide;
            private View vExpandedContent;

            public BudgetViewHolder(View view) {
                super(view);
                tvPeriod = (TextView) view.findViewById(R.id.tvPeriod);
                tvTag = (TextView) view.findViewById(R.id.tvTag);
                tvSaved = (TextView) view.findViewById(R.id.tvSaved);
                pbBudget = (ProgressBar) view.findViewById(R.id.pbBudget);
                tvAmount = (TextView) view.findViewById(R.id.tvAmount);
                tvSpent = (TextView) view.findViewById(R.id.tvSpent);
                tvBudgetType = (TextView) view.findViewById(R.id.tvBudgetType);
                tvEndDate = (TextView) view.findViewById(R.id.tvEndDate);
                tvShowHide = (TextView) view.findViewById(R.id.tvShowHide);
                vExpandedContent = view.findViewById(R.id.data_expanded);
                vExpandedContent.setVisibility(View.GONE);
                ivIcon = (ImageView) view.findViewById(R.id.ivIcon);

                ibShowHide = (ImageButton) view.findViewById(R.id.ibShowHide);
                ibShowHide.setOnClickListener(new View.OnClickListener() {
                                                  boolean collapsed = true;

                                                  @Override
                                                  public void onClick(View view) {
                                                      if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                          TransitionManager.beginDelayedTransition(recyclerView);
                                                      }
                                                      if (collapsed) {
                                                          ibShowHide.setImageResource(R.drawable.ic_navigation_collapse);
                                                          tvShowHide.setText(getString(R.string.budgets_hide));
                                                          vExpandedContent.setVisibility(View.VISIBLE);
                                                          collapsed = false;
                                                      } else {
                                                          ibShowHide.setImageResource(R.drawable.ic_navigation_expand);
                                                          tvShowHide.setText(getString(R.string.budgets_show));
                                                          vExpandedContent.setVisibility(View.GONE);
                                                          collapsed = true;
                                                      }
                                                      //notifyDataSetChanged();
                                                  }
                                              }
                );
            }
        }

        // BudgetsAdapter class.
        private List<Budget> listBudget = new ArrayList<>();

        public BudgetAdapter() {
        }


        public void setList(List<Budget> listBudget) {
            this.listBudget = listBudget;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return listBudget.size();
        }

        @Override
        public BudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_history_item, parent, false);
            return new BudgetViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(BudgetViewHolder holder, int position) {
            Budget budget = listBudget.get(position);

            if (budget != null) {
                Context context = getActivity();
                // Show budget's details in the layout.
                DateFormat dateFormat = UtilityVarious.getDateFormatShort();
                String period = dateFormat.format
                        (new Date(budget.getDateStart())) + " - " + dateFormat.format(new Date(budget.getDateEnd()));
                holder.tvPeriod.setText(period);
                holder.tvTag.setText(budget.getTag());
                double saved = budget.getAmount() - budget.getExpenses();
                if (saved >= 0) {
                    holder.tvSaved.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    holder.tvSaved.setText("+ " + UtilityVarious.getFormattedAmount(saved, context));
                    holder.ivIcon.setImageBitmap(greenPig);
                } else {
                    holder.tvSaved.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    holder.tvSaved.setText(UtilityVarious.getFormattedAmount(saved, context));
                    holder.ivIcon.setImageBitmap(redPig);
                }

                int perc = (int) ((budget.getExpenses() * 100) / budget.getAmount());
                holder.pbBudget.setProgress(perc);
                if (perc >= 100) {
                    holder.pbBudget.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progressbar_accent));
                } else {
                    holder.pbBudget.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progressbar_standard));
                }
                holder.tvAmount.setText(UtilityVarious.getFormattedAmount(budget.getAmount(), context));
                holder.tvSpent.setText(UtilityVarious.getFormattedAmount(budget.getExpenses(), context));
                holder.tvBudgetType.setText(budget.getBudgetType(context));
                holder.tvEndDate.setText(dateFormat.format(new Date(budget.getDateEnd())));
            }
        }
    }
}
