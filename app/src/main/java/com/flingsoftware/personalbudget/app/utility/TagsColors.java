/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget.app.utility;

import com.flingsoftware.personalbudget.R;

/**
 * This class generates random colors used for tags TextView(s).
 */
public class TagsColors {

    private final int[] tagsColors = {R.color.indigo_500, R.color.red_500, R.color.pink_500, R.color.purple_500, R.color.deep_purple_500, R.color.teal_500, R.color.orange_800, R.color.brown_500, R.color.green_600, R.color.cyan_900};

    private static TagsColors ourInstance = new TagsColors();

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
