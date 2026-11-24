package dev.melvinchia3636.airngreen.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParseException;

import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

import org.junit.jupiter.api.Test;

import java.util.List;

class AqiJsonParserTest {

    private static final String STATION_JSON = """
        {
          "station": {
            "idx": "2609",
            "name": "Kapit, Sarawak, Malaysia",
            "aqi": 25,
            "updatedAt": 1795471200,
            "current": { "aqi": 25, "pm25": 13, "pm10": 13, "h": 88 },
            "series": {
              "aqi": [
                { "t": 1795467600, "v": 24 },
                { "t": 1795471200, "v": 25 }
              ]
            }
          }
        }
        """;

    private final AqiJsonParser parser = new AqiJsonParser();

    @Test
    void parseStation_readsEveryField() {
        StationReading reading = parser.parseStation(STATION_JSON);

        assertAll(
            () -> assertEquals("2609", reading.idx()),
            () -> assertEquals("Kapit, Sarawak, Malaysia", reading.name()),
            () -> assertEquals(25, reading.aqi()),
            () -> assertEquals(1795471200, reading.updatedAt()),
            () -> assertEquals(25, reading.pollutant("aqi").orElseThrow()),
            () -> assertEquals(13, reading.pollutant("pm25").orElseThrow()),
            () -> assertEquals(2, reading.series().size()),
            () -> assertEquals(24, reading.series().get(0).value())
        );
    }

    @Test
    void parseStation_skipsMissingSeriesValues() {
        String json = """
            {"station":{"idx":"1","aqi":10,"series":{"aqi":[
              {"t":1,"v":10},{"t":2,"v":null},{"t":3,"v":12}]}}}
            """;

        StationReading reading = parser.parseStation(json);

        assertAll(
            () -> assertEquals(2, reading.series().size()),
            () -> assertEquals(12, reading.series().get(1).value())
        );
    }

    @Test
    void parseStation_toleratesMissingOptionalFields() {
        StationReading reading = parser
            .parseStation("{\"station\":{\"idx\":\"1\"}}");

        assertAll(
            () -> assertEquals("", reading.name()),
            () -> assertTrue(Double.isNaN(reading.aqi())),
            () -> assertTrue(reading.pollutants().isEmpty()),
            () -> assertTrue(reading.series().isEmpty())
        );
    }

    @Test
    void parseStation_rejectsResponseWithoutStation() {
        assertThrows(JsonParseException.class, () -> parser.parseStation("{}"));
    }

    @Test
    void parseStations_readsTheDirectory() {
        String json = """
            {"stations":[
              {"idx":"2609","name":" Kapit ","latitude":2.0,"longitude":112.9,
               "aqi":25,"time":"2026-11-24T00:00:00Z"},
              {"idx":"A1","name":"Idle","latitude":3.0,"longitude":101.0,
               "aqi":null,"time":"2026-11-24T00:00:00Z"}
            ]}
            """;

        List<StationSummary> stations = parser.parseStations(json);

        assertAll(
            () -> assertEquals(2, stations.size()),
            () -> assertEquals("Kapit", stations.get(0).name()),
            () -> assertEquals(25.0, stations.get(0).aqi()),
            () -> assertNull(stations.get(1).aqi())
        );
    }

    @Test
    void parseStations_rejectsResponseWithoutArray() {
        assertThrows(
            JsonParseException.class,
            () -> parser.parseStations("{}")
        );
    }

    @Test
    void errorMessage_extractsTheApiMessage() {
        String body = "{\"error\":{\"code\":\"not_found\","
            + "\"message\":\"Station missing.\"}}";

        assertEquals("Station missing.", parser.errorMessage(body));
    }

    @Test
    void errorMessage_returnsNullForOtherBodies() {
        assertAll(
            () -> assertNull(parser.errorMessage("not json")),
            () -> assertNull(parser.errorMessage("{}")),
            () -> assertNull(parser.errorMessage("{\"error\":\"text\"}"))
        );
    }
}
