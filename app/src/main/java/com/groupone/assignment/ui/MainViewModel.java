package com.groupone.assignment.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.groupone.assignment.repositories.ApiException;
import com.groupone.assignment.repositories.model.Snapshot;
import com.groupone.assignment.repositories.AirQualityRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns the snapshot, loading state and place name so they survive rotation.
 */
public class MainViewModel extends AndroidViewModel {

    private final AirQualityRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Snapshot> snapshot = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> placeName = new MutableLiveData<>("Kuala Lumpur");
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    private double latitude = 3.1390;
    private double longitude = 101.6869;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new AirQualityRepository(application);
        Snapshot cached = repository.loadCached();
        if (cached != null) {
            snapshot.setValue(cached);
        }
    }

    public LiveData<Snapshot> getSnapshot() {
        return snapshot;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getPlaceName() {
        return placeName;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public String placeName() {
        String value = placeName.getValue();
        return value == null ? "Kuala Lumpur" : value;
    }

    private boolean running;

    public void setLocation(String place, double latitude, double longitude) {
        this.placeName.setValue(place);
        this.latitude = latitude;
        this.longitude = longitude;
        startLoad();
    }

    public void startLocationLookup() {
        loading.setValue(true);
    }

    public void refresh() {
        startLoad();
    }

    private void startLoad() {
        if (running) {
            return;
        }
        running = true;
        loading.setValue(true);
        error.setValue(null);
        executor.execute(() -> {
            try {
                Snapshot refreshed = repository.refresh(latitude, longitude);
                snapshot.postValue(refreshed);
            } catch (ApiException failure) {
                if (snapshot.getValue() == null) {
                    error.postValue(failure.message(getApplication()));
                }
            } finally {
                running = false;
                loading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}
