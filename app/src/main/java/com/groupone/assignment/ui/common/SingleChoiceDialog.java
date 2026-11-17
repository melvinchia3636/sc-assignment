package com.groupone.assignment.ui.common;

import android.content.Context;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.groupone.assignment.R;

import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Themed single-choice dialog used by settings screens.
 */
public final class SingleChoiceDialog {

    private SingleChoiceDialog() {
    }

    public static <T> void show(Context context,
                                @StringRes int titleRes,
                                T[] options,
                                ToIntFunction<T> labelRes,
                                int selectedIndex,
                                Consumer<T> onSelected) {
        CharSequence[] labels = new CharSequence[options.length];
        for (int index = 0; index < options.length; index++) {
            labels[index] = context.getString(labelRes.applyAsInt(options[index]));
        }

        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AirGreen_Dialog)
                .setTitle(titleRes)
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                    dialog.dismiss();
                    onSelected.accept(options[which]);
                })
                .show();
    }
}
