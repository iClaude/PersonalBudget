/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.customviews;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * This class wraps a TextView with a custom background (for example, a rectangle with rounded
 * corners).
 * It exposes a method, setBackgroundColorPreserveBackground, that changes the background color
 * of the TextView while preserving the shape of it.
 */

public class TextViewWithBackground extends TextView {
    public TextViewWithBackground(Context context) {
        super(context);
    }

    public TextViewWithBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBackgroundColorPreserveBackground(int color) {
        GradientDrawable bgShape = (GradientDrawable) getBackground();
        bgShape.setColor(color);
        invalidate();
        requestLayout();
    }
}
