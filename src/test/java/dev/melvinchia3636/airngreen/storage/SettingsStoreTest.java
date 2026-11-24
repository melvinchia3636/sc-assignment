package dev.melvinchia3636.airngreen.storage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.melvinchia3636.airngreen.model.AlertThreshold;
import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.service.advice.ChildrenAdvice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class SettingsStoreTest {

    @TempDir
    Path directory;

    @Test
    void defaultsToKualaLumpurGeneralAudienceAndAlertsOff() {
        SettingsStore settings = new SettingsStore(new LocalStore(directory));

        assertAll(
            () -> assertEquals(District.KUALA_LUMPUR, settings.getDistrict()),
            () -> assertEquals("general", settings.getAudienceStrategy().key()),
            () -> assertEquals(AlertThreshold.OFF, settings.getThreshold())
        );
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
            () -> assertEquals(
                AlertThreshold.UNHEALTHY,
                reopened.getThreshold()
            )
        );
    }

    @Test
    void setDistrict_notifiesListeners() {
        SettingsStore settings = new SettingsStore(new LocalStore(directory));
        List<District> seen = new ArrayList<>();
        settings.onDistrictChange(seen::add);

        settings.setDistrict(District.KLANG);

        assertEquals(List.of(District.KLANG), seen);
    }
}
