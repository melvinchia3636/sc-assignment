package dev.melvinchia3636.airngreen.storage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.service.advice.ChildrenAdvice;

class SettingsStoreTest {

    @TempDir
    Path directory;

    @Test
    void defaultsToKualaLumpurGeneralAudienceAndAlertsOff() {
        SettingsStore settings = new SettingsStore(new LocalStore(directory));

        assertAll(
                () -> assertEquals(District.KUALA_LUMPUR, settings.getDistrict()),
                () -> assertEquals("general",
                        settings.getAudienceStrategy().key()),
                () -> assertEquals(AlertThreshold.OFF, settings.getThreshold()));
    }

    @Test
    void audienceStrategy_persistsAcrossInstances() {
        SettingsStore settings = new SettingsStore(new LocalStore(directory));
        settings.setAudienceStrategy(new ChildrenAdvice());

        SettingsStore reopened = new SettingsStore(new LocalStore(directory));

        assertEquals("children", reopened.getAudienceStrategy().key());
    }

    @Test
    void districtAndThreshold_persist() {
        SettingsStore settings = new SettingsStore(new LocalStore(directory));
        settings.setDistrict(District.KLANG);
        settings.setThreshold(AlertThreshold.UNHEALTHY);

        SettingsStore reopened = new SettingsStore(new LocalStore(directory));

        assertAll(
                () -> assertEquals(District.KLANG, reopened.getDistrict()),
                () -> assertEquals(AlertThreshold.UNHEALTHY,
                        reopened.getThreshold()));
    }
}
