package com.groupone.assignment.net;

import com.groupone.assignment.R;
import com.groupone.assignment.data.ApiException;
import com.groupone.assignment.data.GreenSpace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Client for the OpenStreetMap Overpass API. Finds parks, gardens and grass
 * areas within a radius of the given coordinates.
 */
public class GreenSpaceApi {

    private static final int MAX_RESULTS = 8;
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter?data=";

    private final HttpJsonClient client;

    public GreenSpaceApi() {
        this(new HttpJsonClient());
    }

    public GreenSpaceApi(HttpJsonClient client) {
        this.client = client;
    }

    public List<GreenSpace> fetchNearby(double latitude, double longitude, int radiusMeters)
            throws ApiException {
        String url = OVERPASS_URL + encode(buildQuery(latitude, longitude, radiusMeters));
        String body = client.getJson(url);
        try {
            return parse(new JSONObject(body), latitude, longitude);
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_green_data, invalidJson);
        }
    }

    private String buildQuery(double latitude, double longitude, int radiusMeters) {
        String around = String.format(Locale.US, "(around:%d,%.5f,%.5f)",
                radiusMeters, latitude, longitude);
        return "[out:json][timeout:25];("
                + "way[\"leisure\"=\"park\"]" + around + ";"
                + "way[\"leisure\"=\"garden\"]" + around + ";"
                + "way[\"landuse\"=\"grass\"]" + around + ";"
                + ");out tags center " + MAX_RESULTS + ";";
    }

    private String encode(String value) throws ApiException {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_request_prepare, impossible);
        }
    }

    /**
     * Parses an Overpass response into green spaces sorted by distance.
     */
    public static List<GreenSpace> parse(JSONObject root, double latitude, double longitude)
            throws JSONException {
        List<GreenSpace> spaces = new ArrayList<>();
        JSONArray elements = root.optJSONArray("elements");
        if (elements == null) {
            return spaces;
        }
        for (int i = 0; i < elements.length(); i++) {
            JSONObject element = elements.optJSONObject(i);
            if (element == null) {
                continue;
            }
            JSONObject tags = element.optJSONObject("tags");
            JSONObject center = element.optJSONObject("center");
            if (tags == null || center == null) {
                continue;
            }
            String name = tags.optString("name", "").trim();
            if (name.isEmpty()) {
                name = "Unnamed green space";
            }
            String type = tags.optString("leisure",
                    tags.optString("landuse", "green space"));
            double spaceLat = center.optDouble("lat", latitude);
            double spaceLon = center.optDouble("lon", longitude);
            spaces.add(new GreenSpace(name, capitalise(type),
                    distanceMeters(latitude, longitude, spaceLat, spaceLon),
                    spaceLat, spaceLon));
        }
        spaces.sort(Comparator.comparingDouble(GreenSpace::getDistanceMeters));
        return spaces.size() > MAX_RESULTS
                ? new ArrayList<>(spaces.subList(0, MAX_RESULTS))
                : spaces;
    }

    private static String capitalise(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.US) + value.substring(1);
    }

    static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
