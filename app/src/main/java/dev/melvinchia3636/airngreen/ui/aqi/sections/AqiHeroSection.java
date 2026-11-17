package dev.melvinchia3636.airngreen.ui.aqi.sections;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.guidance.AqiLevel;
import dev.melvinchia3636.airngreen.guidance.AqiLevelStrategy;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.ui.common.AqiScale;
import dev.melvinchia3636.airngreen.ui.common.AppIcons;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;

/**
 * AQI hero card: station, reading, category and health advice.
 */
public class AqiHeroSection extends MaterialCardView {

    private final TextView stationName;
    private final TextView value;
    private final TextView category;
    private final TextView advice;
    private final ImageView faceIcon;

    public AqiHeroSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_aqi_hero_section, this, true);

        stationName = findViewById(R.id.hero_station_name);
        value = findViewById(R.id.hero_aqi_value);
        category = findViewById(R.id.hero_category);
        advice = findViewById(R.id.hero_advice);
        faceIcon = findViewById(R.id.hero_face_icon);
    }

    public void bind(StationDetail station, String fallbackName) {
        AqiLevelStrategy level = AqiLevel.classify(station.getAqi());
        int foreground = Color.parseColor(AqiScale.textColourFor(level.colourHex()));

        setCardBackgroundColor(Color.parseColor(level.colourHex()));
        stationName.setTextColor(foreground);
        value.setTextColor(foreground);
        category.setTextColor(foreground);
        advice.setTextColor(foreground);
        faceIcon.setImageDrawable(AppIcons.drawable(
                getContext(), level.icon(), foreground, 18));

        stationName.setText(station.getName().isEmpty() ? fallbackName : station.getName());
        value.setText(AqiScale.format(station.getAqi()));
        category.setText(level.labelRes());
        advice.setText(level.adviceRes());
    }

    public void showError() {
        setCardBackgroundColor(Color.parseColor("#5F665F"));
        stationName.setText("");
        value.setText(R.string.dash);
        category.setText(R.string.unavailable);
        advice.setText(R.string.connection_advice);
        faceIcon.setImageDrawable(AppIcons.drawable(
                getContext(), FontAwesome.Icon.faw_meh, "#FFFFFF", 18));
    }
}
