package dev.melvinchia3636.airngreen.ui.common;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Configures an MPAndroidChart LineChart as a sparkline or a trend chart.
 */
public final class SparklineChart {

    private static final int LINE_COLOUR = Color.parseColor("#1B5E20");
    private static final int AXIS_COLOUR = Color.parseColor("#5F665F");
    private static final int GRID_COLOUR = Color.parseColor("#E0E4DE");

    /** WAQI caps the AQI scale at 500, so the trend axis never goes above it. */
    private static final float AQI_MAXIMUM = 500f;
    /** Smallest trend window, so a flat low reading is not blown up. */
    private static final float AQI_MINIMUM_SPAN = 50f;

    private SparklineChart() {
    }

    public static void show(LineChart chart, List<Double> values, boolean compact) {
        List<Entry> entries = entries(values);
        if (entries.size() < 2) {
            clear(chart);
            return;
        }

        chart.setData(new LineData(dataSet(entries, compact ? 1.3f : 2.5f)));
        chart.notifyDataSetChanged();

        baseStyle(chart);
        chart.setMinOffset(0f);
        chart.setExtraOffsets(2f, 2f, 2f, 2f);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    /**
     * Trend variant with labelled axes; X labels are clock times in the
     * device's timezone.
     */
    public static void showTrend(LineChart chart, List<Double> values,
                                 long startEpochSeconds, int stepSeconds) {
        List<Entry> entries = indexedEntries(values);
        if (entries.size() < 2) {
            clear(chart);
            return;
        }

        chart.setData(new LineData(dataSet(entries, 2f)));
        chart.notifyDataSetChanged();

        baseStyle(chart);
        chart.setMinOffset(4f);
        chart.setExtraOffsets(4f, 4f, 4f, 4f);

        int lastIndex = Math.max(1, values.size() - 1);
        int step = Math.max(1, stepSeconds);
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
                .withZone(ZoneId.systemDefault());
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(AXIS_COLOUR);
        xAxis.setTextSize(9f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(GRID_COLOUR);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(4);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(lastIndex);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long epochSeconds = startEpochSeconds + Math.round(value) * (long) step;
                return timeFormat.format(Instant.ofEpochSecond(epochSeconds));
            }
        });

        YAxis left = chart.getAxisLeft();
        left.setEnabled(true);
        left.setTextColor(AXIS_COLOUR);
        left.setTextSize(9f);
        left.setDrawAxisLine(false);
        left.setDrawGridLines(true);
        left.setGridColor(GRID_COLOUR);
        left.setGridLineWidth(0.5f);
        left.setLabelCount(4);
        left.setAxisMinimum(0f);
        left.setAxisMaximum(trendMaximum(entries));
        left.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    public static void clear(LineChart chart) {
        baseStyle(chart);
        chart.clear();
        chart.invalidate();
    }

    /**
     * Rounds the trend ceiling up to a readable step with a little headroom,
     * keeping it within the AQI scale and never below {@link #AQI_MINIMUM_SPAN}.
     */
    private static float trendMaximum(List<Entry> entries) {
        float highest = 0f;
        for (Entry entry : entries) {
            highest = Math.max(highest, entry.getY());
        }

        float padded = (float) Math.ceil(highest * 1.15f / 10f) * 10f;
        return Math.min(AQI_MAXIMUM, Math.max(AQI_MINIMUM_SPAN, padded));
    }

    private static List<Entry> entries(List<Double> values) {        List<Entry> entries = new ArrayList<>();
        int index = 0;
        if (values != null) {
            for (Double value : values) {
                if (value == null || value.isNaN()) {
                    continue;
                }
                entries.add(new Entry(index++, value.floatValue()));
            }
        }
        return entries;
    }

    private static List<Entry> indexedEntries(List<Double> values) {
        List<Entry> entries = new ArrayList<>();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                Double value = values.get(i);
                if (value != null && !value.isNaN()) {
                    entries.add(new Entry(i, value.floatValue()));
                }
            }
        }
        return entries;
    }

    private static LineDataSet dataSet(List<Entry> entries, float lineWidth) {
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(LINE_COLOUR);
        dataSet.setLineWidth(lineWidth);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x331B5E20, 0x001B5E20}));
        return dataSet;
    }

    private static void baseStyle(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setNoDataText("");
        chart.setBackgroundColor(Color.TRANSPARENT);
    }
}
