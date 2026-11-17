package com.groupone.assignment.guidance;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;

/**
 * Audiences with their own guidance and exposure recommendations.
 */
public enum Audience implements AudienceStrategy {

    GENERAL(R.string.audience_general) {
        @Override
        public int noteRes(double aqi) {
            return 0;
        }

        @Override
        public int outdoorMinutes(double aqi) {
            if (aqi <= 50) {
                return 120;
            }
            if (aqi <= 100) {
                return 60;
            }
            if (aqi <= 150) {
                return 30;
            }
            return 0;
        }
    },

    CHILDREN(R.string.audience_children) {
        @Override
        public int noteRes(double aqi) {
            if (aqi <= 50) {
                return R.string.audience_note_children_good;
            }
            if (aqi <= 100) {
                return R.string.audience_note_children_fair;
            }
            return R.string.audience_note_children_poor;
        }

        @Override
        public int outdoorMinutes(double aqi) {
            if (aqi <= 50) {
                return 90;
            }
            if (aqi <= 100) {
                return 30;
            }
            if (aqi <= 150) {
                return 15;
            }
            return 0;
        }
    },

    ELDERLY(R.string.audience_elderly) {
        @Override
        public int noteRes(double aqi) {
            if (aqi <= 50) {
                return R.string.audience_note_elderly_good;
            }
            if (aqi <= 100) {
                return R.string.audience_note_elderly_fair;
            }
            return R.string.audience_note_elderly_poor;
        }

        @Override
        public int outdoorMinutes(double aqi) {
            if (aqi <= 50) {
                return 60;
            }
            if (aqi <= 100) {
                return 30;
            }
            return 0;
        }
    };

    @StringRes
    private final int labelRes;

    Audience(@StringRes int labelRes) {
        this.labelRes = labelRes;
    }

    @Override
    public int labelRes() {
        return labelRes;
    }
}
