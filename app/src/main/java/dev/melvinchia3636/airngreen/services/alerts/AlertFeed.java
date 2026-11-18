package dev.melvinchia3636.airngreen.services.alerts;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Observer that turns alerts into observable state for the UI.
 */
public final class AlertFeed implements AlertObserver {

    private final MutableLiveData<AqiAlert> latest = new MutableLiveData<>();

    @Override
    public void onThresholdExceeded(AqiAlert alert) {
        latest.postValue(alert);
    }

    public LiveData<AqiAlert> latest() {
        return latest;
    }
}
