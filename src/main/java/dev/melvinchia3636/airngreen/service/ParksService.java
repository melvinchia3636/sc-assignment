package dev.melvinchia3636.airngreen.service;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.melvinchia3636.airngreen.model.District;
import dev.melvinchia3636.airngreen.model.NearbyPark;
import dev.melvinchia3636.airngreen.model.Park;

/**
 * Recommends green spaces near a district.
 *
 * <p>Parks come from a small curated dataset bundled with the application, so
 * the recommendation works without a second network service.</p>
 */
public final class ParksService {

    private static final String RESOURCE = "/parks.json";
    private static final double EARTH_RADIUS_KM = 6371.0088;
    private static final double HALF = 0.5;
    private static final int DEFAULT_LIMIT = 5;

    private final List<Park> parks;

    /**
     * Creates a service over an in-memory park list.
     *
     * @param parks parks to recommend
     */
    public ParksService(List<Park> parks) {
        this.parks = List.copyOf(parks);
    }

    /**
     * Loads the bundled park dataset.
     *
     * @return a service backed by {@code /parks.json}
     * @throws IOException when the resource is missing or unreadable
     */
    public static ParksService fromClasspath() throws IOException {
        InputStream stream = ParksService.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IOException("Missing " + RESOURCE);
        }

        try (InputStream input = stream;
             Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            ParkEntry[] entries = new Gson().fromJson(reader, ParkEntry[].class);
            return new ParksService(toParks(entries));
        }
    }

    /**
     * Every park in the dataset.
     *
     * @return an immutable list
     */
    public List<Park> all() {
        return parks;
    }

    /**
     * The closest parks to a district, nearest first.
     *
     * @param district district providing the query point
     * @return up to five parks
     */
    public List<NearbyPark> nearest(District district) {
        return nearest(district, DEFAULT_LIMIT);
    }

    /**
     * The closest parks to a district, nearest first.
     *
     * @param district district providing the query point
     * @param limit    maximum number of results
     * @return parks ordered by distance
     */
    public List<NearbyPark> nearest(District district, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be positive.");
        }

        return parks.stream()
                .map(park -> new NearbyPark(park, distanceKm(
                        district.latitude(), district.longitude(),
                        park.latitude(), park.longitude())))
                .sorted(Comparator.comparingDouble(NearbyPark::distanceKm))
                .limit(limit)
                .toList();
    }

    /**
     * Great-circle distance between two points.
     *
     * @param lat1 first latitude in degrees
     * @param lon1 first longitude in degrees
     * @param lat2 second latitude in degrees
     * @param lon2 second longitude in degrees
     * @return distance in kilometres
     */
    public static double distanceKm(double lat1, double lon1,
                                    double lat2, double lon2) {
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(deltaLat * HALF), 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(deltaLon * HALF), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }

    private static List<Park> toParks(ParkEntry[] entries) {
        List<Park> result = new ArrayList<>();
        if (entries == null) {
            return result;
        }

        for (ParkEntry entry : entries) {
            Park park = toPark(entry);
            if (park != null) {
                result.add(park);
            }
        }
        return result;
    }

    private static Park toPark(ParkEntry entry) {
        if (entry == null || entry.name == null || entry.district == null) {
            return null;
        }

        try {
            return new Park(entry.name, District.valueOf(entry.district),
                    entry.latitude, entry.longitude, entry.facilities);
        } catch (IllegalArgumentException unknownDistrict) {
            return null;
        }
    }

    /** Raw JSON shape of one park entry. */
    private static final class ParkEntry {
        private String name;
        private String district;
        private double latitude;
        private double longitude;
        private String facilities;
    }
}
