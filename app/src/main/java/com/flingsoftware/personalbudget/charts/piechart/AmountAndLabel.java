/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.charts.piechart;

/**
 * This class represents a couple of amount and corresponding label used in charts.
 * The data used to create a chart consists of an array of AmountAndLabel.
 */

public class AmountAndLabel implements Comparable<AmountAndLabel> {
    private double amount;
    private String label;

    public AmountAndLabel() {
    }

    public AmountAndLabel(String label, double amount) {
        this.label = label;
        this.amount = amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getAmount() {
        return amount;
    }

    public String getLabel() {
        return label;
    }

    // Implementation of Comparable interface (useful for ordering the arrays).
    @Override
    public int compareTo(AmountAndLabel otherAmountAndLabel) {
        return (int) (getAmount() - otherAmountAndLabel.getAmount());
    }
}
