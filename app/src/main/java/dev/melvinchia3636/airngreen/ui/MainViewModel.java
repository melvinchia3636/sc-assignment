package dev.melvinchia3636.airngreen.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import dev.melvinchia3636.airngreen.repositories.AirQualityRepository;
import dev.melvinchia3636.airngreen.services.AirQualityService;
import dev.melvinchia3636.airngreen.services.alerts.AqiAlert;
import dev.melvinchia3636.airngreen.services.alerts.PollutionAlertService;
import dev.melvinchia3636.airngreen.services.LocationService;
import dev.melvinchia3636.airngreen.services.StationsService;

import androidx.lifecycle.LiveData;

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

    /** Latest pollution-threshold crossing, for in-app banners. */
    public LiveData<AqiAlert> latestAlert() {
        return PollutionAlertService.get(getApplication()).latestAlert();
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
