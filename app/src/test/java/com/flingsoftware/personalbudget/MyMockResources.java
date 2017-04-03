/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget;

import android.test.mock.MockResources;

/**
 * Mock Resources class to get an array of budgets from the resources.
 */

public class MyMockResources extends MockResources {

    @Override
    public String[] getStringArray(int id) throws NotFoundException {
        if (id == R.array.ripetizioni_budget) {
            return new String[]{"Una tantum", "Daily", "Weekly", "Bi-Weekly", "Monthly", "Yearly"};
        }
        return null;
    }
}
