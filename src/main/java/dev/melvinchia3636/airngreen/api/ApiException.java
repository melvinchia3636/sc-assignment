package dev.melvinchia3636.airngreen.api;

/**
 * Failure raised when the air-quality service cannot provide usable data.
 *
 * <p>The message is safe to show to a user: it never contains a stack trace or
 * an upstream payload.</p>
 */
public class ApiException extends Exception {

    private static final int NETWORK_FAILURE = 0;
    private static final int SERVER_ERROR_FLOOR = 500;

    private final int statusCode;

    /**
     * Creates a failure with a user-facing message.
     *
     * @param statusCode HTTP status, or {@link #NETWORK_FAILURE} when offline
     * @param message    short explanation for the user
     */
    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * HTTP status that caused the failure, or 0 for a network error.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Reports whether the service itself failed rather than the request.
     *
     * @return true for 5xx responses or a network failure
     */
    public boolean isServiceFailure() {
        return statusCode == NETWORK_FAILURE || statusCode >= SERVER_ERROR_FLOOR;
    }

    /**
     * Default friendly message for an HTTP status.
     *
     * @param statusCode response status
     * @return a sentence suitable for the status bar
     */
    public static String messageFor(int statusCode) {
        if (statusCode == NETWORK_FAILURE) {
            return "Cannot reach the air-quality service. Check your connection.";
        }
        if (statusCode == 404) {
            return "Station not found. Check the station id and try again.";
        }
        if (statusCode == 429) {
            return "Too many requests. Please wait a moment and try again.";
        }
        if (statusCode >= SERVER_ERROR_FLOOR) {
            return "The air-quality service is unavailable. "
                    + "Showing the last saved reading.";
        }

        return "Unexpected response from the air-quality service (HTTP "
                + statusCode + ").";
    }
}
