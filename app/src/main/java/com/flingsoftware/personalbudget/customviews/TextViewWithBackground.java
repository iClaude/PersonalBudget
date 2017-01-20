/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.customviews;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TODO commentare classe
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
