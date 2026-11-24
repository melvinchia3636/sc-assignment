package dev.melvinchia3636.airngreen.storage;

import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategies;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategy;

/**
 * Typed access to the user's saved preferences.
 */
public final class SettingsStore {

    private static final String DISTRICT_KEY = "district";
    private static final String AUDIENCE_KEY = "audience";
    private static final String THRESHOLD_KEY = "alertThreshold";

    private final LocalStore store;
    private final Properties properties;
    private final CopyOnWriteArrayList<Consumer<District>> districtListeners =
            new CopyOnWriteArrayList<>();

    /**
     * Loads settings from the given store.
     *
     * @param store persistence used for the properties file
     */
    public SettingsStore(LocalStore store) {
        this.store = store;
        this.properties = store.loadSettings();
    }

    /**
     * District selected on the dashboard.
     *
     * @return the saved district, defaulting to Kuala Lumpur
     */
    public District getDistrict() {
        return District.fromName(properties.getProperty(DISTRICT_KEY));
    }

    /**
     * Saves the selected district.
     *
     * @param district district to remember
     */
    public void setDistrict(District district) {
        properties.setProperty(DISTRICT_KEY, district.name());
        save();
        for (Consumer<District> listener : districtListeners) {
            listener.accept(district);
        }
    }

    /**
     * Registers a callback invoked after the district changes.
     *
     * @param listener callback receiving the new district
     */
    public void onDistrictChange(Consumer<District> listener) {
        districtListeners.addIfAbsent(listener);
    }

    /**
     * Audience strategy selected on the dashboard.
     *
     * @return the saved strategy, defaulting to general advice
     */
    public AdviceStrategy getAudienceStrategy() {
        return AdviceStrategies.byKey(properties.getProperty(AUDIENCE_KEY));
    }

    /**
     * Saves the selected audience strategy.
     *
     * @param strategy strategy to remember
     */
    public void setAudienceStrategy(AdviceStrategy strategy) {
        properties.setProperty(AUDIENCE_KEY, strategy.key());
        save();
    }

    /**
     * Pollution threshold used by the alert subject.
     *
     * @return the saved threshold, defaulting to off
     */
    public AlertThreshold getThreshold() {
        return AlertThreshold.fromName(properties.getProperty(THRESHOLD_KEY));
    }

    /**
     * Saves the pollution threshold.
     *
     * @param threshold threshold to remember
     */
    public void setThreshold(AlertThreshold threshold) {
        properties.setProperty(THRESHOLD_KEY, threshold.name());
        save();
    }

    private void save() {
        store.saveSettings(properties);
    }
}
