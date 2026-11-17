package com.groupone.assignment.net;

import com.groupone.assignment.data.ApiException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Small OkHttp wrapper. Every failure becomes an {@link ApiException} so the UI
 * never has to deal with raw IO exceptions or stack traces.
 */
public class HttpJsonClient {

    private static final MediaType FORM =
            MediaType.parse("application/x-www-form-urlencoded");

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .build();

    public String getJson(String url) throws ApiException {
        return request("GET", url, null, null);
    }

    public String request(String method, String url, Map<String, String> headers, String body)
            throws ApiException {
        Request.Builder builder = new Request.Builder().url(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
        }

        if ("GET".equals(method)) {
            builder.get();
        } else {
            RequestBody requestBody = body == null
                    ? RequestBody.create(new byte[0], null)
                    : RequestBody.create(body, FORM);
            builder.method(method, requestBody);
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
}
