import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe facade for LotManager that optimizes for concurrent access
 */
public class ConcurrentLotManager {
    private final LotManager lotManager;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PerformanceCache<CacheKey, List<LotComponent>> searchCache;
    
    public ConcurrentLotManager(LotManager lotManager) {
        this.lotManager = lotManager;
        this.searchCache = new PerformanceCache<>(50, 30_000); // Cache 50 searches for 30 seconds
    }
    
    /**
     * Get all available lots using read lock
     * @return List of all lot components
     */
    public List<LotComponent> getAllLots() {
        return withReadLock(lotManager::getAllLots);
    }
    
    /**
     * Search lots with caching
     */
    public List<LotComponent> searchLots(Double minSize, Double maxSize, 
                                       Double minPrice, Double maxPrice, 
                                       Integer blockNumber, String status) {
        CacheKey key = new CacheKey(minSize, maxSize, minPrice, maxPrice, blockNumber, status);
        
        return searchCache.get(key, () -> 
            withReadLock(() -> lotManager.searchLots(minSize, maxSize, minPrice, maxPrice, blockNumber, status))
        );
    }
    
    /**
     * Adds a new lot (invalidates cache)
     */
    public String addLot(String lotDetails) {
        String result = withWriteLock(() -> lotManager.addLot(lotDetails));
        searchCache.clear();
        return result;
    }
    
    /**
     * Reserves a lot (invalidates cache)
     */
    public String reserveLot(String lotId) {
        String result = withWriteLock(() -> lotManager.reserveLot(lotId));
        searchCache.clear();
        return result;
    }
    
    /**
     * Sells a lot (invalidates cache)
     */
    public String sellLot(String lotId) {
        String result = withWriteLock(() -> lotManager.sellLot(lotId));
        searchCache.clear();
        return result;
    }
    
    /**
     * Adds a feature to a lot (invalidates cache)
     */
    public LotComponent addFeatureToLot(String lotId, String feature) {
        LotComponent result = withWriteLock(() -> lotManager.addFeatureToLot(lotId, feature));
        searchCache.clear();
        return result;
    }
    
    /**
     * Save data (using write lock)
     */
    public boolean saveData() {
        return withWriteLock(lotManager::saveData);
    }
    
    /**
     * Execute a function with a read lock
     */
    private <T> T withReadLock(Supplier<T> supplier) {
        lock.readLock().lock();
        try {
            return supplier.get();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Execute a function with a write lock
     */
    private <T> T withWriteLock(Supplier<T> supplier) {
        lock.writeLock().lock();
        try {
            return supplier.get();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Functional interface for suppliers
     */
    private interface Supplier<T> {
        T get();
    }
    
    /**
     * Cache key for search operations
     */
    private static class CacheKey {
        final Double minSize;
        final Double maxSize;
        final Double minPrice;
        final Double maxPrice;
        final Integer blockNumber;
        final String status;
        
        CacheKey(Double minSize, Double maxSize, Double minPrice, Double maxPrice, 
                Integer blockNumber, String status) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.blockNumber = blockNumber;
            this.status = status;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CacheKey)) return false;
            CacheKey other = (CacheKey) obj;
            return equals(minSize, other.minSize) &&
                   equals(maxSize, other.maxSize) &&
                   equals(minPrice, other.minPrice) &&
                   equals(maxPrice, other.maxPrice) &&
                   equals(blockNumber, other.blockNumber) &&
                   equals(status, other.status);
        }
        
        private boolean equals(Object a, Object b) {
            return (a == null && b == null) || (a != null && a.equals(b));
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (minSize == null ? 0 : minSize.hashCode());
            hash = 31 * hash + (maxSize == null ? 0 : maxSize.hashCode());
            hash = 31 * hash + (minPrice == null ? 0 : minPrice.hashCode());
            hash = 31 * hash + (maxPrice == null ? 0 : maxPrice.hashCode());
            hash = 31 * hash + (blockNumber == null ? 0 : blockNumber.hashCode());
            hash = 31 * hash + (status == null ? 0 : status.hashCode());
            return hash;
        }
    }
}
