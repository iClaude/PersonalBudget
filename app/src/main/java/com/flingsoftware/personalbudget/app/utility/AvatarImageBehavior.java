/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app.utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.flingsoftware.personalbudget.R;
import com.flingsoftware.personalbudget.utilita.UtilityVarious;


public class AvatarImageBehavior extends CoordinatorLayout.Behavior<ImageView> {
    private static final String TAG = "AvatarImageBehavior";

    private Activity mActivity;
    private Context mContext;
    private Rect mTmpRect;
    private int appbarHeight;
    private int appbarScrollRange;
    private int toolbarHeight; // when collapsed
    // Position and size of the image.
    private float startX = 0;
    private float startY = 0;
    private float finalX = 0;
    private float finalY = 0;
    private float currentX;
    private float currentY;
    private int startSize = 0;
    private int finalSize = 0;
    private int currentSize;


    public AvatarImageBehavior() {

    }

    public AvatarImageBehavior(Activity mActivity) {
        this.mContext = mActivity;
        this.mActivity = mActivity;
    }

    public AvatarImageBehavior(Context mContext, AttributeSet attrs) {
        this.mContext = mContext;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageView child, View dependency) {
        if (dependency instanceof AppBarLayout) {
            // Action bar sizes.
            appbarHeight = dependency.getHeight();
            appbarScrollRange = ((AppBarLayout) dependency).getTotalScrollRange();
            toolbarHeight = UtilityVarious.getActionBarHeight(mContext);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageView child, View dependency) {
        // Dependency is an instance of the View to which the child is anchored (i.e. FrameLayout)
        if (!(dependency instanceof AppBarLayout)) return false;

        maybeInitProperties(parent, child);

        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }
        // First, let's get the visible rect of the dependency.
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

    private void maybeInitProperties(CoordinatorLayout parent, ImageView child) {
        if (startSize == 0) {
            startSize = child.getHeight();
        }

        if (finalSize == 0) {
            finalSize = (int) (toolbarHeight * 0.7f);
        }

        if (startX == 0) {
            startX = child.getX();
        }

        if (startY == 0) {
            startY = child.getY();
        }

        if (finalX == 0) {
            ImageView ivIconToolbar = (ImageView) parent.findViewById(R.id.ivIconToolbar);
            // Get the x, y coordinates of ivIconToolbar on the screen.
            int arrCoords[] = new int[2];
            ivIconToolbar.getLocationOnScreen(arrCoords);
            finalX = arrCoords[0];
        }

        if (finalY == 0) {
            finalY = UtilityVarious.getStatusBarHeight(mActivity) + (toolbarHeight - finalSize) / 2;
        }
    }
}
