/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget;

import com.flingsoftware.personalbudget.app.FunzioniComuni;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for class FunzioniComuni.
 */

public class FunzioniComuniUnitTest {
    @Test
    public void testGetDataAttuale() throws Exception {
        long today = FunzioniComuni.getDataAttuale();
        assertTrue("Today's time set to zero", today > 0);

        GregorianCalendar gregToday = new GregorianCalendar();
        gregToday.setTimeInMillis(today);

        int hour = gregToday.get(Calendar.HOUR);
        int minute = gregToday.get(Calendar.MINUTE);
        int second = gregToday.get(Calendar.SECOND);
        int millisecond = gregToday.get(Calendar.MILLISECOND);
        assertThat(hour, is(0));
        assertThat(minute, is(0));
        assertThat(second, is(0));
        assertThat(millisecond, is(0));
    }

}
