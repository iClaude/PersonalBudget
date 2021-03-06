/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget;

import android.content.Context;
import android.content.res.Resources;

import com.flingsoftware.personalbudget.utility.UtilityVarious;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit test for class UtilityVarious.
 */

@RunWith(MockitoJUnitRunner.class)
public class UtilityVariousUnitTest {

    private static MyMockContext myMockContext;
    private final String[] budgetTypes = {"Una tantum", "Daily", "Weekly", "Bi-Weekly", "Monthly", "Yearly"};

    @Mock
    Context myMockitoContext;
    @Mock
    Resources myMockitoResources;


    @Test
    public void testCreateTagsList1() throws Exception {
        String tags = "food,clothing,journeys,car";
        List<String> lstTags = UtilityVarious.createTagsList(tags);

        assertNotNull("List is null", lstTags);
        assertThat(lstTags, hasSize(4));
        assertThat(lstTags, hasItem("food"));
        assertThat(lstTags, hasItem("clothing"));
        assertThat(lstTags, hasItem("journeys"));
        assertThat(lstTags, hasItem("car"));
    }

    @Test
    public void testCreateTagsList2() throws Exception {
        String tags = "food";
        List<String> lstTags = UtilityVarious.createTagsList(tags);

        assertNotNull("List is null", lstTags);
        assertThat(lstTags, hasSize(1));
        assertThat(lstTags, hasItem("food"));
    }

    @Test
    public void testCreateTagsList3() throws Exception {
        String tags = "";
        List<String> lstTags = UtilityVarious.createTagsList(tags);

        assertNull("List is not null", lstTags);
    }

    @Test
    public void testCreateTagsList4() throws Exception {
        String tags = null;
        List<String> lstTags = UtilityVarious.createTagsList(tags);

        assertNull("List is not null", lstTags);
    }

    @Test
    public void testCreateTagsList5() throws Exception {
        String tags = ",";
        List<String> lstTags = UtilityVarious.createTagsList(tags);

        assertNull("List is not null", lstTags);
    }

    @BeforeClass
    public static void setupMockContext() throws Exception {
        myMockContext = new MyMockContext();
    }

    @Test
    public void testGetBudgetType() throws Exception {
        String budgetType = UtilityVarious.getBudgetType(myMockContext, "una_tantum");
        assertTrue("Budget type not correct", budgetType.equals("Una tantum"));

        budgetType = UtilityVarious.getBudgetType(myMockContext, "giornaliero");
        assertTrue("Budget type not correct", budgetType.equals("Daily"));

        budgetType = UtilityVarious.getBudgetType(myMockContext, "settimanale");
        assertTrue("Budget type not correct", budgetType.equals("Weekly"));

        budgetType = UtilityVarious.getBudgetType(myMockContext, "bisettimanale");
        assertTrue("Budget type not correct", budgetType.equals("Bi-Weekly"));

        budgetType = UtilityVarious.getBudgetType(myMockContext, "mensile");
        assertTrue("Budget type not correct", budgetType.equals("Monthly"));

        budgetType = UtilityVarious.getBudgetType(myMockContext, "annuale");
        assertTrue("Budget type not correct", budgetType.equals("Yearly"));
    }

    @Test
    public void testGetBudgetTypeWithMockito() throws Exception {
        when(myMockitoContext.getResources())
                .thenReturn(myMockitoResources);
        when(myMockitoResources.getStringArray(R.array.ripetizioni_budget))
                .thenReturn(budgetTypes);

        String budgetType = UtilityVarious.getBudgetType(myMockitoContext, "una_tantum");
        assertTrue("Budget type not correct", budgetType.equals("Una tantum"));

        budgetType = UtilityVarious.getBudgetType(myMockitoContext, "giornaliero");
        assertTrue("Budget type not correct", budgetType.equals("Daily"));

        budgetType = UtilityVarious.getBudgetType(myMockitoContext, "settimanale");
        assertTrue("Budget type not correct", budgetType.equals("Weekly"));

        budgetType = UtilityVarious.getBudgetType(myMockitoContext, "bisettimanale");
        assertTrue("Budget type not correct", budgetType.equals("Bi-Weekly"));

        budgetType = UtilityVarious.getBudgetType(myMockitoContext, "mensile");
        assertTrue("Budget type not correct", budgetType.equals("Monthly"));

        budgetType = UtilityVarious.getBudgetType(myMockitoContext, "annuale");
        assertTrue("Budget type not correct", budgetType.equals("Yearly"));
    }

    @Test
    public void testGetBudgetTypeNull() throws Exception {
        String budgetType = UtilityVarious.getBudgetType(myMockContext, "quinquennale");
        assertNull("Budget type is not null", budgetType);
    }

}
