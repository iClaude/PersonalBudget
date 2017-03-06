/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.compatibility;

import android.os.Build;

/**
 * Returns an implementation of CompatibilityFeatures in order to add features
 * specific to API versions.
 * ConcreteFactory in AbstractFactory pattern.
 */

public class ApiFeaturesFactory {
    public CompatibilityFeatures getApiFeatures() {
        CompatibilityFeatures compatibilityFeatures = null;

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            compatibilityFeatures = new Api19Features();
        }

        return compatibilityFeatures;
    }
}
