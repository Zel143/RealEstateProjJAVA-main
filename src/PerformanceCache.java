import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class for caching expensive operations with time-based expiration
 * @param <K> The key type
 * @param <V> The value type
 */
public class PerformanceCache<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PerformanceCache.class.getName());
    
    private final Map<K, CacheEntry<V>> cache;
    private final long expirationTimeMs;
    private final boolean concurrent;
    private final transient ReentrantReadWriteLock lock;
    
    /**
     * Create a new cache with the specified size and expiration time
     * 
     * @param maxEntries Maximum number of entries in the cache
     * @param expirationTimeMs Time in milliseconds after which entries expire
     */
    public PerformanceCache(int maxEntries, long expirationTimeMs) {
        this.expirationTimeMs = expirationTimeMs;
        
        // Use appropriate implementation based on cache size
        if (maxEntries <= 100) {
            this.concurrent = false;
            this.lock = new ReentrantReadWriteLock();
            this.cache = new LinkedHashMap<K, CacheEntry<V>>(maxEntries + 1, 0.75f, true) {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                    return size() > maxEntries;
                }
            };
        } else {
            this.concurrent = true;
            this.lock = null; // Not needed with ConcurrentHashMap
            this.cache = new ConcurrentHashMap<>(maxEntries);
        }
    }
    
    /**
     * Gets a value from cache, computing it if not present or expired
     * @param key The cache key
     * @param valueSupplier The function to compute the value if not cached
     * @return The cached or computed value
     */
    public V get(K key, Supplier<V> valueSupplier) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        Objects.requireNonNull(valueSupplier, "Value supplier cannot be null");
        
        if (concurrent) {
            return getConcurrent(key, valueSupplier);
        } else {
            return getSynchronized(key, valueSupplier);
        }
    }
    
    /**
     * Get value from concurrent cache implementation
     */
    private V getConcurrent(K key, Supplier<V> valueSupplier) {
        // Periodically clean expired entries (not on every call for performance)
        if (Math.random() < 0.05) { // ~5% chance to trigger cleanup
            cleanupExpiredEntries();
        }
        
        // Check if we have a valid entry
        CacheEntry<V> entry = cache.get(key);
        if (isValidEntry(entry)) {
            return entry.value;
        }
        
        // Compute and cache the value
        V value = valueSupplier.get();
        cache.put(key, new CacheEntry<>(value));
        return value;
    }
    
    /**
     * Get value from synchronized cache implementation
     */
    private V getSynchronized(K key, Supplier<V> valueSupplier) {
        lock.readLock().lock();
        try {
            // Check if we have a valid entry
            CacheEntry<V> entry = cache.get(key);
            if (isValidEntry(entry)) {
                return entry.value;
            }
        } finally {
            lock.readLock().unlock();
        }
        
        // If not found or expired, compute value with write lock
        lock.writeLock().lock();
        try {
            // Double-check since another thread might have updated while we waited
            CacheEntry<V> entry = cache.get(key);
            if (isValidEntry(entry)) {
                return entry.value;
            }
            
            // Compute new value
            V value = valueSupplier.get();
            cache.put(key, new CacheEntry<>(value));
            return value;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error computing cached value", e);
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Check if an entry is valid (not null and not expired)
     */
    private boolean isValidEntry(CacheEntry<V> entry) {
        if (entry == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        return now - entry.timestamp < expirationTimeMs;
    }
    
    /**
     * Cleanup expired entries
     */
    private void cleanupExpiredEntries() {
        if (!concurrent) return; // Only used with ConcurrentHashMap
        
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp >= expirationTimeMs);
    }
    
    /**
     * Invalidate a specific cache entry
     */
    public void invalidate(K key) {
        Objects.requireNonNull(key, "Cache key cannot be null");
        
        if (concurrent) {
            cache.remove(key);
        } else {
            lock.writeLock().lock();
            try {
                cache.remove(key);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Clear the entire cache
     */
    public void clear() {
        if (concurrent) {
            cache.clear();
        } else {
            lock.writeLock().lock();
            try {
                cache.clear();
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Gets the current cache size
     * @return Number of entries in cache
     */
    public int size() {
        if (concurrent) {
            return cache.size();
        } else {
            lock.readLock().lock();
            try {
                return cache.size();
            } finally {
                lock.readLock().unlock();
            }
        }
    }
    
    /**
     * Helper class to store a value with its timestamp
     */
    private static class CacheEntry<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        
        final T value;
        final long timestamp;
        
        CacheEntry(T value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
