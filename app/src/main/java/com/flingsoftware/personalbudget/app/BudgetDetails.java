/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCVociAbs;
import com.flingsoftware.personalbudget.utilita.BlurBuilder;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by agost on 04/11/2016.
 */

public class BudgetDetails extends AppCompatActivity {

    // Constants.
    private static final String TAG = "BudgetDetails";
    private static final String KEY_ID = "KEY_ID";

    // Variables.
    // Widgets.
    private TextView tvToolbarTitle;
    private TextView tvTagAppbar;
    private TextView tvAmountAppbar;
    private TextView tvTagToolbar;
    private TextView tvAmountToolbar;
    // Budget details.
    private long id;
    private String tag;
    private double amount;
    private String repetition;
    private double expenses;
    // Graphics and multimedia.
    private SoundEffectsManager soundEffectsManager;


    // Factory method to create an Intent to start this Activity.
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

        // App bar.
        setUpAppbar();

        // Get expense/earning details and display them.
        getDetails();

        // Sound effects.
        soundEffectsManager = SoundEffectsManager.getInstance();
    }

    // Get widgets' references from the inflated layout.
    private void initLayoutWidgets() {
        tvToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        tvTagAppbar = (TextView) findViewById(R.id.tvTagAppbar);
        tvAmountAppbar = (TextView) findViewById(R.id.tvAmountAppbar);
        tvTagToolbar = (TextView) findViewById(R.id.toolbar_title);
        tvAmountToolbar = (TextView) findViewById(R.id.toolbar_subtitle);
    }

    // Set up app bar layout and behavior.
    private void setUpAppbar() {
        // Toolbar for options menu.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setContentInsetsAbsolute(0, 0); // left margin of toolbar title

        // Listener for appbar expanded/collapsed to show the title correctly.
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener());
    }

    // Get budget details using the id stored in the Intent that launched this Activity.
    private void getDetails() {
        Bundle extras = getIntent().getExtras();
        id = extras.getLong(KEY_ID);
        new GetBudgetDetailsTask().execute(id);
    }

    // Retrieve budget details in a separate thread.
    private class GetBudgetDetailsTask extends AsyncTask<Long, Object, Void> {
        DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDetails.this);

        protected Void doInBackground(Long... params) {
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();

            // Budget details.
            tag = cursor.getString(cursor.getColumnIndex("voce"));
            if (tag.endsWith(",")) {
                tag = tag.substring(0, tag.length() - 1);
            }
            amount = cursor.getDouble(cursor.getColumnIndex("importo_valprin"));
            String repTmp;
            repTmp = cursor.getString(cursor.getColumnIndex("ripetizione"));
            repetition = UtilityVarious.getBudgetType(BudgetDetails.this, repTmp);
            expenses = cursor.getDouble(cursor.getColumnIndex("spesa_sost"));

            cursor.close();
            dbcSpeseBudget.close();
            return null;
        }

        protected void onPostExecute(Void result) {
            displayDetails();
        }
    }

    // Display budget details in the layout.
    private void displayDetails() {
        // Appbar.
        tvTagToolbar.setText(tag);
        tvTagAppbar.setText(tag);
        // Running text.
        tvTagAppbar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvTagAppbar.setSingleLine(true);
        tvTagAppbar.setMarqueeRepeatLimit(5);
        tvTagAppbar.setSelected(true);
        // Amount formatted in main currency.
        Currency prefCurrency = UtilityVarious.getPrefCurrency(this);
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(prefCurrency);
        String amountFormatted = nf.format(amount);
        tvAmountToolbar.setText(amountFormatted + " (" + repetition + ")");
        tvAmountAppbar.setText(amountFormatted);
        // Load main image in a separate thread.
        new LoadHeaderImageTask().execute(tag);
    }

    // Load header image in a separate thread.
    private class LoadHeaderImageTask extends AsyncTask<String, Object, Bitmap> {
        int backgroundColor = R.color.primary_light;
        DBCVociAbs dbcVociAbs = new DBCSpeseVoci(BudgetDetails.this);

        protected Bitmap doInBackground(String... params) {
            // Get the icon id from the tag.
            dbcVociAbs.openLettura();
            Cursor curVoci = dbcVociAbs.getTutteLeVociFiltrato(params[0]);

            int iconaId = R.drawable.tag_1;
            if (curVoci.moveToFirst()) {
                int icona = curVoci.getInt(curVoci.getColumnIndex("icona"));
                iconaId = ListViewIconeVeloce.arrIconeId[icona];
            }
            final int iconaIdDef = iconaId;

            curVoci.close();
            dbcVociAbs.close();

            // Create a blurred image representing the icon.
            Bitmap origBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaIdDef, 128, 128);
            Bitmap blurredBitmap = BlurBuilder.blur(BudgetDetails.this, origBitmap);
            // Get a suitable color for image background.
            Palette palette = Palette.from(origBitmap).generate();
            backgroundColor = palette.getMutedColor(ContextCompat.getColor(BudgetDetails.this, R.color.primary_light));

            return blurredBitmap;
        }

        protected void onPostExecute(Bitmap myBitmap) {
            ImageView ivHeader = (ImageView) findViewById(R.id.appbar_image2);
            ivHeader.setImageBitmap(myBitmap);
            ivHeader.setBackgroundColor(Color.rgb(Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)));

            // Show the header image with a fadein-fadeout animation.
            ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.vsHeader);
            viewSwitcher.showNext();
        }
    }

    /*
    Used to detect when the app bar is collapsed or expanded, in order to show the Toolbar
    title accordingly.
*/
    private class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            int maxScroll = appBarLayout.getTotalScrollRange();
            float percentage = (float) Math.abs(i) / (float) maxScroll;
            tvTagToolbar.setAlpha(percentage);
            tvAmountToolbar.setAlpha(percentage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_budget_dettaglio2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
