/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.utility;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * This class represents a behavior for the FloatingActionButton. The FAB is connected to the
 * AppBarLayout. This class tells when the AppBarLayout is collapsed.
 * Subclasses can hide/show the FAB accordingly.
 */
public class FABShowHideBehavior extends FloatingActionButton.Behavior {
    private Rect mTmpRect;

    public FABShowHideBehavior() {

    }

    public FABShowHideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    public boolean isAppBarcollapsed(CoordinatorLayout parent, AppBarLayout appBarLayout) {
        if (mTmpRect == null) {
            mTmpRect = new Rect();
        }

        // First, let's get the visible rect of the dependency
        final Rect rect = mTmpRect;
        ViewGroupUtils.getDescendantRect(parent, appBarLayout, rect);

        if (rect.bottom <= getMinimumHeightForVisibleOverlappingContent(appBarLayout)) {
            return true;
        } else {
            return false;
        }
    }

    /*
    Method getMinimumHeightForVisibleOverlappingContent of the AppBarLayout class, which is not
    public. So you have to use reflection.
     */
    private int getMinimumHeightForVisibleOverlappingContent(AppBarLayout appBarLayout) {
        try {
            Method method = appBarLayout.getClass().getDeclaredMethod("getMinimumHeightForVisibleOverlappingContent");
            method.setAccessible(true);
            Object value = method.invoke(appBarLayout, null);
            return (int) value;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
