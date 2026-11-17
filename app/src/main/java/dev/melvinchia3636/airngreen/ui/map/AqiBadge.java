package dev.melvinchia3636.airngreen.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Builds the coloured AQI pill used for station markers.
 */
final class AqiBadge {

    private static final float HORIZONTAL_PADDING_DP = 8f;
    private static final float VERTICAL_PADDING_DP = 5f;
    private static final float BORDER_DP = 2f;
    private static final float TEXT_SIZE_DP = 13f;

    private AqiBadge() {
    }

    static Drawable create(Context context, String label, int background, int foreground) {
        float density = context.getResources().getDisplayMetrics().density;
        float textSize = TEXT_SIZE_DP * density;
        float paddingH = HORIZONTAL_PADDING_DP * density;
        float paddingV = VERTICAL_PADDING_DP * density;
        float border = BORDER_DP * density;

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTextSize(textSize);
        text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(foreground);

        int width = Math.round(text.measureText(label) + paddingH * 2 + border * 2);
        int height = Math.round(textSize + paddingV * 2 + border * 2);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float radius = height / 2f;
        RectF outer = new RectF(0, 0, width, height);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.WHITE);
        canvas.drawRoundRect(outer, radius, radius, fill);

        RectF inner = new RectF(outer);
        inner.inset(border, border);
        fill.setColor(background);
        canvas.drawRoundRect(inner, radius, radius, fill);

        float baseline = height / 2f - (text.ascent() + text.descent()) / 2f;
        canvas.drawText(label, width / 2f, baseline, text);

        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.setBounds(0, 0, width, height);
        return drawable;
    }
}
