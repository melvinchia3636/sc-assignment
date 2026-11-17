package com.groupone.assignment.ui.aqi.sections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.groupone.assignment.R;
import com.groupone.assignment.repositories.model.SeriesPoint;
import com.groupone.assignment.repositories.model.StationDetail;
import com.groupone.assignment.ui.common.SparklineChart;

import java.util.ArrayList;
import java.util.List;

/**
 * 24-hour AQI trend section.
 */
public class TrendSection extends LinearLayout {

    private final LineChart chart;
    private final android.widget.TextView range;

    public TrendSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_trend_section, this, true);
        chart = findViewById(R.id.aqi_sparkline);
        range = findViewById(R.id.trend_range);
        SparklineChart.clear(chart);
    }

    public void bind(StationDetail station) {
        List<SeriesPoint> points = station.series("aqi");
        int from = Math.max(0, points.size() - 24);
        List<Double> recent = new ArrayList<>(station.values("aqi").subList(from, points.size()));
        long startEpoch = from < points.size() ? points.get(from).getT() : 0;
        int step = points.size() > 1
                ? (int) (points.get(1).getT() - points.get(0).getT())
                : 3600;
        SparklineChart.showTrend(chart, recent, startEpoch, step);

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        int valid = 0;

        for (double value : recent) {
            if (Double.isNaN(value)) {
                continue;
            }
            min = Math.min(min, value);
            max = Math.max(max, value);
            valid++;
        }

        if (valid < 2) {
            range.setText(R.string.not_enough_history);
            return;
        }

        range.setText(getContext().getString(R.string.aqi_range,
                Math.round(min), Math.round(max), recent.size()));
    }

    public void clear() {
        SparklineChart.clear(chart);
        range.setText("");
    }
}
