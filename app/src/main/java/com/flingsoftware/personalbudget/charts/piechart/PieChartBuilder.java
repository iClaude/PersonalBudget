/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.charts.piechart;

import android.content.Context;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;

/**
 * Design patterns: plays the role of AbstractBuilder.
 * Abstract factory class used to create pie charts.
 */

public abstract class PieChartBuilder {
    // Variables.
    private GraphicalView pieChart;
    private AmountAndLabel amountsAndLabels[];
    private String title;
    private Context context;
    private CategorySeries categorySeries;
    private DefaultRenderer defaultRenderer;

    public GraphicalView getPieChart() {
        return pieChart;
    }

    public void createNewPieChart(Context context, AmountAndLabel[] amountsAndLabels, String title) {
        this.context = context;
        this.title = title;

        prepareData(amountsAndLabels);
        categorySeries = createCategorySeries();
        defaultRenderer = createDefaultRenderer();
        pieChart = ChartFactory.getPieChartView(context, categorySeries, defaultRenderer);
        createOnClickListener();

    }

    /*
    Subclasses must implement these methods to create specific types of pie charts.
     */
    public abstract void prepareData(AmountAndLabel[] amountsAndLabels);

    public abstract CategorySeries createCategorySeries();

    public abstract DefaultRenderer createDefaultRenderer();

    public abstract void createOnClickListener();

    public float getMultText() {
        float multText = getContext().getResources().getDisplayMetrics().density / 2;
        return multText;
    }

    // Setters and getters.


    public Context getContext() {
        return context;
    }

    public String getTitle() {
        return title;
    }

    public AmountAndLabel[] getAmountsAndLabels() {
        return amountsAndLabels;
    }

    public DefaultRenderer getDefaultRenderer() {
        return defaultRenderer;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmountsAndLabels(AmountAndLabel[] amountsAndLabels) {
        this.amountsAndLabels = amountsAndLabels;
    }
}
