/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.charts.piechart;

/**
 * Design patterns: this class plays the role of ConcreteBuilder.
 * This class is used to create a pie chart showing the percentage of completion of a
 * budget, showing the amount spent in red and the remaining amount in green.
 */

public class BudgetPieChartBuilder extends SixElementsPieChartBuilder {

    @Override
    public void prepareData(AmountAndLabel[] amountsAndLabels) {
        setAmountsAndLabels(amountsAndLabels);
        double total = 0.0;
        for (AmountAndLabel amountAndLabel : amountsAndLabels) {
            total += amountAndLabel.getAmount();
        }
        setTotalAmount(total);
    }
}
