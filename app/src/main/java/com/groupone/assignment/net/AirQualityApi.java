package com.groupone.assignment.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.groupone.assignment.BuildConfig;
import com.groupone.assignment.R;
import com.groupone.assignment.data.ApiException;
import com.groupone.assignment.data.StationDetail;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

/**
 * Client for the aqi-api service. The service scrapes WAQI and returns the
 * current readings plus the aligned 72-hour series.
 */
public class AirQualityApi {

    private final HttpJsonClient client;

    public AirQualityApi() {
        this(new HttpJsonClient());
    }

    AirQualityApi(HttpJsonClient client) {
        this.client = client;
    }

    public StationDetail fetch(double latitude, double longitude) throws ApiException {
        HttpUrl url = HttpUrl.get(BuildConfig.AIR_QUALITY_API_URL).newBuilder()
                .addPathSegments("air-quality")
                .addQueryParameter("lat", Double.toString(latitude))
                .addQueryParameter("lon", Double.toString(longitude))
                .build();

        String body = client.request("GET", url.toString(), null, null);
        try {
            return parse(body);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_data, invalidJson);
        }
    }

    static StationDetail parse(String json) {
        JsonObject station = JsonParser.parseString(json).getAsJsonObject()
                .getAsJsonObject("station");
        if (station == null) {
            throw new IllegalStateException("Missing station payload");
        }

        Map<String, Double> current = new LinkedHashMap<>();
        JsonObject currentJson = station.getAsJsonObject("current");
        if (currentJson != null) {
            for (Map.Entry<String, JsonElement> entry : currentJson.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    current.put(entry.getKey(), entry.getValue().getAsDouble());
                }
            }
        }

        Map<String, List<Double>> series = new LinkedHashMap<>();
        JsonObject seriesJson = station.getAsJsonObject("series");
        if (seriesJson != null) {
            for (Map.Entry<String, JsonElement> entry : seriesJson.entrySet()) {
                if (entry.getValue().isJsonArray()) {
                    series.put(entry.getKey(), readSeries(entry.getValue().getAsJsonArray()));
                }
            }
        }

        return new StationDetail(
                string(station, "idx"),
                string(station, "name"),
                number(station, "aqi"),
                station.has("start") ? station.get("start").getAsLong() : 0,
                station.has("step") ? station.get("step").getAsInt() : 3600,
                station.has("updatedAt") ? station.get("updatedAt").getAsLong() : 0,
                current,
                series);
    }

    private static List<Double> readSeries(JsonArray values) {
        List<Double> series = new java.util.ArrayList<>(values.size());
        for (JsonElement value : values) {
            series.add(value.isJsonNull() ? Double.NaN : value.getAsDouble());
        }
        return series;
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static double number(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? Double.NaN : value.getAsDouble();
    }
}
