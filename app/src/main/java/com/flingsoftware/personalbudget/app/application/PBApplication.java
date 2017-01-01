/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.application;

import android.app.Application;
import android.content.Context;

/**
 * This class is used to get an application Context whenever it's needed within the
 * app, especially for Singleton classes not connected to Activities.
 */

public class PBApplication extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

    public static Context getAppContext() {
        return appContext;
    }
}
