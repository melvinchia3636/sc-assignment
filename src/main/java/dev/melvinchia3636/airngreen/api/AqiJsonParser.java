package dev.melvinchia3636.airngreen.api;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

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
 * <p>Gson binds the response into small DTOs, which are then mapped to the
 * immutable model. Parsing stays separate from transport so it can be tested
 * with literal JSON fixtures.</p>
 */
public final class AqiJsonParser {

    private final Gson gson = new Gson();

    /**
     * Parses a {@code /nearest-station} or {@code /station/:idx} body.
     *
     * @param body response document
     * @return the parsed reading
     * @throws JsonParseException when the document has no station object
     */
    public StationReading parseStation(String body) {
        StationEnvelope envelope = gson.fromJson(body, StationEnvelope.class);
        if (envelope == null || envelope.station() == null) {
            throw new JsonParseException("Response has no station object");
        }

        return toReading(envelope.station());
    }

    /**
     * Parses a {@code /stations} body.
     *
     * @param body response document
     * @return every listed station, in upstream order
     * @throws JsonParseException when the document has no station array
     */
    public List<StationSummary> parseStations(String body) {
        StationsEnvelope envelope = gson.fromJson(body, StationsEnvelope.class);
        if (envelope == null || envelope.stations() == null) {
            throw new JsonParseException("Response has no stations array");
        }

        List<StationSummary> stations = new ArrayList<>();
        for (StationDto dto : envelope.stations()) {
            if (dto != null) {
                stations.add(toSummary(dto));
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
            ErrorEnvelope envelope = gson.fromJson(body, ErrorEnvelope.class);

            return envelope == null || envelope.error() == null
                    ? null
                    : envelope.error().message();
        } catch (RuntimeException malformed) {
            return null;
        }
    }

    private StationReading toReading(Station station) {
        return new StationReading(
                text(station.idx()),
                text(station.name()),
                station.aqi() == null ? Double.NaN : station.aqi(),
                station.updatedAt() == null ? 0 : station.updatedAt(),
                currentValues(station.current()),
                aqiSeries(station.series()));
    }

    private StationSummary toSummary(StationDto station) {
        return new StationSummary(
                text(station.idx()),
                text(station.name()).trim(),
                station.latitude() == null ? 0 : station.latitude(),
                station.longitude() == null ? 0 : station.longitude(),
                station.aqi());
    }

    private Map<String, Double> currentValues(Map<String, Double> current) {
        if (current == null) {
            return Map.of();
        }

        Map<String, Double> values = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : current.entrySet()) {
            Double value = entry.getValue();
            if (value != null && Double.isFinite(value)) {
                values.put(entry.getKey(), value);
            }
        }

        return Map.copyOf(values);
    }

    private List<SeriesPoint> aqiSeries(Series series) {
        if (series == null || series.aqi() == null) {
            return List.of();
        }

        List<SeriesPoint> points = new ArrayList<>();
        for (Point point : series.aqi()) {
            if (point != null && point.t() != null && point.v() != null) {
                points.add(new SeriesPoint(point.t(), point.v()));
            }
        }

        return List.copyOf(points);
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    /** {@code { "station": { ... } }}. */
    private record StationEnvelope(Station station) {
    }

    /** One station detail object. */
    private record Station(
            String idx,
            String name,
            Double aqi,
            Long updatedAt,
            Map<String, Double> current,
            Series series) {
    }

    /** The {@code series} object; only the AQI series is used. */
    private record Series(List<Point> aqi) {
    }

    /** One {@code { "t": ..., "v": ... }} sample. */
    private record Point(Long t, Double v) {
    }

    /** {@code { "stations": [ ... ] }}. */
    private record StationsEnvelope(List<StationDto> stations) {
    }

    /** One entry in the station directory. */
    private record StationDto(
            String idx,
            String name,
            Double latitude,
            Double longitude,
            Double aqi) {
    }

    /** {@code { "error": { "message": ... } }}. */
    private record ErrorEnvelope(ErrorDto error) {
    }

    private record ErrorDto(String message) {
    }
}
