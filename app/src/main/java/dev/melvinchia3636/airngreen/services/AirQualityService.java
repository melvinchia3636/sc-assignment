package dev.melvinchia3636.airngreen.services;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dev.melvinchia3636.airngreen.repositories.AirQualityRepository;
import dev.melvinchia3636.airngreen.scrapers.lib.ApiException;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads and caches the nearest station reading.
 */
public class AirQualityService {

    private final Context context;
    private final AirQualityRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<StationDetail> station = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    private boolean running;

    public AirQualityService(Context context, AirQualityRepository repository) {
        this.context = context.getApplicationContext();
        this.repository = repository;
        StationDetail cached = repository.loadCached();
        if (cached != null) {
            station.setValue(cached);
        }
    }

    public LiveData<StationDetail> getStation() {
        return station;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void beginLoading() {
        loading.setValue(true);
    }

    public void refresh(double latitude, double longitude) {
        if (running) {
            return;
        }
        running = true;
        loading.setValue(true);
        error.setValue(null);
        executor.execute(() -> {
            try {
                station.postValue(repository.refresh(latitude, longitude));
            } catch (ApiException failure) {
                if (station.getValue() == null) {
                    error.postValue(failure.message(context));
                }
            } finally {
                running = false;
                loading.postValue(false);
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
