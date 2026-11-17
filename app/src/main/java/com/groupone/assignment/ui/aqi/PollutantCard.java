package com.groupone.assignment.ui.aqi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.card.MaterialCardView;
import com.groupone.assignment.R;
import com.groupone.assignment.ui.common.SparklineChart;

import java.util.ArrayList;
import java.util.List;

/**
 * One pollutant sub-index: value, label and a 24-hour mini trend.
 */
public class PollutantCard extends MaterialCardView {

    private final TextView value;
    private final TextView label;
    private final TextView maximum;
    private final TextView minimum;
    private final LineChart chart;

    public PollutantCard(Context context) {
        this(context, null);
    }

    public PollutantCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PollutantCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_pollutant, this, true);
        value = findViewById(R.id.pollutant_value);
        label = findViewById(R.id.pollutant_label);
        maximum = findViewById(R.id.pollutant_max);
        minimum = findViewById(R.id.pollutant_min);
        chart = findViewById(R.id.pollutant_chart);
        SparklineChart.clear(chart);
    }

    public void bind(String name, double reading, List<Double> series) {
        label.setText(name);
        value.setText(AqiScale.format(reading));

        int from = Math.max(0, series.size() - 24);
        List<Double> recent = new ArrayList<>(series.subList(from, series.size()));
        double high = Double.NaN;
        double low = Double.NaN;
        for (double sample : recent) {
            if (Double.isNaN(sample)) {
                continue;
            }
            high = Double.isNaN(high) ? sample : Math.max(high, sample);
            low = Double.isNaN(low) ? sample : Math.min(low, sample);
        }
        maximum.setText(AqiScale.format(high));
        minimum.setText(AqiScale.format(low));
        SparklineChart.show(chart, recent, true);
    }

    public void clear() {
        value.setText(R.string.dash);
        maximum.setText(R.string.dash);
        minimum.setText(R.string.dash);
        SparklineChart.clear(chart);
    }
}
