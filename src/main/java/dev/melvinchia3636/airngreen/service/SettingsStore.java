package dev.melvinchia3636.airngreen.service;

import java.util.Properties;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategies;
import dev.melvinchia3636.airngreen.service.advice.AdviceStrategy;

/**
 * Typed access to the user's saved preferences.
 */
public final class SettingsStore {

    private static final String DISTRICT_KEY = "district";
    private static final String ADVICE_KEY = "advice";
    private static final String THRESHOLD_KEY = "alertThreshold";

    private final LocalStore store;
    private final Properties properties;

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
    public District district() {
        return District.fromName(properties.getProperty(DISTRICT_KEY));
    }

    /**
     * Saves the selected district.
     *
     * @param district district to remember
     */
    public void district(District district) {
        properties.setProperty(DISTRICT_KEY, district.name());
        save();
    }

    /**
     * Advice audience selected on the dashboard.
     *
     * @return the saved strategy, defaulting to general advice
     */
    public AdviceStrategy advice() {
        return AdviceStrategies.byKey(properties.getProperty(ADVICE_KEY));
    }

    /**
     * Saves the selected advice audience.
     *
     * @param advice strategy to remember
     */
    public void advice(AdviceStrategy advice) {
        properties.setProperty(ADVICE_KEY, advice.key());
        save();
    }

    /**
     * Pollution threshold used by the alert subject.
     *
     * @return the saved threshold, defaulting to off
     */
    public AlertThreshold threshold() {
        return AlertThreshold.fromName(properties.getProperty(THRESHOLD_KEY));
    }

    /**
     * Saves the pollution threshold.
     *
     * @param threshold threshold to remember
     */
    public void threshold(AlertThreshold threshold) {
        properties.setProperty(THRESHOLD_KEY, threshold.name());
        save();
    }

    private void save() {
        store.saveSettings(properties);
    }
}
