/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.budgets;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.flingsoftware.personalbudget.oggetti.ExpenseEarning;

import java.util.List;

/**
 * Used for the ExpandableRecyclerView of BigNerdRanch.
 */

public class ExpensesWithTag implements ParentListItem {

    // Variables.
    private int iconId;
    private String tag;
    private double total;
    private List<ExpenseEarning> expenses; // list of expenses with given tag in a specific budget

    // Constructors.

    public ExpensesWithTag() {
    }

    public ExpensesWithTag(int iconId, String tag, double total, List<ExpenseEarning> expenses) {
        this.iconId = iconId;
        this.tag = tag;
        this.total = total;
        this.expenses = expenses;
    }

    // Getters and setters.

    public int getIconId() {
        return iconId;
    }

    public String getTag() {
        return tag;
    }

    public double getTotal() {
        return total;
    }

    public List<ExpenseEarning> getExpenses() {
        return expenses;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setExpenses(List<ExpenseEarning> expenses) {
        this.expenses = expenses;
    }

    // Implementation of ParentListItem interface.

    @Override
    public List<ExpenseEarning> getChildItemList() {
        return expenses;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
