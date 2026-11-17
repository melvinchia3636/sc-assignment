package com.groupone.assignment.ui.map;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import com.groupone.assignment.R;
import com.groupone.assignment.guidance.AqiLevel;
import com.groupone.assignment.repositories.model.Station;
import com.groupone.assignment.ui.aqi.AqiScale;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * Compact station card shown when a map marker is tapped.
 */
public class StationInfoWindow extends InfoWindow {

    public StationInfoWindow(MapView mapView) {
        super(R.layout.view_station_info_window, mapView);
    }

    @Override
    public void onOpen(Object item) {
        InfoWindow.closeAllInfoWindowsOn(mMapView);

        Marker marker = (Marker) item;
        Station station = (Station) marker.getRelatedObject();
        if (station == null) {
            return;
        }

        AqiLevel level = AqiLevel.classify(station.getAqi());
        TextView name = mView.findViewById(R.id.info_name);
        TextView aqi = mView.findViewById(R.id.info_aqi);
        TextView category = mView.findViewById(R.id.info_category);

        name.setText(station.getName().isEmpty() ? station.getIdx() : station.getName());
        aqi.setText(AqiScale.format(station.getAqi()));
        aqi.setTextColor(Color.parseColor(AqiScale.textColourFor(level.getColourHex())));
        aqi.setBackground(pill(Color.parseColor(level.getColourHex())));
        category.setText(level.getLabelRes());
    }

    private GradientDrawable pill(int color) {
        float density = mView.getResources().getDisplayMetrics().density;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(7 * density);
        return drawable;
    }

    @Override
    public void onClose() {
        // Nothing to release.
    }
}
