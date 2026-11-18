package dev.melvinchia3636.airngreen.ui.map;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.databinding.TabStationMapBinding;
import dev.melvinchia3636.airngreen.repositories.ApiException;
import dev.melvinchia3636.airngreen.repositories.model.AqiLevel;
import dev.melvinchia3636.airngreen.repositories.model.MapProvider;
import dev.melvinchia3636.airngreen.repositories.model.Station;
import dev.melvinchia3636.airngreen.repositories.remote.GoogleTilesRepository;
import dev.melvinchia3636.airngreen.services.ApiKeyStore;
import dev.melvinchia3636.airngreen.services.AppSigning;
import dev.melvinchia3636.airngreen.services.MapProviderService;
import dev.melvinchia3636.airngreen.strategies.AqiLevelStrategy;
import dev.melvinchia3636.airngreen.ui.MainViewModel;
import dev.melvinchia3636.airngreen.ui.common.AqiScale;
import dev.melvinchia3636.airngreen.ui.common.AppIcons;
import dev.melvinchia3636.airngreen.ui.common.BaseTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Map view of every nearby WAQI station, labelled with its AQI. Uses
 * OpenStreetMap or Google tiles depending on the user's setting.
 */
public class StationMapTab extends BaseTab<TabStationMapBinding> {

    public static final GoogleMaterial.Icon NAV_ICON = GoogleMaterial.Icon.gmd_map;

    private ExecutorService tileExecutor;

    private MainViewModel viewModel;
    private MapView map;

    @Override
    protected TabStationMapBinding createBinding(LayoutInflater inflater,
                                                      ViewGroup container) {
        return TabStationMapBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = sharedViewModel();
        map = binding.mapView;
        tileExecutor = Executors.newSingleThreadExecutor();

        boolean nightMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        map.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface));
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        configureTiles(nightMode);
        map.getController().setZoom(11.0);
        map.getController().setCenter(
                new GeoPoint(viewModel.getLocationService().getLatitude(),
                        viewModel.getLocationService().getLongitude()));

        int zoomIconColour = ContextCompat.getColor(requireContext(), R.color.ink);
        binding.mapZoomIn.setImageDrawable(AppIcons.drawable(
                requireContext(), GoogleMaterial.Icon.gmd_add, zoomIconColour, 18));
        binding.mapZoomOut.setImageDrawable(AppIcons.drawable(
                requireContext(), GoogleMaterial.Icon.gmd_remove, zoomIconColour, 18));
        binding.mapZoomIn.setOnClickListener(click -> map.getController().zoomIn());
        binding.mapZoomOut.setOnClickListener(click -> map.getController().zoomOut());

        viewModel.stations().getStations().observe(getViewLifecycleOwner(), this::render);
        viewModel.stations().getLoading().observe(getViewLifecycleOwner(),
                loading -> binding.mapLoading.setVisibility(
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

    private void configureTiles(boolean nightMode) {
        MapProvider provider = MapProviderService.INSTANCE.current(requireContext());
        if (provider != MapProvider.GOOGLE_MAPS) {
            map.setTileSource(nightMode ? MapTiles.DARK : MapTiles.LIGHT);
            binding.mapAttribution.setVisibility(View.GONE);
            return;
        }

        String apiKey = ApiKeyStore.googleMapsKey(requireContext());
        if (apiKey.isEmpty()) {
            map.setVisibility(View.GONE);
            binding.mapAttribution.setVisibility(View.GONE);
            binding.mapNotice.setText(R.string.map_google_key_missing);
            binding.mapNotice.setVisibility(View.VISIBLE);
            return;
        }

        GoogleTileSource tiles = new GoogleTileSource(apiKey);
        map.setTileSource(tiles);
        binding.mapNotice.setVisibility(View.GONE);
        binding.mapAttribution.setVisibility(View.VISIBLE);
        loadGoogleSession(tiles, apiKey, nightMode);
    }

    private void loadGoogleSession(GoogleTileSource tiles, String apiKey, boolean nightMode) {
        String styleJson = nightMode ? MapStyles.night(requireContext()) : null;
        binding.mapLoading.setVisibility(View.VISIBLE);
        tileExecutor.execute(() -> {
            try {
                GoogleTilesRepository.Session session =
                        GoogleTilesRepository.createSession(apiKey,
                                AppSigning.googleKeyHeaders(requireContext()), styleJson);
                postToMap(() -> {
                    tiles.setSession(session.token());
                    binding.mapLoading.setVisibility(View.GONE);
                    map.invalidate();
                });
            } catch (ApiException failure) {
                postToMap(() -> {
                    binding.mapLoading.setVisibility(View.GONE);
                    binding.mapNotice.setText(detailMessage(failure));
                    binding.mapNotice.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void postToMap(Runnable action) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(() -> {
            if (isAdded() && map != null) {
                action.run();
            }
        });
    }

    private String detailMessage(ApiException failure) {
        String message = failure.message(requireContext());
        String detail = failure.getDetail();
        return detail == null || detail.isEmpty() ? message : message + "\n" + detail;
    }

    private Marker markerFor(Station station) {
        AqiLevelStrategy level = AqiLevel.classify(station.getAqi());
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(station.getLatitude(), station.getLongitude()));
        marker.setTitle(station.getName().isEmpty() ? station.getIdx() : station.getName());
        marker.setRelatedObject(station);
        marker.setIcon(AqiBadge.create(requireContext(), AqiScale.format(station.getAqi()),
                Color.parseColor(level.colourHex()),
                Color.parseColor(AqiScale.textColourFor(level.colourHex()))));
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
        if (tileExecutor != null) {
            tileExecutor.shutdownNow();
            tileExecutor = null;
        }
        if (map != null) {
            map.onDetach();
            map = null;
        }
        super.onDestroyView();
    }
}
