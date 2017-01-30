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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.app.BudgetModifica;
import com.flingsoftware.personalbudget.charts.piechart.AmountAndLabel;
import com.flingsoftware.personalbudget.charts.piechart.BudgetPieChartBuilder;
import com.flingsoftware.personalbudget.charts.piechart.PieChartBuilder;
import com.flingsoftware.personalbudget.customviews.MioToast;
import com.flingsoftware.personalbudget.database.DBCSpeseBudget;
import com.flingsoftware.personalbudget.database.DBCSpeseVoci;
import com.flingsoftware.personalbudget.database.DBCVociAbs;
import com.flingsoftware.personalbudget.oggetti.Budget;
import com.flingsoftware.personalbudget.utility.BlurBuilder;
import com.flingsoftware.personalbudget.utility.ListViewIconeVeloce;
import com.flingsoftware.personalbudget.utility.NumberFormatter;
import com.flingsoftware.personalbudget.utility.SoundEffectsManager;

import org.achartengine.GraphicalView;

import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.OPERAZIONE_ELIMINAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiDettaglioVoce.TIPO_OPERAZIONE;
import static com.flingsoftware.personalbudget.app.MainPersonalBudget.CostantiVarie.WIDGET_AGGIORNA;


/**
 * This Activity displays budget data. Budget details are displayed in the app bar as well
 * as in the 3 Fragments contained in this Activity.
 */

public class BudgetDetails extends AppCompatActivity {

    // Constants.
    private static final String TAG = "BUDGETS";
    private static final String KEY_ID = "KEY_ID";
    public static final String KEY_BUDGET = "KEY_BUDGET";
    private static final int RESULT_CODE_EDIT = 0;

    // Variables.
    // Widgets.
    private TextView tvTagAppbar;
    private TextView tvAmountAppbar;
    private TextView tvTagToolbar;
    private TextView tvAmountToolbar;
    private FloatingActionButton fabEdit;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ConstraintLayout bottomSheetViewgroup;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout llChart;
    // Budget details.
    private Fragment[] fragments;
    private Budget budget;
    private long id;
    private String budgetType;
    private long imageDisplayedId = -1; // currently displayed image
    // Graphics and multimedia.
    private SoundEffectsManager soundEffectsManager;
    private boolean fabVisible = true;
    private boolean appbarExpanded = true;


    /*
        Fragments in this Activity must implement this interface which is used to reload data
        from this Activity when a budget is edited.
     */
    public interface ReloadingData {
        void reloadData();
    }

    // Factory method to create an Intent to start this Activity.
    public static Intent makeIntent(Context context, long id) {
        Intent intent = new Intent(context, BudgetDetails.class);
        intent.putExtra(KEY_ID, id);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // When finish slide the Activity down with a transition.
            getWindow().setReturnTransition(new Slide(Gravity.START));
        }
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

        // BottomSheet.
        setupBottomSheet();

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
        tvTagAppbar = (TextView) findViewById(R.id.tvTagAppbar);
        tvAmountAppbar = (TextView) findViewById(R.id.tvAmountAppbar);
        tvTagToolbar = (TextView) findViewById(R.id.toolbar_title);
        tvAmountToolbar = (TextView) findViewById(R.id.toolbar_subtitle);
        fabEdit = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        TextView tvHint = (TextView) findViewById(R.id.tvHint);
        tvHint.setOnClickListener(ExpandCollapseListener);
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
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        BudgetFragmentPagerAdapter fragmentAdapter = new BudgetFragmentPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(fragmentAdapter);
        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        // Setting a custom view for each tab (icon + text).
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(fragmentAdapter.getTabView(i));
        }
    }

    // Setup BottomSheet and related actions.
    private void setupBottomSheet() {
        llChart = (LinearLayout) findViewById(R.id.llChart);
        bottomSheetViewgroup = (ConstraintLayout) findViewById(R.id.clBottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetViewgroup);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            /*
            When the BottomSheet is hidden, remove the FAB's anchor to it, so that the FAB
            can be shown/hidden when the app bar gets expanded/collapsed.
             */
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fabEdit.getLayoutParams();
                    lp.setAnchorId(View.NO_ID);
                    lp.gravity = Gravity.BOTTOM | GravityCompat.END;
                    fabEdit.setLayoutParams(lp);
                }
            }

            // Shrink the FAB when the BottomSheet slides up, and viceversa.
            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
                if (slideOffset >= 0) {
                    fabEdit.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
                }
            }
        });
    }

    // Get budget details using the id stored in the Intent that launched this Activity.
    private void getDetails() {
        new GetBudgetDetailsTask().execute(id);
    }

    // Retrieve budget details in a separate thread.
    private class GetBudgetDetailsTask extends AsyncTask<Long, Object, Void> {
        final DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDetails.this);

        protected Void doInBackground(Long... params) {
            dbcSpeseBudget.openLettura();
            Cursor cursor = dbcSpeseBudget.getSpesaBudget(params[0]);
            cursor.moveToFirst();

            budget = Budget.makeBudgetFromCursor(cursor, BudgetDetails.this);
            budgetType = budget.getBudgetType(BudgetDetails.this);

            cursor.close();
            dbcSpeseBudget.close();
            return null;
        }

        protected void onPostExecute(Void result) {
            displayDetailsActivity();
            displayPieChart();
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
        String amountFormatted = NumberFormatter.formatAmountMainCurrency(budget.getAmount(), this);
        tvAmountToolbar.setText(amountFormatted + " (" + budgetType + ")");
        tvAmountAppbar.setText(amountFormatted + " / " + budgetType);
        // Rating bar.
        float perc = (float) (budget.getExpenses() / budget.getAmount()) * 100;
        setRatingBar(perc);
        // Load main image in a separate thread.
        new LoadHeaderImageTask().execute(budget.getTag());
    }

    // Display pie chart in the bottom sheet.
    private void displayPieChart() {
        int arrSize = budget.getExpenses() >= budget.getAmount() ? 1 : 2;
        AmountAndLabel[] amountsAndLabels = new AmountAndLabel[arrSize];
        amountsAndLabels[0] = new AmountAndLabel(getString(R.string.budgets_chart_spent), budget.getExpenses());
        if (arrSize == 2) {
            amountsAndLabels[1] = new AmountAndLabel(getString(R.string.budgets_chart_left), budget.getAmount() - budget.getExpenses());
        }

        PieChartBuilder pieChartBuilder = new BudgetPieChartBuilder();
        pieChartBuilder.createNewPieChart(BudgetDetails.this, amountsAndLabels, getString(R.string.budgets_chart_title));
        GraphicalView pieChart = pieChartBuilder.getPieChart();
        llChart.removeAllViews();
        llChart.addView(pieChart);
    }

    // Load header image in a separate thread.
    private class LoadHeaderImageTask extends AsyncTask<String, Object, Bitmap> {
        int backgroundColor = R.color.primary_light;
        final DBCVociAbs dbcVociAbs = new DBCSpeseVoci(BudgetDetails.this);

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

            if (iconaId == imageDisplayedId)
                return null; // the image has not changed after a budget's update
            imageDisplayedId = iconaId; // update currently displayed image id

            // Create a blurred image representing the icon.
            Bitmap origBitmap = ListViewIconeVeloce.decodeSampledBitmapFromResource(getResources(), iconaIdDef, 128, 128);
            Bitmap blurredBitmap = BlurBuilder.blur(BudgetDetails.this, origBitmap);
            // Get a suitable color for image background.
            Palette palette = Palette.from(origBitmap).generate();
            backgroundColor = palette.getMutedColor(ContextCompat.getColor(BudgetDetails.this, R.color.primary_light));

            return blurredBitmap;
        }

        protected void onPostExecute(Bitmap myBitmap) {
            if (myBitmap == null) return;

            ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.vsHeader);
            int imageViewId = viewSwitcher.getDisplayedChild() == 0 ? R.id.appbar_image2 : R.id.appbar_image1;
            ImageView ivHeader = (ImageView) findViewById(imageViewId);
            ivHeader.setImageBitmap(myBitmap);
            ivHeader.setBackgroundColor(Color.rgb(Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)));

            // Show the header image with a fadein-fadeout animation.
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

            appbarExpanded = percentage <= 0.9;

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                if (!appbarExpanded && fabVisible) {
                    fabEdit.hide();
                    fabVisible = false;
                } else if (appbarExpanded && !fabVisible) {
                    fabEdit.show();
                    fabVisible = true;
                }
            }
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
        private final String[] tabTitles = getResources().getStringArray(R.array.budget_fragments_titles);
        private final int[] iconeTabIds = {R.drawable.ic_action_view_as_list, R.drawable.ic_action_bad, R.drawable.ic_content_event};
        private final Context context;

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

    private View.OnClickListener ExpandCollapseListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    };

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
                supportFinishAfterTransition();
                return true;
            case R.id.menu_budget_dettaglio_cancella:
                delete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Hide the BottomSheet.
    public void closeBottomSheet(View v) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    // Edit this budget.
    public void edit(View v) {
        Intent intent = new Intent(this, BudgetModifica.class);
        intent.putExtra(KEY_BUDGET, budget);

        startActivityForResult(intent, RESULT_CODE_EDIT);
    }

    private void delete() {
        final boolean singleBudget = budget.getRepetition().equals("una_tantum");

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
        final DBCSpeseBudget dbcSpeseBudget = new DBCSpeseBudget(BudgetDetails.this);
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
            supportFinishAfterTransition();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_CODE_EDIT && resultCode == RESULT_OK) {
            new GetBudgetDetailsTask().execute(id);
            displayPieChart();
            // Update only active Fragments (one to the left, one to the right.
            int currFrag = viewPager.getCurrentItem();
            ((ReloadingData) fragments[currFrag]).reloadData();
            if ((currFrag + 1) <= 2) ((ReloadingData) fragments[currFrag + 1]).reloadData();
            if ((currFrag - 1) >= 0) ((ReloadingData) fragments[currFrag - 1]).reloadData();
        }
    }
}
