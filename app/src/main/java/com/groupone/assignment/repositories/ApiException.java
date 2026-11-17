package com.groupone.assignment.repositories;

import android.content.Context;

import androidx.annotation.StringRes;

import com.groupone.assignment.R;

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

    public ApiException(int statusCode, @StringRes int messageRes) {
        this(statusCode, messageRes, null);
    }

    public ApiException(int statusCode, @StringRes int messageRes, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
        this.messageRes = messageRes;
    }

    public static ApiException network(IOException cause) {
        return new ApiException(NETWORK_ERROR, R.string.error_offline, cause);
    }

    public static ApiException http(int statusCode) {
        return new ApiException(statusCode, messageFor(statusCode));
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
