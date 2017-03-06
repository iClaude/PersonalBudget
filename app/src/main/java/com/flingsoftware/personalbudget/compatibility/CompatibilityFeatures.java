/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.compatibility;

import android.app.Activity;

/**
 * Compatibility features for specific API versions.
 * AbstractProduct in AbstractFactory pattern.
 */

public interface CompatibilityFeatures {
    void setActivityFeatures(Activity activity);
}
