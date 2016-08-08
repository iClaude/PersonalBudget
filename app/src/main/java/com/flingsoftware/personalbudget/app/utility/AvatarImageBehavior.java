/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.utility;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Claudio on 08/08/2016.
 */
public class AvatarImageBehavior extends CoordinatorLayout.Behavior<ImageView> {
    private static final String TAG = "AvatarImageBehavior";

    private Context context;


    public AvatarImageBehavior() {

    }

    public AvatarImageBehavior(Context context, AttributeSet attrs) {
        this.context = context;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView child, View dependency) {
        if(dependency instanceof AppBarLayout) {
            Log.d(TAG, "y = " + dependency.getY());
            Log.d(TAG, "scroll range = " + ((AppBarLayout) dependency).getTotalScrollRange());
        }
        return false;
    }
}
