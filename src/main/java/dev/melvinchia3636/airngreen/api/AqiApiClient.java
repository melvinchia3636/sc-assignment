package dev.melvinchia3636.airngreen.api;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import okhttp3.HttpUrl;

import dev.melvinchia3636.airngreen.model.StationReading;
import dev.melvinchia3636.airngreen.model.StationSummary;

/**
 * Read-only client for the air-quality service.
 *
 * <p>The service wraps WAQI data. Every method turns transport and payload
 * problems into {@link ApiException} so callers only deal with one failure
 * type.</p>
 */
public final class AqiApiClient {

    /** Public deployment used unless another base URL is supplied. */
    public static final String DEFAULT_BASE_URL =
            "https://aqi-api-melvin.fly.dev";

    private static final int NO_CONTENT = 204;

    private final HttpTransport transport;
    private final AqiJsonParser parser;
    private final HttpUrl baseUrl;

    /**
     * Creates a client for the default deployment.
     *
     * @param transport HTTP implementation, real or stubbed
     */
    public AqiApiClient(HttpTransport transport) {
        this(transport, DEFAULT_BASE_URL);
    }

    /**
     * Creates a client for a specific deployment.
     *
     * @param transport HTTP implementation
     * @param baseUrl   service root such as {@code https://example.test}
     */
    public AqiApiClient(HttpTransport transport, String baseUrl) {
        this.transport = transport;
        this.parser = new AqiJsonParser();
        this.baseUrl = HttpUrl.get(baseUrl);
    }

    /**
     * Finds the nearest reporting station to a point.
     *
     * @param latitude  degrees north
     * @param longitude degrees east
     * @return the station's current reading and recent history
     * @throws ApiException when the service cannot answer
     */
    public StationReading nearestStation(double latitude, double longitude)
            throws ApiException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("nearest-station")
                .addQueryParameter("lat", Double.toString(latitude))
                .addQueryParameter("lon", Double.toString(longitude))
                .build();
        return fetch(url.toString(), parser::parseStation);
    }

    /**
     * Loads one station by its upstream id.
     *
     * @param idx station id such as {@code 2609} or {@code A2820325}
     * @return the station's current reading and recent history
     * @throws ApiException when the station is unknown or the service fails
     */
    public StationReading station(String idx) throws ApiException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("station")
                .addPathSegment(idx)
                .build();
        return fetch(url.toString(), parser::parseStation);
    }

    /**
     * Loads the public station directory.
     *
     * @return every station known to the service
     * @throws ApiException when the service cannot answer
     */
    public List<StationSummary> stations() throws ApiException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("stations")
                .build();
        return fetch(url.toString(), parser::parseStations);
    }

    /**
     * Loads stations inside a map bounding box.
     *
     * @param south southern latitude in degrees
     * @param west  western longitude in degrees
     * @param north northern latitude in degrees
     * @param east  eastern longitude in degrees
     * @return stations inside the box
     * @throws ApiException when the service cannot answer
     */
    public List<StationSummary> stations(double south, double west,
                                         double north, double east)
            throws ApiException {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("stations")
                .addQueryParameter("south", Double.toString(south))
                .addQueryParameter("west", Double.toString(west))
                .addQueryParameter("north", Double.toString(north))
                .addQueryParameter("east", Double.toString(east))
                .build();
        return fetch(url.toString(), parser::parseStations);
    }

    /**
     * Checks the service's liveness endpoint.
     *
     * @return true when the service answers with 204
     */
    public boolean isAlive() {
        HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("Index")
                .addPathSegment("liveness")
                .build();
        try {
            return transport.get(url.toString()).statusCode() == NO_CONTENT;
        } catch (IOException unreachable) {
            return false;
        }
    }

    private <T> T fetch(String url, Function<String, T> reader)
            throws ApiException {
        String body = read(url);

        try {
            return reader.apply(body);
        } catch (RuntimeException malformed) {
            throw new ApiException(200, "The service returned an unexpected response.");
        }
    }

    private String read(String url) throws ApiException {
        HttpTransport.Response response = send(url);
        if (response.statusCode() / 100 != 2) {
            throw failure(response);
        }
        return response.body();
    }

    private HttpTransport.Response send(String url) throws ApiException {
        try {
            return transport.get(url);
        } catch (IOException unreachable) {
            throw new ApiException(0, ApiException.messageFor(0));
        }
    }

    private ApiException failure(HttpTransport.Response response) {
        String message = parser.errorMessage(response.body());
        return new ApiException(response.statusCode(),
                message == null ? ApiException.messageFor(response.statusCode()) : message);
    }
}
