package dev.melvinchia3636.airngreen.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dev.melvinchia3636.airngreen.repositories.AirQualityRepository;
import dev.melvinchia3636.airngreen.repositories.ApiException;
import dev.melvinchia3636.airngreen.repositories.model.Station;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads the nearby station list used by the map.
 */
public class StationsService {

    private final AirQualityRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Station>> stations =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public StationsService(AirQualityRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Station>> getStations() {
        return stations;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void load(double latitude, double longitude) {
        loading.setValue(true);
        executor.execute(() -> {
            try {
                stations.postValue(repository.nearbyStations(latitude, longitude));
            } catch (ApiException ignored) {
                // The map keeps the last station list.
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
