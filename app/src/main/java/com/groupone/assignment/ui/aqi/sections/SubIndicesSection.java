package com.groupone.assignment.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.aqi.PollutantCard;

/**
 * Pollutant sub-index grid.
 */
public class SubIndicesSection extends LinearLayout {

    private static final String[] KEYS = { "pm25", "pm10", "no2", "o3", "so2", "co" };
    private static final String[] NAMES = { "PM2.5", "PM10", "NO₂", "O₃", "SO₂", "CO" };

    private final PollutantCard[] cards;

    public SubIndicesSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_sub_indices_section, this, true);
        cards = new PollutantCard[]{
                findViewById(R.id.pm25_card),
                findViewById(R.id.pm10_card),
                findViewById(R.id.no2_card),
                findViewById(R.id.o3_card),
                findViewById(R.id.so2_card),
                findViewById(R.id.co_card),
        };
    }

    public void bind(StationDetail station) {
        for (int index = 0; index < cards.length; index++) {
            String key = KEYS[index];
            cards[index].bind(NAMES[index], station.current(key), station.values(key));
        }
    }

    public void clear() {
        for (PollutantCard card : cards) {
            card.clear();
        }
    }
}
