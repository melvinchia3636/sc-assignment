package com.groupone.assignment.lib;

import com.groupone.assignment.repositories.ApiException;

import java.util.HashMap;
import java.util.Map;

/**
 * Remembers values for a fixed time, mirroring the scraper API's TTL cache.
 * A cached null is distinct from a cache miss.
 */
public final class TtlCache<T> {

    private static final class Entry<T> {
        final T value;
        final long expiresAt;

        Entry(T value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    public interface Loader<T> {
        T load() throws ApiException;
    }

    private final Map<String, Entry<T>> entries = new HashMap<>();

    public boolean has(String key) {
        Entry<T> entry = entries.get(key);
        if (entry == null) {
            return false;
        }
        if (entry.expiresAt <= Time.nowMillis()) {
            entries.remove(key);
            return false;
        }
        return true;
    }

    public T get(String key) {
        if (!has(key)) {
            return null;
        }
        return entries.get(key).value;
    }

    public void set(String key, T value, long ttlMs) {
        entries.put(key, new Entry<>(value, Time.nowMillis() + ttlMs));
    }

    public T getOrLoad(String key, long ttlMs, Loader<T> loader) throws ApiException {
        if (has(key)) {
            return get(key);
        }
        T value = loader.load();
        set(key, value, ttlMs);
        return value;
    }
}
