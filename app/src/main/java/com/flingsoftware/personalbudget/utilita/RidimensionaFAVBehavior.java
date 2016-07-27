/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.utilita;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.flingsoftware.personalbudget.R;

/**
 * Created by Claudio on 27/07/2016.
 */
public class RidimensionaFAVBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    private static final String TAG = "RidimensionaFAVBehavior";
    private int mActionBarSize;

    public RidimensionaFAVBehavior() { }

    public RidimensionaFAVBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        float y = dependency.getY();

        if(y == 0 && (child.getVisibility() == View.INVISIBLE || child.getVisibility() ==View.GONE)) {
            child.show();
        }
        else if(y < (-mActionBarSize * 0.9) && child.getVisibility() == View.VISIBLE) {
            FloatingActionButton fabFav = (FloatingActionButton) parent.findViewById(R.id.fabFav);
            FloatingActionButton fabMic = (FloatingActionButton) parent.findViewById(R.id.fabMic);
            FloatingActionButton fabAgg = (FloatingActionButton) parent.findViewById(R.id.fabAgg);

            child.hide();
            if(fabFav.getVisibility() == View.VISIBLE) {
                fabFav.setVisibility(View.GONE);
                fabMic.setVisibility(View.GONE);
                fabAgg.setVisibility(View.GONE);
            }
         }

        return false;
    }
}
