package com.groupone.assignment.ui.common;

import android.content.Context;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.groupone.assignment.R;

import java.util.function.IntConsumer;

/**
 * Themed single-choice dialog used by settings screens.
 */
public final class SingleChoiceDialog {

    private SingleChoiceDialog() {
    }

    public static void show(Context context,
                            @StringRes int titleRes,
                            CharSequence[] labels,
                            int selectedIndex,
                            IntConsumer onSelected) {
        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AirGreen_Dialog)
                .setTitle(titleRes)
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                    dialog.dismiss();
                    onSelected.accept(which);
                })
                .show();
    }
}
