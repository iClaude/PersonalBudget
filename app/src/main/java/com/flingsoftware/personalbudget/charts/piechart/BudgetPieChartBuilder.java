/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.charts.piechart;

import android.graphics.Color;

import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Design patterns: this class plays the role of ConcreteBuilder.
 * This class is used to create a pie chart showing the percentage of completion of a
 * budget, showing the amount spent in red and the remaining amount in green.
 */

public class BudgetPieChartBuilder extends SixElementsPieChartBuilder {

    @Override
    public void prepareData(AmountAndLabel[] amountsAndLabels) {
        // the raw data is ok
    }

    // Special formatting for BudgetPieChart (used in the BudgetDetails Activity.
    @Override
    public DefaultRenderer createDefaultRenderer() {
        AmountAndLabel[] amountsAndLabel = getAmountsAndLabels();
        NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.getDefault());
        float multText = getMultText();
        final DefaultRenderer defaultRenderer = new DefaultRenderer();
        for (int i = 0; i < amountsAndLabel.length; i++) {
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(colors[i]);
            seriesRenderer.setDisplayChartValues(true);
            seriesRenderer.setChartValuesFormat(percentFormat);
            seriesRenderer.setChartValuesSpacing(5);
            if (i == 0) {
                seriesRenderer.setHighlighted(true);
            }

            defaultRenderer.addSeriesRenderer(seriesRenderer);
        }

        defaultRenderer.setChartTitle("Budget\n\n");
        defaultRenderer.setChartTitleTextSize(30 * multText);
        defaultRenderer.setZoomEnabled(true);
        defaultRenderer.setZoomButtonsVisible(true);
        defaultRenderer.setPanEnabled(false);
        defaultRenderer.setApplyBackgroundColor(true);
        defaultRenderer.setBackgroundColor(Color.argb(255, 197, 202, 233));
        defaultRenderer.setDisplayValues(true);
        defaultRenderer.setLabelsTextSize(30 * multText);
        defaultRenderer.setLabelsColor(Color.argb(217, 66, 66, 66));
        defaultRenderer.setLegendTextSize(35 * multText);
        defaultRenderer.setClickEnabled(true);
        defaultRenderer.setSelectableBuffer(10);

        return defaultRenderer;
    }
}
