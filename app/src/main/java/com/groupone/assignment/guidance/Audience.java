package com.groupone.assignment.guidance;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;

/**
 * Audiences with their own guidance and exposure recommendations.
 */
public enum Audience implements AudienceStrategy {

    GENERAL(R.string.audience_general,
            new int[]{},
            new int[]{120, 60, 30, 0}),
    CHILDREN(R.string.audience_children,
            new int[]{
                    R.string.audience_note_children_good,
                    R.string.audience_note_children_fair,
                    R.string.audience_note_children_poor,
            },
            new int[]{90, 30, 15, 0}),
    ELDERLY(R.string.audience_elderly,
            new int[]{
                    R.string.audience_note_elderly_good,
                    R.string.audience_note_elderly_fair,
                    R.string.audience_note_elderly_poor,
            },
            new int[]{60, 30, 0, 0});

    @StringRes
    private final int labelRes;
    private final int[] noteTiers;
    private final int[] minuteTiers;

    Audience(@StringRes int labelRes, int[] noteTiers, int[] minuteTiers) {
        this.labelRes = labelRes;
        this.noteTiers = noteTiers;
        this.minuteTiers = minuteTiers;
    }

    @Override
    public int labelRes() {
        return labelRes;
    }

    @Override
    public int noteRes(double aqi) {
        return noteTiers.length == 0 ? 0 : tier(aqi, noteTiers);
    }

    @Override
    public int outdoorMinutes(double aqi) {
        return tier(aqi, minuteTiers);
    }

    private static int tier(double aqi, int[] values) {
        int index;
        if (aqi <= 50) {
            index = 0;
        } else if (aqi <= 100) {
            index = 1;
        } else if (aqi <= 150) {
            index = 2;
        } else {
            index = 3;
        }
        return values[Math.min(index, values.length - 1)];
    }
}
