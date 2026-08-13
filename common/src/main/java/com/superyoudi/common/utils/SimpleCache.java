package com.superyoudi.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author Sijayyx
 */
public class SimpleCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    public V getOrCreate(K key, Supplier<V> supplier) {
        return cache.computeIfAbsent(key, k -> supplier.get());
    }

    public void updateData(K key, V value) {
        cache.put(key, value);
    }


    public void clearCache() {
        cache.clear();
    }
}
