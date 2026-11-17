package com.groupone.assignment.repositories.model;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;

/**
 * Audience used to tailor air-quality guidance.
 */
public enum Audience {

    GENERAL(R.string.audience_general, 0),
    CHILDREN(R.string.audience_children, R.string.audience_note_children),
    ELDERLY(R.string.audience_elderly, R.string.audience_note_elderly);

    private final int labelRes;
    private final int noteRes;

    Audience(@StringRes int labelRes, @StringRes int noteRes) {
        this.labelRes = labelRes;
        this.noteRes = noteRes;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    /** Returns 0 when the audience needs no extra guidance. */
    @StringRes
    public int getNoteRes() {
        return noteRes;
    }
}
