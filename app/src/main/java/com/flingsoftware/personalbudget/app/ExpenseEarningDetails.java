/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.utility.AvatarImageBehavior;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Base class for displaying expense or earning details.
 * Must be extended to show an expense or earning.
 * This class contains common code used by both.
 */

public abstract class ExpenseEarningDetails extends AppCompatActivity implements SpeseEntrateEliminaVociRipetute.EliminaVociRipetuteListener {

    // Constants used by Intent(s).
    protected final String KEY_ID = "KEY_ID";
    protected final String KEY_AMOUNT = "KEY_AMOUNT";
    protected final String KEY_TAG = "KEY_TAG";
    protected final String KEY_DATE = "KEY_DATE";
    protected final String KEY_DESC = "KEY_DESC";
    protected final String KEY_REP_ID = "KEY_REP_ID"; //repetition id
    protected final String KEY_AMOUNT_CURR = "KEY_AMOUNT_CURR"; // amount in original currency
    protected final String KEY_CURRENCY = "KEY_CURRENCY";
    protected final String KEY_ACCOUNT = "KEY_ACCOUNT";
    protected final String KEY_FAVORITE = "KEY_FAVORITE";

    // Constants related to animations.
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.8f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    // Other constants.
    private final Locale miaLocale = (Locale.getDefault().getDisplayLanguage().equals("italiano") ? Locale.getDefault() : Locale.UK);


    // Instance variables.
    // Layout widgets.
    protected TextView tvVoce;
    protected TextView tvImporto;
    protected TextView tvConto;
    protected TextView tvData;
    protected TextView tvDescrizione;
    protected TextView tvRipetizione;
    protected TextView tvFineRipetizione;
    protected ImageView ivIcona;
    protected ImageView ivIconToolbar;
    protected FloatingActionButton fabAlto;
    protected TextView tvToolbarTitle;
    protected LinearLayout llExpandedTitle;

    // Details of this expense/earning.
    private long id;
    private long ripetizione_id;
    private double importo;
    private String conto;
    private int preferito;
    private String tag;
    private long data;
    private String descrizione;
    private long dataFine;
    private long dataInizio;
    private String valuta;
    private double importoValprin;
    protected SoundEffectsManager soundEffectsManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spese_entrate_dettaglio_voce);

        // App bar.
        setUpAppbar();

        // Get widgets' references.
        initLayoutWidgets();

        // Get expense/earning details and display them.
        getDetails();
        displayDetails();

        // Sound effects.
        setupSoundEffects();
    }


    // Set up app bar layout and behavior.
    private void setUpAppbar() {
        // Toolbar for options menu.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Listener for appbar expanded/collapsed to show the title correctly.
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener());

        // Set height of icon in the toolbar to 70% of action bar height.
        int ivIconToolbarSize = (int) (UtilityVarious.getActionBarHeight(this) * 0.7);
        ivIconToolbar.getLayoutParams().width = ivIconToolbarSize;
        ivIconToolbar.getLayoutParams().height = ivIconToolbarSize;
        ivIconToolbar.requestLayout();

        // Set custom layout behavior on ImageView ivIcona.
        AvatarImageBehavior avatarImageBehavior = new AvatarImageBehavior(this);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) ivIcona.getLayoutParams();
        params.setBehavior(avatarImageBehavior);
    }


    /*
        Used to detect when the app bar is collapsed or expanded, in order to show the FAB and the
        title accordingly.
    */
    private class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {
        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            int maxScroll = appBarLayout.getTotalScrollRange();
            float percentage = (float) Math.abs(i) / (float) maxScroll;
            handleToolbarTitleVisibility(percentage);
            handleExpandedTitleVisibility(percentage);
        }
    }


    // Generic alpha animation from 0 to 1 and viceversa, depending on wether the View is or not visible.
    private void startAlphaAnimation(View view, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }


    // Show title on the Toolbar when the app bar is collapsed and viceversa.
    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage == 1) {
            ivIconToolbar.setVisibility(View.VISIBLE);
        } else if (ivIconToolbar.getVisibility() == View.VISIBLE) {
            ivIconToolbar.setVisibility(View.INVISIBLE);
        }

        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (ivIconToolbar.getVisibility() == View.INVISIBLE) {
                tvToolbarTitle.setVisibility(View.VISIBLE);
                startAlphaAnimation(tvToolbarTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
            }
        } else {
            if (ivIconToolbar.getVisibility() == View.VISIBLE) {
                tvToolbarTitle.setVisibility(View.INVISIBLE);
                startAlphaAnimation(tvToolbarTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
            }
        }
    }


    // Hide/show title in expanded app bar.
    private void handleExpandedTitleVisibility(float percentage) {
        if (percentage <= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (llExpandedTitle.getAlpha() == 0) {
                startAlphaAnimation(llExpandedTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
            }
        } else {
            if (llExpandedTitle.getAlpha() == 1) {
                startAlphaAnimation(llExpandedTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
            }
        }
    }


    // Get widgets' references from the inflated layout.
    private void initLayoutWidgets() {
        tvVoce = (TextView) findViewById(R.id.tvVoce);
        tvImporto = (TextView) findViewById(R.id.tvImporto);
        tvConto = (TextView) findViewById(R.id.sedv_tvConto);
        tvData = (TextView) findViewById(R.id.tvData);
        tvDescrizione = (TextView) findViewById(R.id.tvDescrizione);
        tvRipetizione = (TextView) findViewById(R.id.dettagli_voce_tvRipetizione);
        tvFineRipetizione = (TextView) findViewById(R.id.dettagli_voce_tvFineRipetizione);
        ivIcona = (ImageView) findViewById(R.id.spese_entrate_dettaglio_voce_ivIcona);
        ivIconToolbar = (ImageView) findViewById(R.id.ivIconToolbar);
        fabAlto = (FloatingActionButton) findViewById(R.id.fab);
        tvToolbarTitle = (TextView) findViewById(R.id.main_textview_title);
        llExpandedTitle = (LinearLayout) findViewById(R.id.main_linearlayout_title);
    }


    // Get expense/earning details from the Intent that launched this Activity.
    private void getDetails() {
        Bundle extras = getIntent().getExtras();
        id = extras.getLong(KEY_ID);
        importo = extras.getDouble(KEY_AMOUNT);
        tag = extras.getString(KEY_TAG);
        data = extras.getLong(KEY_DATE);
        descrizione = extras.getString(KEY_DESC);
        ripetizione_id = extras.getLong(KEY_REP_ID);
        importoValprin = extras.getDouble(KEY_AMOUNT_CURR);
        valuta = extras.getString(KEY_CURRENCY);
        conto = extras.getString(KEY_ACCOUNT);
        preferito = extras.getInt(KEY_FAVORITE);
    }


    // Display expense/earning details on the layout.
    private void displayDetails() {
        // App bar: tag and amount.
        tvVoce.setText(tag);
        tvToolbarTitle.setText(tag);

        Currency prefCurrency = UtilityVarious.getPrefCurrency(this);
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
        nf.setCurrency(prefCurrency);
        String importoFormattato = nf.format(importoValprin);
        tvImporto.setText(importoFormattato);

        // Currency section.
        if (!valuta.equals(prefCurrency.getCurrencyCode())) {
            NumberFormat nfCurrency = NumberFormat.getInstance(Locale.getDefault());
            NumberFormat nfExchangeRate = NumberFormat.getInstance();
            nfExchangeRate.setMaximumFractionDigits(4);

            float cambio = (float) (importoValprin / importo);
            ((TextView) findViewById(R.id.tvImportoOriginale)).setText(nfCurrency.format(importo) + " " + Currency.getInstance(valuta).getSymbol());
            ((TextView) findViewById(R.id.tvTassoCambio)).setText(nfCurrency.format(cambio));
        } else {
            findViewById(R.id.spese_entrate_dettaglio_voce_rlImporto).setVisibility(View.GONE);
        }

        // Details section.
        tvConto.setText(conto);

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);

        tvData.setText(df.format(new Date(data)));
        if (descrizione.length() > 0) {
            tvDescrizione.setText(descrizione);
        } else {
            tvDescrizione.setVisibility(View.GONE);
            findViewById(R.id.tvDescrizioneTitolo).setVisibility(View.GONE);
        }
    }


    // Set up sound effects it these are enabled.
    private void setupSoundEffects() {
        if (UtilityVarious.soundsEnabled(this)) {
            soundEffectsManager = SoundEffectsManager.getInstance();
            soundEffectsManager.loadSounds(this.getApplicationContext());
        }
    }

}
