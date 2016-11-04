/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;

/**
 * Created by agost on 04/11/2016.
 */

public class BudgetDetails extends AppCompatActivity {

    // Constants.
    private static final String KEY_ID = "KEY_ID";

    // Variables.
    private TextView tvToolbarTitle;
    private TextView tvTagAppbar;
    private TextView tvAmountAppbar;


    public static Intent makeIntent(Context context, long id) {
        Intent intent = new Intent(context, BudgetDetails.class);
        intent.putExtra(KEY_ID, id);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_details);

        // Get references to layout widgets.
        initLayoutWidgets();
    }

    // Get widgets' references from the inflated layout.
    private void initLayoutWidgets() {
        tvToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        tvTagAppbar = (TextView) findViewById(R.id.tvTagAppbar);
        tvAmountAppbar = (TextView) findViewById(R.id.tvAmountAppbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_budget_dettaglio2, menu);

        return true;
    }
}
