package com.groupone.assignment.scrapers.parsers;

import com.groupone.assignment.repositories.StationRepository.Coordinates;
import com.groupone.assignment.repositories.StationRepository.Station;
import com.groupone.assignment.repositories.BoundsRepository;

import net.sf.geographiclib.Geodesic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parses WAQI map bounds responses into station summaries.
 */
public class StationsParser {

    /**
     * Parses the WAQI map bounds response.
     *
     * Invalid entries (missing id or coordinates) are dropped. When
     * {@code origin} is given, stations are sorted nearest first and include
     * {@code distanceMeters}; otherwise they are sorted by name without
     * distances.
     */
    public List<Station> parse(BoundsRepository.Response root, Coordinates origin) {
        List<Station> stations = new ArrayList<>();
        if (root.data != null) {
            for (BoundsRepository.Entry entry : root.data) {
                Station station = toStation(entry, origin);
                if (station != null) {
                    stations.add(station);
                }
            }
        }

        if (origin != null) {
            stations.sort(Comparator.comparingDouble(station ->
                    station.getDistanceMeters() == null ? 0 : station.getDistanceMeters()));
        } else {
            stations.sort(Comparator
                    .comparing(Station::getName)
                    .thenComparing(Station::getIdx));
        }
        return stations;
    }

    private Station toStation(BoundsRepository.Entry entry, Coordinates origin) {
        if (entry.idx == null || entry.idx.isEmpty() || entry.geo == null
                || entry.geo.size() < 2) {
            return null;
        }
        Double latitude = entry.geo.get(0);
        Double longitude = entry.geo.get(1);
        if (latitude == null || longitude == null
                || !Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            return null;
        }

        Double distanceMeters = origin == null ? null
                : Geodesic.WGS84.Inverse(origin.getLat(), origin.getLon(),
                        latitude, longitude).s12;

        return new Station(
                entry.idx,
                entry.name == null || entry.name.isEmpty() ? entry.idx : entry.name,
                latitude,
                longitude,
                parseAqi(entry.aqi),
                entry.utime == null ? "" : entry.utime,
                distanceMeters);
    }

    private double parseAqi(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (!(value instanceof String) || ((String) value).isEmpty()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble((String) value);
        } catch (NumberFormatException invalidNumber) {
            return Double.NaN;
        }
    }
}
