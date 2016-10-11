/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.utility.AvatarImageBehavior;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCExpEarAbs;
import com.flingsoftware.personalbudget.database.DBCExpEarRepeatedAbs;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_ELIMINAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.TIPO_OPERAZIONE;
import static com.flingsoftware.personalbudget.database.DBCExpEarRepeatedAbs.COL_DATE_END;
import static com.flingsoftware.personalbudget.database.DBCExpEarRepeatedAbs.COL_DATE_START;
import static com.flingsoftware.personalbudget.database.DBCExpEarRepeatedAbs.COL_REP;

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
    private final DateFormat dfDate = DateFormat.getDateInstance(DateFormat.MEDIUM, miaLocale);

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

        tvData.setText(dfDate.format(new Date(data)));
        if (descrizione.length() > 0) {
            tvDescrizione.setText(descrizione);
        } else {
            tvDescrizione.setVisibility(View.GONE);
            findViewById(R.id.tvDescrizioneTitolo).setVisibility(View.GONE);
        }

        // Repetition section.
        if (ripetizione_id == 1) {
            findViewById(R.id.spese_entrate_dettaglio_voce_rlRipetizione).setVisibility(View.GONE);
        } else {
            new LoadRepetitionDetails().execute(ripetizione_id);
        }
    }


    // Set up sound effects it these are enabled.
    private void setupSoundEffects() {
        if (UtilityVarious.soundsEnabled(this)) {
            soundEffectsManager = SoundEffectsManager.getInstance();
            soundEffectsManager.loadSounds(this.getApplicationContext());
        }
    }


    // Load details of repeated expenses/earnings.
    private class LoadRepetitionDetails extends AsyncTask<Long, Object, Cursor> {
        DBCExpEarRepeatedAbs dbcExpEarRepeated = getDBCExpEarRepeated();
        long dataFine;

        protected Cursor doInBackground(Long... params) {
            dbcExpEarRepeated.openLettura();
            Cursor cursor = dbcExpEarRepeated.getItemRepeated(params[0]);

            return cursor;
        }

        protected void onPostExecute(Cursor cursor) {
            cursor.moveToFirst();
            String repetition = cursor.getString(cursor.getColumnIndex(COL_REP));
            String ripetizione = cursor.getString(cursor.getColumnIndex(COL_REP));
            dataFine = cursor.getLong(cursor.getColumnIndex(COL_DATE_END));
            dataInizio = cursor.getLong(cursor.getColumnIndex(COL_DATE_START));

            String tipiBudget[] = getResources().getStringArray(R.array.ripetizioni);
            if (ripetizione.equals("nessuna")) {
                ripetizione = tipiBudget[0];
            } else if (ripetizione.equals("giornaliero")) {
                ripetizione = tipiBudget[1];
            } else if (ripetizione.equals("settimanale")) {
                ripetizione = tipiBudget[2];
            } else if (ripetizione.equals("bisettimanale")) {
                ripetizione = tipiBudget[3];
            } else if (ripetizione.equals("mensile")) {
                ripetizione = tipiBudget[4];
            } else if (ripetizione.equals("annuale")) {
                ripetizione = tipiBudget[5];
            } else if (ripetizione.equals("giorni_lavorativi")) {
                ripetizione = tipiBudget[6];
            } else if (ripetizione.equals("weekend")) {
                ripetizione = tipiBudget[7];
            }

            tvRipetizione.setText(ripetizione);
            tvFineRipetizione.setText(dfDate.format(new Date(dataFine)));
            cursor.close();
            cursor.close();
        }
    }

    /*
        Returns an implementation of DBCSpeseRipetute or DBCEntrate ripetute, depending on
        wether this class displays an expense or earning.
     */
    public abstract DBCExpEarRepeatedAbs getDBCExpEarRepeated();


    /*
        Enter animation. NestedScrollView, which contains a CardView representing the main content,
        is moved upwards with an animation.
    */
    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        View contentView = findViewById(R.id.nsv_main_content);
        float offset = getResources().getDimensionPixelSize(R.dimen.content_offset_y);
        Interpolator interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);
        contentView.setVisibility(View.VISIBLE);
        fabAlto.setVisibility(View.VISIBLE);
        contentView.setTranslationY(offset);
        contentView.setAlpha(0.3f);
        contentView.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(interpolator)
                .setDuration(500L)
                .start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_speseentratedettagliovoce, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_speseEntrateDettaglioVoce_cancella:
                eliminaVoce();

                return true;
            case R.id.menu_speseEntrateDettaglioVoce_duplica:
                duplicate(); // duplication of expense/earning

                return true;
            case android.R.id.home:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // FAB pressed: edit expense/earning.
    public void fabPressed(View v) {
        edit();
    }


    // Launch Activity to edit this expense/earning.
    private void edit() {
        Intent intent = getEditIntent();
        intent.putExtra(KEY_ID, id);
        intent.putExtra(KEY_TAG, tag);
        intent.putExtra(KEY_AMOUNT, importo);
        intent.putExtra(KEY_CURRENCY, valuta);
        intent.putExtra(KEY_AMOUNT_CURR, importoValprin);
        intent.putExtra(KEY_DATE, data);
        intent.putExtra(KEY_DESC, descrizione);
        intent.putExtra(KEY_REP_ID, ripetizione_id);
        intent.putExtra(KEY_ACCOUNT, conto);
        intent.putExtra(KEY_FAVORITE, preferito);
        startActivityForResult(intent, 0);
    }


    /*
        Returns an Intent to launch an Activity to edit this expense/earning.
        Subclasses must specify the class (for expenses or earnings).
     */
    public abstract Intent getEditIntent();


    // Duplicate this element: add a new expense/earning with the same data.
    private void duplicate() {
        UtilityVarious.visualizzaDialogOKAnnulla(ExpenseEarningDetails.this,
                getString(R.string.dettagli_voce_conferma_duplica_titolo),
                getString(R.string.dettagli_voce_conferma_duplica_msg),
                getString(R.string.ok),
                true, getString(R.string.cancella),
                0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DuplicateElementTask().execute((Object[]) null);
                    }
                });
    }


    /*
        Duplicazione voce: AsyncTask x duplicare la voce in un thread separato. NB: duplicazione =
        aggiungere nuova voce con data oggi e con gli stessi dati.
    */
    private class DuplicateElementTask extends AsyncTask<Object, Void, Void> {
        DBCExpEarAbs dbcExpEar = getDBCExpEar();

        @Override
        protected Void doInBackground(Object... params) {
            dbcExpEar.openModifica();
            dbcExpEar.insertElement(FunzioniComuni.getDataAttuale(), tag, importo, valuta, importoValprin, descrizione, ripetizione_id, conto, 0);
            dbcExpEar.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String msg = getResources().getString(R.string.dettagli_voce_elemento_duplicato);
            new MioToast(ExpenseEarningDetails.this, msg).visualizza(Toast.LENGTH_SHORT);
            // Subclasses representing an expense must override this method to update the budget table.
            updateBudgetTable();

            setResult(Activity.RESULT_OK);
            finish();
        }
    }


    /*
        Returns an implementation of DBCSpeseSostenute or DBCEntrateIncassate, depending on
        wether this class displays an expense or earning.
    */
    public abstract DBCExpEarAbs getDBCExpEar();


    /*
        The Activity that displays expenses must override this method to update the budget table
        when an expense is duplicated. The Activity diplaying earnings must override this
        and keeping it void.
     */
    public abstract void updateBudgetTable();


    // Delete this expense/earning in a separate thread.
    private class DeleteThisElementTask extends AsyncTask<Long, Object, Object> {
        DBCExpEarAbs dbcExpEarAbs = getDBCExpEar();

        @Override
        protected Object doInBackground(Long... params) {
            dbcExpEarAbs.openModifica();
            dbcExpEarAbs.deleteElement(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            dbcExpEarAbs.close();

            String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, 1, 1);
            new MioToast(ExpenseEarningDetails.this, msg).visualizza(Toast.LENGTH_SHORT);
            // Subclasses representing an expense must override this method to update the budget table.
            updateBudgetTable();

            Intent returnIntent = new Intent();
            returnIntent.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }


    /*
        Deletes all repeated expenses/earnings from tables spese_sost and entrate_inc,
        as well as spese_ripet and entrate_ripet.
    */
    private class DeleteAllRepeatedElementsTask extends AsyncTask<Long, Object, Integer> {
        DBCExpEarAbs dbcExpEarAbs = getDBCExpEar();
        DBCExpEarRepeatedAbs dbcExpEarRepeatedAbs = getDBCExpEarRepeated();

        @Override
        protected Integer doInBackground(Long... params) {
            dbcExpEarAbs.openModifica();
            int elementsDeleted = dbcExpEarAbs.deleteElementRepeated(params[0]);

            dbcExpEarRepeatedAbs.openModifica();
            dbcExpEarRepeatedAbs.deleteElementRepeated(params[0]);

            return elementsDeleted;
        }

        @Override
        protected void onPostExecute(Integer result) {
            dbcExpEarAbs.close();
            dbcExpEarRepeatedAbs.close();

            String msg = getResources().getQuantityString(R.plurals.dettagli_voce_x_voci_eliminate, result, result);
            new MioToast(ExpenseEarningDetails.this, msg).visualizza(Toast.LENGTH_SHORT);
            // Subclasses representing an expense must override this method to update the budget table.
            updateBudgetTable();

            Intent returnIntent = new Intent();
            returnIntent.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // TODO: 10/10/2016 inserire suoni quando si cancella o aggiunge una voce
/*        if (suoniAbilitati && confermaElimina) {
            soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_CANCELLAZIONE), 1, 1, 1, 0, 1f);
        } else if (suoniAbilitati && confermaDuplica) {
            soundPool.play(mappaSuoni.get(CostantiSuoni.SUONO_AGGIUNGI_SPESA_ENTRATA), 1, 1, 1, 0, 1f);
        }*/
    }
}
