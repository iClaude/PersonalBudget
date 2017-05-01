/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget;

import com.flingsoftware.personalbudget.utility.NumberFormatter;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit tests for class NumberFormatter.
 */

public class NumberFormatterUnitTest {

    @Test
    public void testFormatAmountNoCurrency1() throws Exception {
        double amount = 125.00;
        String amountForm = NumberFormatter.formatAmountNoCurrency(amount);
        assertThat(amountForm, equalTo("125,00"));
    }

    @Test
    public void testFormatAmountNoCurrency2() throws Exception {
        double amount = 12500.75;
        String amountForm = NumberFormatter.formatAmountNoCurrency(amount);
        assertThat(amountForm, equalTo("12.500,75"));
    }

    @Test
    public void testFormatAmountNoCurrency3() throws Exception {
        double amount = -12500.75;
        String amountForm = NumberFormatter.formatAmountNoCurrency(amount);
        assertThat(amountForm, equalTo(" - 12.500,75"));
    }
}
