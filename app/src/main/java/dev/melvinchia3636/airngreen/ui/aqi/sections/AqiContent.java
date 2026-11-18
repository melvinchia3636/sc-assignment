package dev.melvinchia3636.airngreen.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.model.Audience;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;

/**
 * AQI screen body: composes the header and every reading section, and owns
 * the transition between a loaded station and the error state.
 */
public class AqiContent extends LinearLayout {

    private final HeaderSection header;
    private final AqiHeroSection hero;
    private final GuidanceSection guidance;
    private final ErrorCard error;
    private final SubIndicesSection subIndices;
    private final WeatherSection weather;
    private final TrendSection trend;

    private String placeName = "";

    public AqiContent(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_aqi_content, this, true);

        header = findViewById(R.id.header_section);
        hero = findViewById(R.id.aqi_hero);
        guidance = findViewById(R.id.guidance_section);
        error = findViewById(R.id.error_card);
        subIndices = findViewById(R.id.sub_indices_section);
        weather = findViewById(R.id.weather_section);
        trend = findViewById(R.id.trend_section);
    }

    public void setOnRetryListener(Runnable action) {
        error.setOnRetryListener(action);
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName == null ? "" : placeName;
        header.setPlaceName(this.placeName);
    }

    public void bind(StationDetail station, Audience audience) {
        hero.bind(station, placeName);
        guidance.bind(audience, station.getAqi());
        header.bind(station);
        subIndices.bind(station);
        weather.bind(station);
        trend.bind(station);

        error.hide();
    }

    public void bindAudience(Audience audience) {
        guidance.bindAudience(audience);
    }

    public void showError(String message) {
        error.show(message);
        hero.showError();
        guidance.clear();
        weather.clear();
        trend.clear();
        subIndices.clear();
        header.setUnavailable();
    }
}
