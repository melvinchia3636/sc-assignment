package com.groupone.assignment.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groupone.assignment.R;

/**
 * Titled section container. XML children are appended below the title.
 */
public class SectionView extends LinearLayout {

    public SectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_section_header, this, true);

        TypedArray attributes = context.obtainStyledAttributes(
                attrs, R.styleable.SectionView);
        try {
            TextView title = findViewById(R.id.section_title);
            CharSequence text = attributes.getText(R.styleable.SectionView_sectionTitle);
            if (text != null) {
                title.setText(text);
            }
        } finally {
            attributes.recycle();
        }
    }
}
