/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.utility;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

/**
 * This class represents the Behavior for a FAB at the bottom of the screen: when the AppBarLayout
 * expands, the FAB should disappear and viceversa.
 */
public class FABLowShowHideBehavior extends FABShowHideBehavior {

    public FABLowShowHideBehavior() {
    }

    public FABLowShowHideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        // Dependency is an instance of the View to which the FAB is anchored (i.e. NestedScrollView)
        if (!(dependency instanceof AppBarLayout)) return false;

        if (isAppBarcollapsed(parent, (AppBarLayout) dependency) && child.getVisibility() != View.VISIBLE) {
            child.show();
        } else if (!isAppBarcollapsed(parent, (AppBarLayout) dependency) && child.getVisibility() == View.VISIBLE) {
            child.hide();
        }

        return false;
    }

}
