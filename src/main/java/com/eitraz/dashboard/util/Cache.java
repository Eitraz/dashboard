package com.eitraz.dashboard.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

public class Cache<T> {
    private final Duration timeout;
    private LocalDateTime cacheTime;
    private T cache;

    public Cache(Duration timeout) {
        Objects.requireNonNull(timeout, "Timeout is required");
        this.timeout = timeout;
        cacheTime = LocalDateTime.now();
    }

    public synchronized T get(Supplier<T> supplier) {
        if (cache == null || LocalDateTime.now().isAfter(cacheTime.plus(timeout))) {
            cache = supplier.get();
            cacheTime = LocalDateTime.now();
        }
        return cache;
    }
}
