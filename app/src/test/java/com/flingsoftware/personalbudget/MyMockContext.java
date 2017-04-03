/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget;

import android.content.res.Resources;
import android.test.mock.MockContext;

/**
 * Mock context for testing purposes.
 */

public class MyMockContext extends MockContext {
    @Override
    public Resources getResources() {
        return new MyMockResources();
    }
}
