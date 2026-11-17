package com.groupone.assignment.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

/**
 * Builds tinted, correctly sized Iconics drawables.
 */
public final class AppIcons {

    private AppIcons() {
    }

    public static Drawable drawable(Context context, IIcon icon, int colour, int sizeDp) {
        IconicsDrawable drawable = new IconicsDrawable(context, icon);
        drawable.setTint(ColorStateList.valueOf(colour));
        int sizePx = Math.round(sizeDp * context.getResources().getDisplayMetrics().density);
        drawable.setSizeXPx(sizePx);
        drawable.setSizeYPx(sizePx);
        return drawable;
    }

    public static Drawable drawable(Context context, IIcon icon, String colourHex, int sizeDp) {
        return drawable(context, icon, Color.parseColor(colourHex), sizeDp);
    }
}
