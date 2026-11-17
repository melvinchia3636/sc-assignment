package dev.melvinchia3636.airngreen.services;

import android.content.Context;

/**
 * A user-selectable preference with a fixed set of options.
 */
public abstract class ChoiceService<T> {

    public abstract T[] options();

    public abstract T current(Context context);

    public abstract void apply(Context context, T value);

    public abstract int labelRes(T value);

    public int indexOf(T value) {
        T[] options = options();
        for (int index = 0; index < options.length; index++) {
            if (options[index] == value) {
                return index;
            }
        }
        return 0;
    }
}
