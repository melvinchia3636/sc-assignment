package dev.melvinchia3636.airngreen.scrapers.lib;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.repositories.ApiException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Upstream HTTP helpers for the WAQI scrapers.
 */
public final class Upstream {

    private static final long TIMEOUT_SECONDS = 15;
    private static final MediaType FORM =
            MediaType.parse("application/x-www-form-urlencoded");
    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    public static final Map<String, String> BROWSER_HEADERS;
    public static final Map<String, String> FORM_HEADERS;

    static {
        Map<String, String> browser = new HashMap<>();
        browser.put("Accept", "*/*");
        browser.put("Accept-Language", "en-US,en;q=0.9");
        browser.put("Cache-Control", "no-cache");
        browser.put("Pragma", "no-cache");
        browser.put("Referer", "https://aqicn.org/");
        browser.put("User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36");
        BROWSER_HEADERS = Collections.unmodifiableMap(browser);

        Map<String, String> form = new HashMap<>(browser);
        form.put("Content-Type", "application/x-www-form-urlencoded");
        FORM_HEADERS = Collections.unmodifiableMap(form);
    }

    private Upstream() {
    }

    /** Request details for one upstream call. */
    public static final class Options {

        private final String method;
        private final Map<String, String> headers;
        private final String body;
        private final MediaType mediaType;

        private Options(String method, Map<String, String> headers, String body,
                        MediaType mediaType) {
            this.method = method;
            this.headers = headers;
            this.body = body;
            this.mediaType = mediaType;
        }

        public static Options get() {
            return new Options("GET", null, null, null);
        }

        public static Options get(Map<String, String> headers) {
            return new Options("GET", headers, null, null);
        }

        public static Options post(Map<String, String> headers, String body) {
            return new Options("POST", headers, body, FORM);
        }

        public static Options postJson(String body) {
            return new Options("POST", null, body, JSON);
        }
    }

    /** Fetches a URL and returns the decoded text, mapping failures to ApiException. */
    public static String fetchText(String url) throws ApiException {
        return fetchText(url, Options.get());
    }

    public static String fetchText(String url, Options options) throws ApiException {
        Request.Builder builder = new Request.Builder().url(url);
        if (options.headers != null) {
            for (Map.Entry<String, String> header : options.headers.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
        }

        if ("GET".equals(options.method)) {
            builder.get();
        } else {
            RequestBody requestBody = options.body == null
                    ? RequestBody.create(new byte[0], null)
                    : RequestBody.create(options.body, options.mediaType);
            builder.method(options.method, requestBody);
        }

        try (Response response = CLIENT.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) {
                throw ApiException.http(response.code());
            }
            ResponseBody responseBody = response.body();
            return responseBody == null ? "" : responseBody.string();
        } catch (IOException failure) {
            throw ApiException.network(failure);
        }
    }

    /** Fetches JSON and validates that it deserialises into the expected shape. */
    public static <T> T fetchJson(String url, Class<T> type, Options options)
            throws ApiException {
        String text = fetchText(url, options);
        try {
            T parsed = GSON.fromJson(text, type);
            if (parsed == null) {
                throw new JsonParseException("Empty payload");
            }
            return parsed;
        } catch (JsonParseException invalidJson) {
            throw new ApiException(ApiException.INVALID_RESPONSE,
                    R.string.error_station_data, invalidJson);
        }
    }
}
