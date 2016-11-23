/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app.budgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.BudgetModifica;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCVociAbs;
import com.flingsoftware.personalbudget.oggetti.Budget;
import com.flingsoftware.personalbudget.utilita.BlurBuilder;
import com.flingsoftware.personalbudget.utilita.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utilita.SoundEffectsManager;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_ELIMINAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.TIPO_OPERAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;


/**
 * This Activity displays budget data. Budget details are displayed in the app bar as well
 * as in the 3 Fragments contained in this Activity.
 */

public class BudgetDetails extends AppCompatActivity {

    // Constants.
    private static final String TAG = "BudgetDetails";
    private static final String KEY_ID = "KEY_ID";
    public static final String KEY_BUDGET = "KEY_BUDGET";
    private static final int RESULT_CODE_EDIT = 0;

    // Variables.
    // Widgets.
    private TextView tvToolbarTitle;
    private TextView tvTagAppbar;
    private TextView tvAmountAppbar;
    private TextView tvTagToolbar;
    private TextView tvAmountToolbar;
    // Budget details.
    private Fragment[] fragments;
    private Budget budget;
    private long id;
    private String budgetType;
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

        // First get the budget id from the Intent that launched this Activity and create the Fragments.
        getBudgetIdAndCreateFragments();

        // Get references to layout widgets.
        initLayoutWidgets();

        // App bar.
        setUpAppbar();

        // Fragments.
        setupFragments();

        // Get expense/earning details and display them.
        getDetails();

        // Sound effects.
        soundEffectsManager = SoundEffectsManager.getInstance();
    }

    // First get the budget id from the Intent that launched this Activity and create the Fragments.
    private void getBudgetIdAndCreateFragments() {
        Bundle extras = getIntent().getExtras();
        id = extras.getLong(KEY_ID);
        fragments = new Fragment[3];
        fragments[0] = BudgetDetailsData.newInstance(id);
        fragments[1] = BudgetDetailsExpenses.newInstance(id);
        fragments[2] = BudgetDetailsHistory.newInstance(id);
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

    // Set up sliding tabs for this Activity.
    private void setupFragments() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        BudgetFragmentPagerAdapter fragmentAdapter = new BudgetFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);
        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        // Setting a custom view for each tab (icon + text).
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(fragmentAdapter.getTabView(i));
        }
    }

    // Get budget details using the id stored in the Intent that launched this Activity.
    private void getDetails() {
        new GetBudgetDetailsTask().execute(id);
    }

    // Retrieve budget details in a separate thread.
    private class GetBudgetDetailsTask extends AsyncTask<Long, Object, Void> {
        DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDetails.this);

        protected Void doInBackground(Long... params) {
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();

            budget = Budget.makeBudgetFromCursor(cursor, BudgetDetails.this);

            cursor.close();
            dbcSpeseBudget.close();
            return null;
        }

        protected void onPostExecute(Void result) {
            displayDetailsActivity();
        }
    }

    // Display budget details in the layout.
    private void displayDetailsActivity() {
        // Appbar.
        tvTagToolbar.setText(budget.getTagWithoutComma());
        tvTagAppbar.setText(budget.getTagWithoutComma());
        // Running text.
        tvTagAppbar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvTagAppbar.setSingleLine(true);
        tvTagAppbar.setMarqueeRepeatLimit(5);
        tvTagAppbar.setSelected(true);
        // Amount formatted in main currency.
        String amountFormatted = UtilityVarious.getFormattedAmount(budget.getAmount(), this);
        tvAmountToolbar.setText(amountFormatted + " (" + budgetType + ")");
        tvAmountAppbar.setText(amountFormatted + " / " + budgetType);
        // Rating bar.
        float perc = (float) (budget.getExpenses() / budget.getAmount()) * 100;
        setRatingBar(perc);
        // Load main image in a separate thread.
        new LoadHeaderImageTask().execute(budget.getTag());
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

    /*
        Given the percentage of budget already spent, show a RatingBar with appropriate color
        and number of stars filled.
     */
    private void setRatingBar(float perc) {
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        float filling = (perc * ratingBar.getNumStars()) / 100;
        ratingBar.setRating(filling);
    }

    private class BudgetFragmentPagerAdapter extends FragmentPagerAdapter {
        private final int PAGE_COUNT = 3;
        private String[] tabTitles = getResources().getStringArray(R.array.budget_fragments_titles);
        private int iconeTabIds[] = {R.drawable.ic_action_view_as_list, R.drawable.ic_action_bad, R.drawable.ic_content_event};
        private Context context;

        public BudgetFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position.
            return tabTitles[position];
        }

        // Returns a custom view for each tab (icon + text view).
        public View getTabView(int position) {
            View v = LayoutInflater.from(context).inflate(R.layout.tab, null);
            ImageView ivIcon = (ImageView) v.findViewById(R.id.ivIcona);
            TextView tvTab = (TextView) v.findViewById(R.id.tvTitolo);
            ivIcon.setImageResource(iconeTabIds[position]);
            tvTab.setText(tabTitles[position]);

            return v;
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
            case R.id.menu_budget_dettaglio_cancella:
                delete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Edit this budget.
    public void edit(View v) {
        Intent intent = new Intent(this, BudgetModifica.class);
        intent.putExtra(KEY_BUDGET, budget);

        startActivityForResult(intent, RESULT_CODE_EDIT);
    }

    private void delete() {
        final boolean singleBudget = budget.getRepetition().equals("una_tantum") ? true : false;

        AlertDialog.Builder builder = new AlertDialog.Builder(BudgetDetails.this);
        builder.setTitle(R.string.dettagli_voce_conferma_elimina_titolo);
        int msgId = singleBudget ? R.string.dettagli_voce_conferma_elimina_msg : R.string.budget_BudgetDettaglioVoce_eliminaRipetuti_msg;
        builder.setMessage(msgId);
        builder.setNegativeButton(R.string.cancella, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Use of the Template Method Pattern.
                DeleteBudgetTask deleteBudgetTask = singleBudget ? new DeleteOneBudgetTask() : new DeleteMultipleBudgetsTask();
                deleteBudgetTask.execute(id);
            }
        });
        builder.setCancelable(true);
        AlertDialog confirmDialog = builder.create();
        confirmDialog.show();
    }

    /*
        AsyncTask used to delete one or multiple budges of the same type.
        Plays the rose of AbstractClass in the Template Method Pattern. Subclasses must implement
        deleteBudgets() method, depending on wether they delete one or multiple budges.
        doInBackground and onPostExecute are template methods, containing the code that doesn't
        change.
     */
    private abstract class DeleteBudgetTask extends AsyncTask<Long, Object, Integer> {
        DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDetails.this);
        long budgetId;

        @Override
        protected Integer doInBackground(Long... params) {
            budgetId = params[0];
            dbcSpeseBudget.openModifica();
            int budgetsDeleted = deleteBudgets();
            dbcSpeseBudget.close();
            return budgetsDeleted;
        }


        @Override
        protected void onPostExecute(Integer result) {
            soundEffectsManager.playSound(SoundEffectsManager.SOUND_DELETED);
            String msg = getResources().getString(R.string.toast_budget_eliminato, result);
            new MioToast(BudgetDetails.this, msg).visualizza(Toast.LENGTH_SHORT);

            final Intent intAggiornaWidget = new Intent(WIDGET_AGGIORNA);
            sendBroadcast(intAggiornaWidget);

            Intent intRitorno = new Intent();
            intRitorno.putExtra(TIPO_OPERAZIONE, OPERAZIONE_ELIMINAZIONE);
            setResult(Activity.RESULT_OK, intRitorno);
            finish();
        }

        /*
            Subclasses must override this method to implement the algorithm to delete one or
            multiple budgets.
         */
        public abstract int deleteBudgets();
    }

    /*
        ConcreteClass in the TemplateMethodPattern.
        This class deletes a single budget.
     */
    private class DeleteOneBudgetTask extends DeleteBudgetTask {
        @Override
        public int deleteBudgets() {
            return dbcSpeseBudget.eliminaSpesaBudget(budgetId);
        }
    }

    /*
    ConcreteClass in the TemplateMethodPattern.
    This class deletes multiple budgets of the same type.
 */
    private class DeleteMultipleBudgetsTask extends DeleteBudgetTask {
        @Override
        public int deleteBudgets() {
            return dbcSpeseBudget.eliminaBudgetAnaloghi(budget.getFirstBudget());
        }
    }

}
