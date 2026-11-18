package dev.melvinchia3636.airngreen.scrapers.lib;

import android.content.Context;

import androidx.annotation.StringRes;

import dev.melvinchia3636.airngreen.R;

import java.io.IOException;

/**
 * A failure that can be shown to the user. Carries the HTTP status (or
 * {@link #NETWORK_ERROR}) and a string resource that is safe to display.
 */
public class ApiException extends Exception {

    public static final int NETWORK_ERROR = 0;
    public static final int INVALID_RESPONSE = -1;

    private final int statusCode;
    @StringRes
    private final int messageRes;
    private final String detail;

    public ApiException(int statusCode, @StringRes int messageRes) {
        this(statusCode, messageRes, null, null);
    }

    public ApiException(int statusCode, @StringRes int messageRes, Throwable cause) {
        this(statusCode, messageRes, null, cause);
    }

    public ApiException(int statusCode, @StringRes int messageRes, String detail,
                        Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
        this.messageRes = messageRes;
        this.detail = detail;
    }

    public static ApiException network(IOException cause) {
        return new ApiException(NETWORK_ERROR, R.string.error_offline, cause);
    }

    public static ApiException http(int statusCode) {
        return new ApiException(statusCode, messageFor(statusCode));
    }

    public static ApiException http(int statusCode, String detail) {
        return new ApiException(statusCode, messageFor(statusCode), detail, null);
    }

    private static int messageFor(int statusCode) {
        if (statusCode == 404) {
            return R.string.error_no_data;
        }
        if (statusCode == 429) {
            return R.string.error_too_many;
        }
        if (statusCode >= 500) {
            return R.string.error_unavailable;
        }
        return R.string.error_unexpected;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** Server-supplied detail for the failure, or {@code null} when none. */
    public String getDetail() {
        return detail;
    }

    @StringRes
    public int getMessageRes() {
        return messageRes;
    }

    public String message(Context context) {
        if (messageRes == R.string.error_unexpected) {
            return context.getString(messageRes, statusCode);
        }
        return context.getString(messageRes);
    }

    public boolean isNetworkError() {
        return statusCode == NETWORK_ERROR;
    }
}
