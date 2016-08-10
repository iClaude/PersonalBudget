/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.flingsoftware.personalbudget.R;

/**
 * TODO.
 */
public class AvatarImageBehavior extends CoordinatorLayout.Behavior<ImageView> {
    private static final String TAG = "AvatarImageBehavior";

    private Context context;
    private Rect mTmpRect;
    private int appbarHeight;
    private int appbarScrollRange;
    private float startX;
    private float startY;
    private float finalX;
    private float finalY;
    private float currentX;
    private float currentY;
    private int startSize;
    private int finalSize;
    private int currentSize;


    public AvatarImageBehavior() {

    }

    public AvatarImageBehavior(Context context, AttributeSet attrs) {
        this.context = context;


    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        if(dependency instanceof AppBarLayout) {
            appbarHeight = dependency.getHeight();
            appbarScrollRange = ((AppBarLayout) dependency).getTotalScrollRange();

            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView child, View dependency) {
        // Dependency is an instance of the View to which the child is anchored (i.e. FrameLayout)
        if(!(dependency instanceof AppBarLayout)) return false;

        maybeInitProperties(child);

        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }
        // First, let's get the visible rect of the dependency
        final Rect rect = mTmpRect;
        ViewGroupUtils.getDescendantRect(parent, dependency, rect);

        int scrollDistance = appbarHeight - rect.bottom;
        float percCompleted = ((float) scrollDistance / appbarScrollRange);

        currentX = startX - (startX - finalX) * percCompleted;
        currentY = startY - (startY - finalY) * percCompleted;
        currentSize = (int) (startSize - (startSize - finalSize) * percCompleted);

        child.setX(currentX);
        child.setY(currentY);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.width = currentSize;
        lp.height = currentSize;
        child.setLayoutParams(lp);

        return true;
    }

    private void maybeInitProperties(ImageView child) {
        if(startX == 0) {
            startX = child.getX();
        }
        if(startY == 0) {
            startY = child.getY();
        }
        if(finalX == 0) {
            finalX = context.getResources().getDimensionPixelOffset(R.dimen.abc_action_bar_content_inset_material);
        }
        if(finalY == 0) {
            finalY = 20;
        }
        if(startSize == 0) {
            startSize = child.getHeight();
        }
        if(finalSize == 0) {
            finalSize = (int) ((appbarHeight - appbarScrollRange) * 0.8f);
        }
    }
}
