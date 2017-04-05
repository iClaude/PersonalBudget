/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget;

import com.flingsoftware.personalbudget.oggetti.Budget;

import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Unit test for class Budget.
 */

public class BudgetUnitTest {

    private static MyMockContext myMockContext;
    private static Budget budget;


    @BeforeClass
    public static void setupObjects() throws Exception {
        myMockContext = new MyMockContext();
        budget = new Budget(0, 100.00, 2500, 1500, 57.00, 15, 6, 1, "giornaliero", "food,clothes,journeys,");
    }

    @Test
    public void testCreateWithConstructor() throws Exception {
        assertNotNull("Budget object is null", budget);
        assertTrue("Budget field not correct", budget.getAddRest() == 0);
        assertTrue("Budget field not correct", budget.getAmount() == 100.00);
        assertTrue("Budget field not correct", budget.getDateEnd() == 2500);
        assertTrue("Budget field not correct", budget.getDateStart() == 1500);
        assertTrue("Budget field not correct", budget.getExpenses() == 57.00);
        assertTrue("Budget field not correct", budget.getFirstBudget() == 15);
        assertTrue("Budget field not correct", budget.getId() == 6);
        assertTrue("Budget field not correct", budget.getLastAdded() == 1);
        assertTrue("Budget field not correct", budget.getRepetition().equals("giornaliero"));
        assertTrue("Budget field not correct", budget.getTag().equals("food,clothes,journeys,"));
    }

    @Test
    public void testGetTagWithoutComma() throws Exception {
        String tagEd = budget.getTagWithoutComma();
        assertNotNull("Tag edited is null", tagEd);
        char lastChar = tagEd.charAt(tagEd.length() - 1);
        assertFalse("Last char of tag is a comma", lastChar == ',');

        budget.setTag("food,clothes");
        tagEd = null;
        tagEd = budget.getTagWithoutComma();
        assertNotNull("Tag edited is null", tagEd);
        lastChar = tagEd.charAt(tagEd.length() - 1);
        assertFalse("Last char of tag is a comma", lastChar == ',');
    }

    @Test
    public void testBudgetType() throws Exception {
        budget.setRepetition("una_tantum");
        String budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Una tantum"));

        budget.setRepetition("giornaliero");
        budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Daily"));

        budget.setRepetition("settimanale");
        budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Weekly"));

        budget.setRepetition("bisettimanale");
        budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Bi-Weekly"));

        budget.setRepetition("mensile");
        budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Monthly"));

        budget.setRepetition("annuale");
        budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Yearly"));

        budget.setRepetition("quinquennale");
        budgetType = budget.getBudgetType(myMockContext);
        assertTrue("Budget type not correct", budgetType.equals("Una tantum"));
    }
}
