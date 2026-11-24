package dev.melvinchia3636.airngreen.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.melvinchia3636.airngreen.model.SeriesPoint;
import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

/**
 * Converts the API's JSON documents into domain objects.
 *
 * <p>Parsing is kept separate from transport so it can be unit-tested with
 * literal JSON fixtures.</p>
 */
public final class AqiJsonParser {

    private static final String STATION = "station";
    private static final String STATIONS = "stations";

    /**
     * Parses a {@code /nearest-station} or {@code /station/:idx} body.
     *
     * @param root response document
     * @return the parsed reading
     * @throws JsonParseException when the document has no station object
     */
    public StationReading parseStation(JsonObject root) {
        JsonObject station = requireObject(root, STATION);

        return new StationReading(
                text(station, "idx"),
                text(station, "name"),
                number(station, "aqi", Double.NaN),
                (long) number(station, "updatedAt", 0),
                parseCurrent(station),
                parseAqiSeries(station));
    }

    /**
     * Parses a {@code /stations} body.
     *
     * @param root response document
     * @return every listed station, in upstream order
     * @throws JsonParseException when the document has no station array
     */
    public List<StationSummary> parseStations(JsonObject root) {
        JsonElement element = root.get(STATIONS);
        if (element == null || !element.isJsonArray()) {
            throw new JsonParseException("Response has no stations array");
        }

        JsonArray array = element.getAsJsonArray();
        List<StationSummary> stations = new ArrayList<>(array.size());
        for (JsonElement entry : array) {
            if (entry.isJsonObject()) {
                stations.add(parseSummary(entry.getAsJsonObject()));
            }
        }
        return List.copyOf(stations);
    }

    /**
     * Extracts the message from an API error envelope.
     *
     * @param body raw response body
     * @return the message, or null when the body is not an error envelope
     */
    public String errorMessage(String body) {
        try {
            JsonElement error = JsonParser.parseString(body)
                    .getAsJsonObject()
                    .get("error");
            if (error != null && error.isJsonObject()) {
                return text(error.getAsJsonObject(), "message");
            }
        } catch (RuntimeException malformed) {
            return null;
        }
        return null;
    }

    private StationSummary parseSummary(JsonObject station) {
        Double aqi = station.has("aqi") && !station.get("aqi").isJsonNull()
                ? station.get("aqi").getAsDouble()
                : null;

        return new StationSummary(
                text(station, "idx"),
                text(station, "name").trim(),
                number(station, "latitude", 0),
                number(station, "longitude", 0),
                aqi);
    }

    private Map<String, Double> parseCurrent(JsonObject station) {
        JsonObject current = object(station, "current");
        Map<String, Double> values = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : current.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                values.put(entry.getKey(), value.getAsDouble());
            }
        }
        return values;
    }

    private List<SeriesPoint> parseAqiSeries(JsonObject station) {
        JsonElement element = object(station, "series").get("aqi");
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }

        List<SeriesPoint> points = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            addPoint(points, entry);
        }
        return List.copyOf(points);
    }

    private void addPoint(List<SeriesPoint> points, JsonElement entry) {
        if (!entry.isJsonObject()) {
            return;
        }

        JsonObject point = entry.getAsJsonObject();
        JsonElement value = point.get("v");
        if (value == null || value.isJsonNull() || !value.isJsonPrimitive()) {
            return;
        }
        points.add(new SeriesPoint((long) number(point, "t", 0), value.getAsDouble()));
    }

    private JsonObject requireObject(JsonObject parent, String name) {
        JsonElement element = parent.get(name);
        if (element == null || !element.isJsonObject()) {
            throw new JsonParseException("Response has no " + name + " object");
        }
        return element.getAsJsonObject();
    }

    private JsonObject object(JsonObject parent, String name) {
        JsonElement element = parent.get(name);
        return element != null && element.isJsonObject()
                ? element.getAsJsonObject()
                : new JsonObject();
    }

    private String text(JsonObject parent, String name) {
        JsonElement element = parent.get(name);
        return element != null && element.isJsonPrimitive()
                ? element.getAsString()
                : "";
    }

    private double number(JsonObject parent, String name, double fallback) {
        JsonElement element = parent.get(name);
        if (element == null || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        return element.getAsDouble();
    }
}
