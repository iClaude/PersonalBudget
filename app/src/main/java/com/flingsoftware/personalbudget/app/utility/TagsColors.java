/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.app.utility;

import com.flingsoftware.personalbudget.R;

/**
 * This class generates random colors used for tags TextView(s).
 */
public class TagsColors {

    private final int[] tagsColors = {R.color.tag_color_01, R.color.tag_color_03, R.color.tag_color_02, R.color.tag_color_04, R.color.tag_color_05, R.color.tag_color_06, R.color.tag_color_07, R.color.tag_color_08, R.color.tag_color_09, R.color.tag_color_10};

    private static final TagsColors ourInstance = new TagsColors();

    public static TagsColors getInstance() {
        return ourInstance;
    }

    private TagsColors() {
    }

    /*
        Given and index i (i.e. ListView position) returns a color randomly selected from
        the array tagsColors.
     */
    public int getRandomColor(int i) {
        return tagsColors[i % tagsColors.length];
    }
}
