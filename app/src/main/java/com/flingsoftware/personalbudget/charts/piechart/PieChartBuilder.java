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
    private DefaultRenderer defaultRenderer;
    private AmountAndLabel amountsAndLabels[];
    private String title;
    private Context context;
    private double totalAmount;


    public GraphicalView getPieChart() {
        return pieChart;
    }

    /*
        For BudgetPieChart the array amountsAndLabels consists of 2 elements: the amount spent and
        the amount saved.
     */
    public void createNewPieChart(Context context, AmountAndLabel[] amountsAndLabels, String title) {
        setContext(context);
        setTitle(title);
        setAmountsAndLabels(amountsAndLabels);

        setTotalAmount(calculateTotal(amountsAndLabels));
        prepareData(amountsAndLabels);
        CategorySeries categorySeries = createCategorySeries();
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
        return getContext().getResources().getDisplayMetrics().density / 2;
    }

    private double calculateTotal(AmountAndLabel[] myAmountsAndLabels) {
        double total = 0.0;
        for (AmountAndLabel amountAndLabel : myAmountsAndLabels) {
            total += amountAndLabel.getAmount();
        }

        return total;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public void setAmountsAndLabels(AmountAndLabel[] amountsAndLabels) {
        this.amountsAndLabels = amountsAndLabels;
    }

    private void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
