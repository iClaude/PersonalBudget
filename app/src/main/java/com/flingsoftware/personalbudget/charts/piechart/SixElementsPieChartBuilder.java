/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.charts.piechart;

import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;

import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
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
    protected final int[] colors = {Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.GRAY};

    /*
        In the SixElementPieChart we show only the 6 greatest elements, putting the remainder
        in the 6th position under the tag "others".
        First we sort the array, then create an array of 6 elements, copy the 5 greatest
        elements in the first 5 position, and the rest is summed in the 6th position.
        To do this we modify the array amountsAndLabels used by PieChartBuilder.
     */
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
        }
        if (amountsAndLabels.length > 6) {
            newAmountsAndsLabels[5].setLabel(getContext().getString(R.string.database_varie));
        }

        setAmountsAndLabels(newAmountsAndsLabels);
    }

    // Special formatting for SixElementsPieChart.
    @Override
    public CategorySeries createCategorySeries() {
        AmountAndLabel[] amountsAndLabels = getAmountsAndLabels();
        CategorySeries distributionSeries = new CategorySeries("");
        for (AmountAndLabel amountsAndLabel : amountsAndLabels) {
            distributionSeries.add(amountsAndLabel.getLabel(), amountsAndLabel.getAmount() / getTotalAmount());
        }

        return distributionSeries;
    }

    // Special formatting for SixElementsPieChart.
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

    /*
        Generic click listener for a pie chart: display the amount and percentage.
     */
    @Override
    public void createOnClickListener() {
        final GraphicalView pieChart = getPieChart();
        pieChart.setOnClickListener(new View.OnClickListener() {
            int evidPrec = 0;
            final NumberFormat nfPerc = NumberFormat.getPercentInstance(Locale.getDefault());

            {
                nfPerc.setMaximumFractionDigits(2);
            }

            @Override
            public void onClick(View v) {
                SeriesSelection seriesSelection = pieChart.getCurrentSeriesAndPoint();
                if (seriesSelection != null) {
                    int seriesIndex = seriesSelection.getPointIndex();

                    getDefaultRenderer().getSeriesRendererAt(evidPrec).setHighlighted(false);
                    getDefaultRenderer().getSeriesRendererAt(seriesIndex).setHighlighted(true);
                    evidPrec = seriesIndex;
                    pieChart.repaint();

                    Toast.makeText(getContext(), getContext().getString(R.string.statistiche_importo) + ": " + UtilityVarious.formatAmountCurrency(getAmountsAndLabels()[seriesIndex].getAmount(), getContext()) + "\n" + getContext().getString(R.string.statistiche_percentuale) + ": " + nfPerc.format(getAmountsAndLabels()[seriesIndex].getAmount() / getTotalAmount()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
