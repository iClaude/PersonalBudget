/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.oggetti.Budget;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.util.Date;
import java.util.List;

/**
 * Fragment used to display data in the Activity of budget details.
 */
public class BudgetDetailsData extends Fragment {

    // Constants.
    private static final String KEY_ID = "KEY_ID";

    // Variables.
    private Budget budget;
    // Graphic.
    private final int[] tagsColors = {R.color.indigo_500, R.color.red_500, R.color.pink_500, R.color.purple_500, R.color.deep_purple_500, R.color.teal_500, R.color.orange_800, R.color.brown_500, R.color.green_600, R.color.cyan_900};
    // Widgets' references.
    private LinearLayout llTags;
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
        llTags = (LinearLayout) view.findViewById(R.id.llTags);
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
        displayTags();
        //tvTag.setText(budget.getTagWithoutComma());
        tvAmount.setText(UtilityVarious.getFormattedAmount(budget.getExpenses(), getActivity()) + " " + getString(R.string.di) + " " + UtilityVarious.getFormattedAmount(budget.getAmount(), getActivity()));
        tvBudgetType.setText(budget.getBudgetType(getActivity()));
        tvDate.setText(UtilityVarious.getDateFormatShort().format(new Date(budget.getDateEnd())));
        int imageId = budget.getAddRest() == 0 ? R.drawable.cross : R.drawable.check;
        ivAddSavings.setImageResource(imageId);
    }

    /*
        Display tags with colored text views in a linear layout contained in a horizontal scrollview.
     */
    private void displayTags() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        List<String> tags = UtilityVarious.createTagsList(budget.getTag());
        for (int i = 0; i < tags.size(); i++) {
            TextView tvTag = (TextView) inflater.inflate(R.layout.tag_text_view, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.tag_textview_marginright), 0);
            tvTag.setLayoutParams(params);
            // Set the color of the tag dinamically preserving the rounded rectangle shape.
            int tagColor = tagsColors[i % 10];
            GradientDrawable bgShape = (GradientDrawable) tvTag.getBackground();
            bgShape.setColor(ContextCompat.getColor(getActivity(), tagColor));
            tvTag.setText(tags.get(i));
            llTags.addView(tvTag);
        }
    }

    /*
        Called from the BudgetDetails Activity when we have to reload data from the database (
        when the budget has been edited).
    */
    public void reloadData() {
        new GetBudgetDetailsTask().execute(budget.getId());
    }
}
