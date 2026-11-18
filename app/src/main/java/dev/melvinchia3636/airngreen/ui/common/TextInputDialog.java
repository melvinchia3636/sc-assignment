package dev.melvinchia3636.airngreen.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dev.melvinchia3636.airngreen.R;

import java.util.function.Consumer;

/**
 * Themed dialog with a single text field, used for secrets such as API keys.
 */
public final class TextInputDialog {

    private TextInputDialog() {
    }

    public static void show(Context context,
                            @StringRes int titleRes,
                            @StringRes int hintRes,
                            String initialValue,
                            Consumer<String> onSave) {
        View content = LayoutInflater.from(context)
                .inflate(R.layout.view_text_input, null);
        TextInputLayout layout = content.findViewById(R.id.text_input_layout);
        layout.setHint(context.getString(hintRes));
        TextInputEditText input = content.findViewById(R.id.text_input);
        input.setText(initialValue);
        if (initialValue != null && !initialValue.isEmpty()) {
            input.setSelection(initialValue.length());
        }

        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_AirGreen_Dialog)
                .setTitle(titleRes)
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, (dialog, which) ->
                        onSave.accept(String.valueOf(input.getText())))
                .show();
    }
}
