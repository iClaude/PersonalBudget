/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.charts.piechart;

import android.graphics.Color;

import com.flingsoftware.personalbudget.R;

import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

/**
 * Design patterns: plays the role of ConcreteBuilder.
 * Used to create a pie chart with a maximum of 6 elements (if there are more than 6 elements
 * smaller amounts will be grouped into a category named "others".
 */

public class SixElementsPieChartBuilder extends PieChartBuilder {
    private int colors[] = {Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.GRAY};
    private double totalAmount;

    @Override
    public void prepareData(AmountAndLabel[] amountsAndLabels) {
        Arrays.sort(amountsAndLabels);
        int newSize = amountsAndLabels.length <= 6 ? amountsAndLabels.length : 6;
        AmountAndLabel[] newAmountsAndsLabels = new AmountAndLabel[newSize];
        for (int i = 0; i < amountsAndLabels.length; i++) {
            if (i < 5) {
                newAmountsAndsLabels[i].setAmount(amountsAndLabels[i].getAmount());
                newAmountsAndsLabels[i].setLabel(amountsAndLabels[i].getLabel());
            } else {
                newAmountsAndsLabels[i].setAmount(newAmountsAndsLabels[i].getAmount() + amountsAndLabels[i].getAmount());
            }
            totalAmount = totalAmount + amountsAndLabels[i].getAmount();
        }
        if (amountsAndLabels.length > 6) {
            newAmountsAndsLabels[5].setLabel(getContext().getString(R.string.database_varie));
        }

        setAmountsAndLabels(newAmountsAndsLabels);
    }

    @Override
    public CategorySeries createCategorySeries() {
        AmountAndLabel[] amountsAndLabels = getAmountsAndLabels();
        CategorySeries distributionSeries = new CategorySeries("");
        for (int i = 0; i < amountsAndLabels.length; i++) {
            distributionSeries.add(amountsAndLabels[i].getLabel(), amountsAndLabels[i].getAmount() / totalAmount);
        }

        return distributionSeries;
    }

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

        defaultRenderer.setZoomEnabled(true);
        defaultRenderer.setZoomButtonsVisible(false);
        defaultRenderer.setPanEnabled(true);
        defaultRenderer.setApplyBackgroundColor(true);
        defaultRenderer.setBackgroundColor(Color.argb(255, 255, 255, 255));
        defaultRenderer.setChartTitle(getTitle());
        defaultRenderer.setChartTitleTextSize(35 * multText);
        defaultRenderer.setDisplayValues(true);
        defaultRenderer.setLabelsTextSize(30 * multText);
        defaultRenderer.setLabelsColor(Color.BLACK);
        defaultRenderer.setLegendTextSize(25 * multText);
        defaultRenderer.setClickEnabled(true);
        defaultRenderer.setSelectableBuffer(10);

        return defaultRenderer;
    }

    @Override
    public void createOnClickListener() {

    }
}
