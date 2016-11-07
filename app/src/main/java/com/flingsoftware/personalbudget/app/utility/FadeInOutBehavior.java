/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app.utility;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * This is a simple fade in/out behavior that can be applied to any View, depending on how much
 * the Appbar is collapsed (collapsed: fade in; expanded: fade out).
 */

public class FadeInOutBehavior extends CoordinatorLayout.Behavior<View> {

    private static final String TAG = "FadeInOutBehavior";
    // Variables.
    private int appbarHeight;
    private int appbarScrollRange;
    private Rect mTmpRect;


    public FadeInOutBehavior() {
    }

    public FadeInOutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        Log.d(TAG, "layoutDependsOn: ");
        if (dependency instanceof AppBarLayout) {
            // App bar sizes.
            appbarHeight = dependency.getHeight();
            appbarScrollRange = ((AppBarLayout) dependency).getTotalScrollRange();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        Log.d(TAG, "onDependentViewChanged: ");
        // Dependency is an instance of the View to which the child is anchored (i.e. FrameLayout)
        if (!(dependency instanceof AppBarLayout)) return false;

        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }
        // First, let's get the visible rect of the dependency.
        final Rect rect = mTmpRect;
        ViewGroupUtils.getDescendantRect(parent, dependency, rect);
        int scrollDistance = appbarHeight - rect.bottom;
        float percCompleted = scrollDistance / appbarScrollRange;
        Log.d(TAG, "onDependentViewChanged: " + percCompleted);

        child.setAlpha(percCompleted);

        return false;
    }
}
