package dev.melvinchia3636.airngreen.ui.map;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.policy.AqiLevel;
import dev.melvinchia3636.airngreen.repositories.strategies.AqiLevelStrategy;
import dev.melvinchia3636.airngreen.repositories.model.Station;
import dev.melvinchia3636.airngreen.ui.common.AqiScale;

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

        AqiLevelStrategy level = AqiLevel.classify(station.getAqi());
        TextView name = mView.findViewById(R.id.info_name);
        TextView aqi = mView.findViewById(R.id.info_aqi);
        TextView category = mView.findViewById(R.id.info_category);

        name.setText(station.getName().isEmpty() ? station.getIdx() : station.getName());
        aqi.setText(AqiScale.format(station.getAqi()));
        aqi.setTextColor(Color.parseColor(AqiScale.textColourFor(level.colourHex())));
        aqi.setBackground(pill(Color.parseColor(level.colourHex())));
        category.setText(level.labelRes());
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
