package com.groupone.assignment.ui.map;

import android.os.Bundle;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.groupone.assignment.R;import com.groupone.assignment.databinding.FragmentStationMapBinding;
import com.groupone.assignment.repositories.model.Station;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.aqi.AqiScale;
import com.groupone.assignment.ui.common.AppIcons;
import com.groupone.assignment.ui.common.BaseTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

/**
 * OpenStreetMap view of every nearby WAQI station, labelled with its AQI.
 */
public class StationMapTab extends BaseTab<FragmentStationMapBinding> {

    private MainViewModel viewModel;
    private MapView map;

    @Override
    protected FragmentStationMapBinding createBinding(LayoutInflater inflater,
                                                      ViewGroup container) {
        return FragmentStationMapBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = sharedViewModel();
        map = binding.mapView;

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.getController().setZoom(11.0);
        map.getController().setCenter(
                new GeoPoint(viewModel.getLatitude(), viewModel.getLongitude()));

        binding.mapZoomIn.setImageDrawable(AppIcons.drawable(
                requireContext(), GoogleMaterial.Icon.gmd_add, "#1A1C19", 22));
        binding.mapZoomOut.setImageDrawable(AppIcons.drawable(
                requireContext(), GoogleMaterial.Icon.gmd_remove, "#1A1C19", 22));
        binding.mapZoomIn.setOnClickListener(click -> map.getController().zoomIn());
        binding.mapZoomOut.setOnClickListener(click -> map.getController().zoomOut());

        observeStations(this::render);
        observeStationsLoading(loading -> binding.mapLoading.setVisibility(
                Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE));
    }

    private void render(List<Station> stations) {
        if (map == null) {
            return;
        }
        map.getOverlays().clear();
        for (Station station : stations) {
            map.getOverlays().add(markerFor(station));
        }

        binding.mapCount.setText(stations.isEmpty()
                ? getString(R.string.map_empty)
                : getString(R.string.map_station_count, stations.size()));
        map.invalidate();
    }

    private Marker markerFor(Station station) {
        AqiScale.Level level = AqiScale.classify(station.getAqi());
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(station.getLatitude(), station.getLongitude()));
        marker.setTitle(station.getName().isEmpty() ? station.getIdx() : station.getName());
        marker.setSnippet(getString(R.string.map_station_snippet,
                AqiScale.format(station.getAqi()), station.getIdx()));
        marker.setIcon(AqiBadge.create(requireContext(), AqiScale.format(station.getAqi()),
                Color.parseColor(level.getColourHex()),
                Color.parseColor(level.getTextColourHex())));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setInfoWindow(new StationInfoWindow(map));
        return marker;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onPause() {
        if (map != null) {
            map.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (map != null) {
            map.onDetach();
            map = null;
        }
        super.onDestroyView();
    }
}
