package com.groupone.assignment.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.groupone.assignment.repositories.AirQualityRepository;
import com.groupone.assignment.services.AirQualityService;
import com.groupone.assignment.services.LocationService;
import com.groupone.assignment.services.StationsService;

/**
 * Coordinates the activity-scoped services and exposes their state to tabs.
 */
public class MainViewModel extends AndroidViewModel {

    private final AirQualityService airQuality;
    private final StationsService stations;
    private final LocationService location;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AirQualityRepository repository = new AirQualityRepository(application);
        airQuality = new AirQualityService(application, repository);
        stations = new StationsService(repository);
        location = new LocationService(application);
    }

    public LocationService getLocationService() {
        return location;
    }

    public AirQualityService airQuality() {
        return airQuality;
    }

    public StationsService stations() {
        return stations;
    }

    public void onLocationChanged() {
        startLoad();
    }

    public void startLocationLookup() {
        airQuality.beginLoading();
    }

    public void refresh() {
        startLoad();
    }

    private void startLoad() {
        airQuality.refresh(location.getLatitude(), location.getLongitude());
        stations.load(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onCleared() {
        airQuality.shutdown();
        stations.shutdown();
        location.shutdown();
        super.onCleared();
    }
}
