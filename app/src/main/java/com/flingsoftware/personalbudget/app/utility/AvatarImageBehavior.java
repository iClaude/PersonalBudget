/*
 * Copyright (c) - Software developed by iClaude.
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

/*
    When collapsing the app bar the icon moves to to toolbar.
    The animation is divided into 2 parts (equal):
    1) the icon goes up (y) to the final y position
    2) the icon goes left (x) and shrinks (size) to its final position
    and viceversa
 */
public class AvatarImageBehavior extends CoordinatorLayout.Behavior<ImageView> {
    private static final String TAG = "AvatarImageBehavior";
    private static final float HALF_ANIM_HEIGHT = 2; // change this to have a different speed

    private Activity mActivity;
    private Context mContext;
    private Rect mTmpRect;
    private int appbarHeight;
    private int appbarScrollRange;
    private int toolbarHeight; // when collapsed
    // Position and size of the image.
    private float startX;
    private float startY;
    private float finalX;
    private float finalY;
    private float currentX;
    private float currentY;
    private float treshold;
    private float startSize;
    private float finalSize;
    private float currentSize;
    private float scale;


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
            /*
                treshold = 0 - treshold: the icon goes up
                           treshold - total scroll range: the icon moves left and shrinks
             */
            treshold = appbarScrollRange / HALF_ANIM_HEIGHT;

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

        float percCompleted;
        if (scrollDistance <= treshold) { // icon moves up/down without changing size
            currentX = startX;
            scale = 1f;

            percCompleted = scrollDistance / treshold;
            currentY = startY - (startY - finalY) * percCompleted;
            if (currentY > startY) currentY = startY;
        }
        else { // icon moves left/right and shrinks/expands
            currentY = finalY;

            percCompleted = (scrollDistance - treshold) / (appbarScrollRange - treshold);
            currentX = startX - (startX - finalX) * percCompleted;

            currentSize = (int) (startSize - (startSize - finalSize) * percCompleted);
            scale = currentSize / startSize;
        }

        // Move the View.
        child.setX(currentX);
        child.setY(currentY);

        // Resize the image.
        child.setPivotX(0f);
        child.setPivotY(0f);
        child.setScaleX(scale);
        child.setScaleY(scale);

        return true;
    }

    private void maybeInitProperties(CoordinatorLayout parent, ImageView child) {
        if (startSize == 0) {
            startSize = child.getHeight();
            currentSize = startSize;
            scale = 1f;
        }

        if (finalSize == 0) {
            finalSize = (int) (toolbarHeight * 0.7f);
        }

        if (startX == 0) {
            startX = child.getX();
            currentX = startX;
        }

        if (startY == 0) {
            startY = child.getY();
            currentY = startY;
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
