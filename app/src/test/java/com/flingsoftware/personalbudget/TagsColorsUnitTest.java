/*
 * Copyright (c) - Software developed by iClaude.
 */

package com.flingsoftware.personalbudget;

import com.flingsoftware.personalbudget.app.utility.TagsColors;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for TagsColors class.
 */

public class TagsColorsUnitTest {
    private static TagsColors tagsColors;
    private static final Integer[] tagsColorsArray = {R.color.tag_color_01, R.color.tag_color_03, R.color.tag_color_02, R.color.tag_color_04, R.color.tag_color_05, R.color.tag_color_06, R.color.tag_color_07, R.color.tag_color_08, R.color.tag_color_09, R.color.tag_color_10};
    private static List<Integer> lstTags;

    @BeforeClass
    public static void setupTagsColors() throws Exception {
        tagsColors = TagsColors.getInstance();
        lstTags = Arrays.asList(tagsColorsArray);
    }

    @Test
    public void testGetRandomColor() throws Exception {
        TagsColors tagsColors2;

        for (int i = 0; i < 20; i++) {
            tagsColors2 = TagsColors.getInstance();
            assertThat(tagsColors2, sameInstance(tagsColors));

            Integer color = tagsColors2.getRandomColor(i);
            assertThat(lstTags, hasItem(color));
        }
    }
}
