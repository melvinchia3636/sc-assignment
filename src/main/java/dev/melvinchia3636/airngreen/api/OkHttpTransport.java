package dev.melvinchia3636.airngreen.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * {@link HttpTransport} backed by OkHttp.
 */
public final class OkHttpTransport implements HttpTransport {

    private static final long CONNECT_TIMEOUT_SECONDS = 5;
    private static final long READ_TIMEOUT_SECONDS = 10;
    private static final String USER_AGENT = "AirGreen/1.0";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    @Override
    public Response get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .get()
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            return new Response(response.code(), body);
        }
    }
}
