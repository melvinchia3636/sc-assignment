package com.groupone.assignment.ui.map;

import android.view.View;
import android.widget.TextView;

import com.groupone.assignment.R;

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
        TextView name = mView.findViewById(R.id.info_name);
        TextView details = mView.findViewById(R.id.info_details);
        name.setText(marker.getTitle());
        details.setText(marker.getSnippet());
    }

    @Override
    public void onClose() {
        // Nothing to release.
    }
}
