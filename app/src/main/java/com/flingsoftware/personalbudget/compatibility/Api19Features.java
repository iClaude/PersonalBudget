/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.compatibility;

import android.app.Activity;
import android.support.v4.content.ContextCompat;

import com.flingsoftware.personalbudget.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Compatibility featues for API 19 (Android 4.4 Kitkat).
 * ConcreteProduct in AbstractFactory pattern.
 */

public class Api19Features implements CompatibilityFeatures {
    @Override
    public void setActivityFeatures(Activity activity) {
        // In API 19 tint the status bar with primary color.
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(ContextCompat.getColor(activity, R.color.primary_dark));
    }
}
