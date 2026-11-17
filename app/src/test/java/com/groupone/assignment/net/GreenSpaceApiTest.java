package com.groupone.assignment.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.groupone.assignment.data.GreenSpace;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

class GreenSpaceApiTest {

    private static final String SAMPLE = """
            {
              "elements": [
                {
                  "type": "way",
                  "tags": {"name": "KLCC Park", "leisure": "park"},
                  "center": {"lat": 3.1560, "lon": 101.7130}
                },
                {
                  "type": "way",
                  "tags": {"landuse": "grass"},
                  "center": {"lat": 3.1400, "lon": 101.6870}
                },
                {
                  "type": "way",
                  "tags": {"name": "Pocket Garden", "leisure": "garden"},
                  "center": {"lat": 3.1395, "lon": 101.6870}
                },
                {
                  "type": "way",
                  "tags": {"name": "No Center", "leisure": "park"}
                }
              ]
            }
            """;

    @Test
    void parse_ordersByDistanceAndSkipsElementsWithoutCentre() throws JSONException {
        List<GreenSpace> spaces = GreenSpaceApi.parse(new JSONObject(SAMPLE), 3.1390, 101.6869);

        assertEquals(3, spaces.size());
        assertEquals("Pocket Garden", spaces.get(0).getName());
        assertTrue(spaces.get(0).getDistanceMeters() < spaces.get(1).getDistanceMeters());
        assertEquals("KLCC Park", spaces.get(2).getName());
    }

    @Test
    void parse_unnamedSpace_getsFriendlyName() throws JSONException {
        List<GreenSpace> spaces = GreenSpaceApi.parse(new JSONObject(SAMPLE), 3.1390, 101.6869);

        assertEquals("Unnamed green space", spaces.get(1).getName());
        assertEquals("Grass", spaces.get(1).getType());
    }

    @Test
    void parse_missingElements_returnsEmptyList() throws JSONException {
        assertTrue(GreenSpaceApi.parse(new JSONObject("{}"), 0, 0).isEmpty());
    }

    @Test
    void parse_nonArrayElements_returnsEmptyList() throws JSONException {
        assertTrue(GreenSpaceApi.parse(new JSONObject("{\"elements\": 5}"), 0, 0).isEmpty());
    }
}
