package com.groupone.assignment.ui.common;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.groupone.assignment.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
     * Trend variant with labelled axes and relative hour labels.
     */
    public static void showTrend(LineChart chart, List<Double> values) {
        List<Entry> entries = entries(values);
        if (entries.size() < 2) {
            clear(chart);
            return;
        }

        chart.setData(new LineData(dataSet(entries, 2f)));
        chart.notifyDataSetChanged();

        baseStyle(chart);
        chart.setMinOffset(4f);
        chart.setExtraOffsets(4f, 4f, 4f, 4f);

        int lastIndex = entries.size() - 1;
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
                int hoursAgo = Math.round(lastIndex - value);
                if (hoursAgo <= 0) {
                    return chart.getContext().getString(R.string.sparkline_now);
                }
                return chart.getContext().getString(R.string.sparkline_hours_ago, hoursAgo);
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

    private static List<Entry> entries(List<Double> values) {
        List<Entry> entries = new ArrayList<>();
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
