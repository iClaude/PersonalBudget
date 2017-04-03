/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget;

import com.flingsoftware.personalbudget.utility.UtilityVarious;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit test for class UtilityVarious.
 */

public class UtilityVariousUnitTest {

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
}
