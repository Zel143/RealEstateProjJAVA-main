import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A utility class for caching expensive operations with time-based expiration
 * @param <K> The key type
 * @param <V> The value type
 */
public class PerformanceCache<K, V> {
    private final Map<K, CacheEntry<V>> cache;
    private final int maxEntries;
    private final long expirationTimeMs;
    
    public PerformanceCache(int maxEntries, long expirationTimeMs) {
        this.maxEntries = maxEntries;
        this.expirationTimeMs = expirationTimeMs;
        
        // Use LinkedHashMap with removeEldestEntry to implement LRU cache
        this.cache = new LinkedHashMap<K, CacheEntry<V>>(maxEntries + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                return size() > maxEntries;
            }
        };
    }
    
    /**
     * Gets a value from cache, computing it if not present or expired
     * @param key The cache key
     * @param valueSupplier The function to compute the value if not cached
     * @return The cached or computed value
     */
    public synchronized V get(K key, Supplier<V> valueSupplier) {
        CacheEntry<V> entry = cache.get(key);
        long now = System.currentTimeMillis();
        
        // Check if entry exists and is still valid
        if (entry != null && now - entry.timestamp < expirationTimeMs) {
            return entry.value;
        }
        
        // Compute new value and store in cache
        V value = valueSupplier.get();
        cache.put(key, new CacheEntry<>(value, now));
        return value;
    }
    
    /**
     * Invalidate a specific cache entry
     * @param key The key to invalidate
     */
    public synchronized void invalidate(K key) {
        cache.remove(key);
    }
    
    /**
     * Clear the entire cache
     */
    public synchronized void clear() {
        cache.clear();
    }
    
    /**
     * Gets the current cache size
     * @return Number of entries in cache
     */
    public synchronized int size() {
        return cache.size();
    }
    
    /**
     * Helper class to store a value with its timestamp
     */
    private static class CacheEntry<T> {
        final T value;
        final long timestamp;
        
        CacheEntry(T value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
