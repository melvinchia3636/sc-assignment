package com.groupone.assignment.net;

import com.groupone.assignment.R;
import com.groupone.assignment.data.ApiException;
import com.groupone.assignment.data.Station;
import com.groupone.assignment.data.StationDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Port of the LifeForge air-quality scraper (WAQI / aqicn.org).
 *
 * <p>Two station families exist: numeric ids use the api2 token + feed flow,
 * while ids starting with {@code A} use the airnet hourly + CWOP weather flow.
 * Both are aligned to a 72-hour, one-point-per-hour series like the original.
 */
public class WaqiApi {

    private static final String STATIONS_URL =
            "https://mapq.waqi.info/mapq2/bounds"
                    + "?bounds=%s,%s,%s,%s&inc=placeholders&viewer=webgl&markers=all";
    private static final String API2_TOKEN_URL = "https://api2.waqi.info/api/token/%s";
    private static final String API2_FEED_URL = "https://api2.waqi.info/api/feed/@%s/aqi.json";
    private static final String AIRNET_TOKEN_URL = "https://airnet.waqi.info/airnet/token";
    private static final String AIRNET_HOURLY_URL =
            "https://airnet.waqi.info/airnet/feed/hourly/%s?key=%s";
    private static final String AIRNET_CWOP_URL =
            "https://airnet.waqi.info/airnet/feed/cwop/%s?key=%s";

    private static final long TOKEN_TTL_MS = 1000L * 60 * 30;
    private static final long AIRNET_TTL_MS = 1000L * 60 * 30;
    private static final int SERIES_HOURS = 72;
    private static final int SERIES_STEP = 3600;
    private static final double[] SEARCH_RADII = {0.35, 1.0, 3.0};
    private static final int MAX_STATION_ATTEMPTS = 3;

    private static final Map<String, String> BROWSER_HEADERS = browserHeaders();
    private static final Map<String, String> FORM_HEADERS = formHeaders();

    private final HttpJsonClient client = new HttpJsonClient();
    private final Map<String, CachedToken> tokenCache = new HashMap<>();

    private CachedToken airnetKey;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Finds the nearest station to the coordinates that actually reports data.
     */
    public StationDetail findNearest(double latitude, double longitude) throws ApiException {
        for (double radius : SEARCH_RADII) {
            List<Station> stations = fetchStations(
                    latitude - radius, longitude - radius,
                    latitude + radius, longitude + radius, latitude, longitude);
            for (int i = 0; i < Math.min(MAX_STATION_ATTEMPTS, stations.size()); i++) {
                StationDetail detail = fetchDetail(stations.get(i).getIdx());
                if (detail != null) {
                    return detail;
                }
            }
        }
        throw new ApiException(404, R.string.error_no_station);
    }

    public List<Station> fetchStations(double south, double west, double north, double east,
                                       double originLat, double originLon) throws ApiException {
        String url = String.format(Locale.US, STATIONS_URL, west, south, east, north);
        String body = client.request("GET", url, BROWSER_HEADERS, null);
        try {
            return parseStations(new JSONObject(body), originLat, originLon);
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_list, invalidJson);
        }
    }

    public StationDetail fetchDetail(String idx) throws ApiException {
        if (idx.startsWith("A")) {
            return buildAirnetDetail(idx);
        }
        return buildApi2Detail(idx);
    }

    // -----------------------------------------------------------------------
    // WAQI api2 flow (numeric station ids)
    // -----------------------------------------------------------------------

    private StationDetail buildApi2Detail(String idx) throws ApiException {
        String token = api2Token(idx);
        String body = "token=" + encode(token) + "&id=" + encode(idx);
        String json = client.request(
                "POST", String.format(Locale.US, API2_FEED_URL, encode(idx)),
                FORM_HEADERS, body);
        try {
            return parseApi2Detail(new JSONObject(json), idx);
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_data, invalidJson);
        }
    }

    private String api2Token(String idx) throws ApiException {
        CachedToken cached = tokenCache.get(idx);
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            return cached.value;
        }
        String json = client.request("POST",
                String.format(Locale.US, API2_TOKEN_URL, encode(idx)), FORM_HEADERS, "");
        try {
            JSONObject root = new JSONObject(json);
            JSONArray obs = root.optJSONObject("rxs") == null
                    ? null : root.optJSONObject("rxs").optJSONArray("obs");
            String token = null;
            if (obs != null) {
                for (int i = 0; i < obs.length() && token == null; i++) {
                    JSONObject entry = obs.optJSONObject(i);
                    if (entry == null) {
                        continue;
                    }
                    JSONObject msg = entry.optJSONObject("msg");
                    if (msg != null) {
                        token = msg.optString("token", null);
                    }
                }
            }
            if (token == null || token.isEmpty()) {
                throw new ApiException(ApiException.INVALID_RESPONSE,
                        R.string.error_station_token);
            }
            tokenCache.put(idx, new CachedToken(token, System.currentTimeMillis() + TOKEN_TTL_MS));
            return token;
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_token, invalidJson);
        }
    }

    // -----------------------------------------------------------------------
    // airnet flow (station ids starting with A)
    // -----------------------------------------------------------------------

    private StationDetail buildAirnetDetail(String idx) throws ApiException {
        String key = airnetKey();
        String id = encode(idx.substring(1));
        String hourlyJson = client.request("GET",
                String.format(Locale.US, AIRNET_HOURLY_URL, id, encode(key)),
                BROWSER_HEADERS, null);
        JSONObject hourly;
        try {
            hourly = new JSONObject(hourlyJson);
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_data, invalidJson);
        }
        JSONObject cwop;
        try {
            String cwopJson = client.request("GET",
                    String.format(Locale.US, AIRNET_CWOP_URL, id, encode(key)),
                    BROWSER_HEADERS, null);
            cwop = new JSONObject(cwopJson);
        } catch (ApiException | JSONException weatherUnavailable) {
            cwop = null;
        }
        try {
            return parseAirnetDetail(hourly, cwop, idx);
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_data, invalidJson);
        }
    }

    private String airnetKey() throws ApiException {
        if (airnetKey != null && airnetKey.expiresAt > System.currentTimeMillis()) {
            return airnetKey.value;
        }
        String json = client.request("POST", AIRNET_TOKEN_URL, FORM_HEADERS, "");
        try {
            String key = new JSONObject(json).optString("key", "");
            if (key.isEmpty()) {
                throw new ApiException(ApiException.INVALID_RESPONSE,
                        R.string.error_airnet_key);
            }
            airnetKey = new CachedToken(key, System.currentTimeMillis() + AIRNET_TTL_MS);
            return key;
        } catch (JSONException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_airnet_key, invalidJson);
        }
    }

    // -----------------------------------------------------------------------
    // Parsing (package-visible for unit tests)
    // -----------------------------------------------------------------------

    static List<Station> parseStations(JSONObject root, double originLat, double originLon)
            throws JSONException {
        List<Station> stations = new ArrayList<>();
        JSONArray data = root.optJSONArray("data");
        if (data == null) {
            return stations;
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.optJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONArray geo = item.optJSONArray("geo");
            if (geo == null || geo.length() < 2) {
                continue;
            }
            double lat = geo.optDouble(0);
            double lon = geo.optDouble(1);
            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                continue;
            }
            String aqiText = item.optString("aqi", "").trim();
            double aqi = aqiText.isEmpty() || aqiText.equals("-")
                    ? Double.NaN : parseDouble(aqiText);
            stations.add(new Station(
                    item.optString("idx", ""),
                    item.optString("name", ""),
                    lat, lon, aqi,
                    item.optString("utime", ""),
                    distanceMeters(originLat, originLon, lat, lon)));
        }
        stations.sort(Comparator.comparingDouble(Station::getDistanceMeters));
        return stations;
    }

    static StationDetail parseApi2Detail(JSONObject root, String idx) throws JSONException {
        JSONObject rxs = root.optJSONObject("rxs");
        JSONArray observations = rxs == null ? null : rxs.optJSONArray("obs");
        if (observations == null) {
            return null;
        }
        JSONObject msg = null;
        for (int i = 0; i < observations.length(); i++) {
            JSONObject entry = observations.optJSONObject(i);
            if (entry == null) {
                continue;
            }
            Object candidate = entry.opt("msg");
            if (candidate instanceof JSONObject) {
                msg = (JSONObject) candidate;
                if (msg.optJSONObject("iaqi") != null) {
                    break;
                }
            }
        }
        if (msg == null || msg.optJSONObject("iaqi") == null
                || msg.optJSONObject("obs") == null) {
            return null;
        }

        JSONObject time = msg.optJSONObject("time");
        String timezone = time == null ? "+00:00" : time.optString("tz", "+00:00");

        List<Series> decoded = new ArrayList<>();
        JSONObject obs = msg.optJSONObject("obs");
        Iterator<String> keys = obs.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONObject series = obs.optJSONObject(key);
            if (series != null) {
                decoded.add(new Series(key, decodeApi2Series(series, timezone)));
            }
        }

        Aligned aligned = alignSeries(decoded, SERIES_HOURS, SERIES_STEP);

        Map<String, Double> current = new LinkedHashMap<>();
        JSONObject iaqi = msg.optJSONObject("iaqi");
        double aqi = msg.optDouble("aqi", Double.NaN);
        if (!Double.isNaN(aqi)) {
            current.put("aqi", aqi);
        }
        Iterator<String> iaqiKeys = iaqi.keys();
        while (iaqiKeys.hasNext()) {
            String key = iaqiKeys.next();
            JSONObject entry = iaqi.optJSONObject(key);
            if (entry != null) {
                current.put(key, entry.optDouble("v", Double.NaN));
            }
        }

        Map<String, List<Double>> series = new LinkedHashMap<>(aligned.series);
        if (!series.containsKey("aqi")) {
            List<Double> aqiSeries = combineSubIndices(series);
            if (!aqiSeries.isEmpty()) {
                Map<String, List<Double>> withAqi = new LinkedHashMap<>();
                withAqi.put("aqi", aqiSeries);
                withAqi.putAll(series);
                series = withAqi;
            }
        }

        long updatedAt = time == null ? 0 : time.optLong("v", 0);
        if (updatedAt == 0) {
            updatedAt = latestTimestamp(decoded);
        }
        JSONObject city = msg.optJSONObject("city");
        String name = city == null ? "" : city.optString("name", "");

        return new StationDetail(idx, name, aqi, aligned.start, aligned.step,
                updatedAt, current, series);
    }

    static StationDetail parseAirnetDetail(JSONObject hourly, JSONObject cwop, String idx)
            throws JSONException {
        JSONObject data = hourly.optJSONObject("data");
        JSONArray pm25 = data == null ? null : data.optJSONArray("pm25");
        JSONArray pm10 = data == null ? null : data.optJSONArray("pm10");
        JSONArray pm1 = data == null ? null : data.optJSONArray("pm1");
        if (pm25 == null || pm10 == null || pm25.length() == 0 || pm10.length() == 0) {
            return null;
        }

        List<Series> decoded = new ArrayList<>();
        decoded.add(new Series("pm25", hourlySeries(pm25, true)));
        decoded.add(new Series("pm10", hourlySeries(pm10, false)));
        if (pm1 != null && pm1.length() > 0) {
            decoded.add(new Series("pm1", hourlySeries(pm1, true)));
        }
        addWeatherSeries(decoded, cwop);

        Aligned aligned = alignSeries(decoded, SERIES_HOURS, SERIES_STEP);

        Map<String, List<Double>> series = new LinkedHashMap<>();
        series.put("aqi", combineMax(aligned.series, "pm25", "pm10", "pm1"));
        series.putAll(aligned.series);

        JSONObject feed = hourly.optJSONObject("feed");
        Map<String, Double> current = new LinkedHashMap<>();
        double pm25Index = feedIndex(feed, "pm25", true);
        double pm10Index = feedIndex(feed, "pm10", false);
        double pm1Index = feedIndex(feed, "pm1", true);
        if (!Double.isNaN(pm25Index)) {
            current.put("pm25", pm25Index);
        }
        if (!Double.isNaN(pm10Index)) {
            current.put("pm10", pm10Index);
        }
        if (!Double.isNaN(pm1Index)) {
            current.put("pm1", pm1Index);
        }
        double aqi = maxOf(pm25Index, pm10Index, pm1Index);
        if (!Double.isNaN(aqi) && aqi > 0) {
            current.put("aqi", aqi);
        }

        addLatestWeather(current, cwop);

        JSONObject meta = hourly.optJSONObject("meta");
        String name = meta == null ? "" : meta.optString("name", "");
        long updatedAt = meta == null ? 0 : meta.optLong("utime", 0);

        return new StationDetail(idx, name, aqi, aligned.start, aligned.step,
                updatedAt, current, series);
    }

    static List<Point> decodeApi2Series(JSONObject series, String timezone)
            throws JSONException {
        int scale = series.optInt("m", 1);
        String start = series.optString("s", "");
        JSONArray values = series.optJSONArray("v");
        if (values == null || values.length() == 0) {
            return Collections.emptyList();
        }
        List<Point> points = new ArrayList<>();
        long time = epochSeconds(start, timezone);
        double value = values.optDouble(0, Double.NaN) / scale;
        points.add(new Point(time, value));
        for (int i = 1; i < values.length(); i++) {
            JSONArray delta = values.optJSONArray(i);
            if (delta == null) {
                continue;
            }
            value += delta.optDouble(0, 0);
            time += delta.optLong(1, 0);
            points.add(new Point(time, value / scale));
        }
        Collections.reverse(points);
        return points;
    }

    static Aligned alignSeries(List<Series> input, int hours, int step) {
        long maxTime = Long.MIN_VALUE;
        for (Series series : input) {
            for (Point point : series.points) {
                maxTime = Math.max(maxTime, point.t);
            }
        }
        if (maxTime == Long.MIN_VALUE) {
            return new Aligned(0, step, Collections.emptyMap());
        }
        long start = maxTime - (long) (hours - 1) * step;
        Map<String, List<Double>> aligned = new LinkedHashMap<>();
        for (Series series : input) {
            Map<Long, Double> byTime = new HashMap<>();
            for (Point point : series.points) {
                byTime.put(point.t, point.v);
            }
            List<Double> values = new ArrayList<>();
            for (long t = start; t <= maxTime; t += step) {
                Double value = byTime.get(t);
                values.add(value == null ? Double.NaN : value);
            }
            aligned.put(series.key, values);
        }
        return new Aligned(start, step, aligned);
    }

    static double pm25ToAqi(double concentration) {
        return interpolate(new double[][]{
                {0, 12.0, 0, 50},
                {12.1, 35.4, 51, 100},
                {35.5, 55.4, 101, 150},
                {55.5, 150.4, 151, 200},
                {150.5, 250.4, 201, 300},
                {250.5, 350.4, 301, 400},
                {350.5, 500.4, 401, 500},
        }, concentration);
    }

    static double pm10ToAqi(double concentration) {
        return interpolate(new double[][]{
                {0, 54, 0, 50},
                {55, 154, 51, 100},
                {155, 254, 101, 150},
                {255, 354, 151, 200},
                {355, 424, 201, 300},
                {425, 504, 301, 400},
                {505, 604, 401, 500},
        }, concentration);
    }

    private static double interpolate(double[][] breakpoints, double concentration) {
        for (double[] band : breakpoints) {
            if (concentration <= band[1]) {
                return Math.round(((band[3] - band[2]) / (band[1] - band[0]))
                        * (concentration - band[0]) + band[2]);
            }
        }
        return 500;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static List<Point> hourlySeries(JSONArray entries, boolean pm25Scale)
            throws JSONException {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.optJSONObject(i);
            if (entry == null) {
                continue;
            }
            double mean = entry.optDouble("mean", Double.NaN);
            if (Double.isNaN(mean)) {
                continue;
            }
            long time = epochSeconds(entry.optString("time", ""), "+00:00");
            points.add(new Point(time, pm25Scale ? pm25ToAqi(mean) : pm10ToAqi(mean)));
        }
        return points;
    }

    private static void addWeatherSeries(List<Series> decoded, JSONObject cwop)
            throws JSONException {
        JSONObject data = cwop == null ? null : cwop.optJSONObject("data");
        JSONObject entries = data == null ? null : data.optJSONObject("data");
        if (entries == null) {
            return;
        }
        List<String> times = new ArrayList<>();
        Iterator<String> keys = entries.keys();
        while (keys.hasNext()) {
            times.add(keys.next());
        }
        Collections.sort(times);

        List<Point> humidity = new ArrayList<>();
        List<Point> pressure = new ArrayList<>();
        List<Point> temperature = new ArrayList<>();
        List<Point> wind = new ArrayList<>();
        for (String time : times) {
            JSONObject entry = entries.optJSONObject(time);
            if (entry == null) {
                continue;
            }
            long epoch = epochSeconds(time, "+00:00");
            humidity.add(new Point(epoch, entry.optDouble("h", 0)));
            pressure.add(new Point(epoch, entry.optDouble("p", 0)));
            temperature.add(new Point(epoch, entry.optDouble("t", 0)));
            wind.add(new Point(epoch, entry.optDouble("ws", 0)));
        }
        decoded.add(new Series("h", humidity));
        decoded.add(new Series("p", pressure));
        decoded.add(new Series("t", temperature));
        decoded.add(new Series("w", wind));
    }

    private static void addLatestWeather(Map<String, Double> current, JSONObject cwop)
            throws JSONException {
        JSONObject data = cwop == null ? null : cwop.optJSONObject("data");
        JSONObject entries = data == null ? null : data.optJSONObject("data");
        if (entries == null) {
            return;
        }
        String latest = null;
        Iterator<String> keys = entries.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (latest == null || key.compareTo(latest) > 0) {
                latest = key;
            }
        }
        JSONObject entry = latest == null ? null : entries.optJSONObject(latest);
        if (entry == null) {
            return;
        }
        current.put("h", entry.optDouble("h", 0));
        current.put("p", entry.optDouble("p", 0));
        current.put("t", entry.optDouble("t", 0));
        current.put("w", entry.optDouble("ws", 0));
    }

    private static double feedIndex(JSONObject feed, String key, boolean pm25Scale) {
        if (feed == null) {
            return Double.NaN;
        }
        JSONArray pair = feed.optJSONArray(key);
        if (pair == null || pair.length() < 2) {
            return Double.NaN;
        }
        double concentration = pair.optDouble(1, Double.NaN) / 100;
        if (Double.isNaN(concentration)) {
            return Double.NaN;
        }
        return pm25Scale ? pm25ToAqi(concentration) : pm10ToAqi(concentration);
    }

    private static List<Double> combineSubIndices(Map<String, List<Double>> series) {
        String[] keys = {"pm25", "pm10", "o3", "no2", "so2", "co"};
        List<List<Double>> found = new ArrayList<>();
        for (String key : keys) {
            List<Double> values = series.get(key);
            if (values != null) {
                found.add(values);
            }
        }
        if (found.isEmpty()) {
            return Collections.emptyList();
        }
        int size = found.get(0).size();
        List<Double> combined = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            double max = Double.NaN;
            for (List<Double> values : found) {
                if (i < values.size() && !values.get(i).isNaN()) {
                    max = Double.isNaN(max) ? values.get(i) : Math.max(max, values.get(i));
                }
            }
            combined.add(max);
        }
        return combined;
    }

    private static List<Double> combineMax(Map<String, List<Double>> series, String... keys) {
        List<Double> first = null;
        for (String key : keys) {
            if (series.get(key) != null) {
                first = series.get(key);
                break;
            }
        }
        if (first == null) {
            return Collections.emptyList();
        }
        List<Double> combined = new ArrayList<>();
        for (int i = 0; i < first.size(); i++) {
            double max = Double.NaN;
            for (String key : keys) {
                List<Double> values = series.get(key);
                if (values != null && i < values.size() && !values.get(i).isNaN()) {
                    max = Double.isNaN(max) ? values.get(i) : Math.max(max, values.get(i));
                }
            }
            combined.add(max);
        }
        return combined;
    }

    private static long latestTimestamp(List<Series> series) {
        long latest = 0;
        for (Series item : series) {
            for (Point point : item.points) {
                latest = Math.max(latest, point.t);
            }
        }
        return latest;
    }

    static long epochSeconds(String text, String timezone) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String value = text.trim().replace(' ', 'T');
        try {
            OffsetDateTime dateTime;
            if (value.endsWith("Z") || value.length() > 19) {
                dateTime = OffsetDateTime.parse(value);
            } else {
                String offset = (timezone == null || timezone.isEmpty()) ? "+00:00" : timezone;
                dateTime = OffsetDateTime.parse(value + offset);
            }
            return dateTime.toEpochSecond();
        } catch (DateTimeParseException invalidDate) {
            return 0;
        }
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

    private static double maxOf(double... values) {
        double max = Double.NaN;
        for (double value : values) {
            if (Double.isNaN(value)) {
                continue;
            }
            max = Double.isNaN(max) ? value : Math.max(max, value);
        }
        return max;
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException invalidNumber) {
            return Double.NaN;
        }
    }

    private static String encode(String value) throws ApiException {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_request_prepare, impossible);
        }
    }

    private static Map<String, String> browserHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("Referer", "https://aqicn.org/");
        headers.put("User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36");
        return headers;
    }

    private static Map<String, String> formHeaders() {
        Map<String, String> headers = new HashMap<>(browserHeaders());
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    // -----------------------------------------------------------------------
    // Small value types
    // -----------------------------------------------------------------------

    static final class Point {

        final long t;
        final double v;

        Point(long t, double v) {
            this.t = t;
            this.v = v;
        }
    }

    static final class Series {

        final String key;
        final List<Point> points;

        Series(String key, List<Point> points) {
            this.key = key;
            this.points = points;
        }
    }

    static final class Aligned {

        final long start;
        final int step;
        final Map<String, List<Double>> series;

        Aligned(long start, int step, Map<String, List<Double>> series) {
            this.start = start;
            this.step = step;
            this.series = series;
        }
    }

    private static final class CachedToken {

        final String value;
        final long expiresAt;

        CachedToken(String value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
