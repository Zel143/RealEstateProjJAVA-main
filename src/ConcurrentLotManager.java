import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe facade for LotManager that optimizes for concurrent access
 */
public class ConcurrentLotManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConcurrentLotManager.class.getName());
    
    private final LotManager lotManager;
    private final transient ReadWriteLock lock;
    private final transient PerformanceCache<CacheKey, List<LotComponent>> searchCache;
    
    /**
     * Create a thread-safe manager
     * @param lotManager the delegate lot manager
     */
    public ConcurrentLotManager(LotManager lotManager) {
        this.lotManager = Objects.requireNonNull(lotManager, "LotManager cannot be null");
        this.lock = new ReentrantReadWriteLock();
        this.searchCache = new PerformanceCache<>(50, 30_000); // Cache 50 searches for 30 seconds
    }
    
    // Get lock (for subclasses)
    protected ReadWriteLock getLock() {
        return lock;
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
     * Get lots by named predicate
     */
    public List<LotComponent> getLotsByPredicate(String predicateName) {
        return withReadLock(() -> lotManager.getLotsByPredicate(predicateName));
    }
    
    /**
     * Adds a new lot (invalidates cache)
     */
    public String addLot(String lotDetails) {
        String result = withWriteLock(() -> lotManager.addLot(lotDetails));
        invalidateCache();
        return result;
    }
    
    /**
     * Reserves a lot (invalidates cache)
     */
    public String reserveLot(String lotId) {
        String result = withWriteLock(() -> lotManager.reserveLot(lotId));
        invalidateCache();
        return result;
    }
    
    /**
     * Sells a lot (invalidates cache)
     */
    public String sellLot(String lotId) {
        String result = withWriteLock(() -> lotManager.sellLot(lotId));
        invalidateCache();
        return result;
    }
    
    /**
     * Adds a feature to a lot (invalidates cache)
     */
    public LotComponent addFeatureToLot(String lotId, String feature) {
        LotComponent result = withWriteLock(() -> lotManager.addFeatureToLot(lotId, feature));
        invalidateCache();
        return result;
    }
    
    /**
     * Save data (using write lock)
     */
    public boolean saveData() {
        return withWriteLock(lotManager::saveData);
    }
    
    /**
     * Invalidate search cache
     */
    protected void invalidateCache() {
        searchCache.clear();
    }
    
    /**
     * Execute a function with a read lock
     */
    protected <T> T withReadLock(Supplier<T> supplier) {
        lock.readLock().lock();
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in read operation", e);
            throw e;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Execute a function with a write lock
     */
    protected <T> T withWriteLock(Supplier<T> supplier) {
        lock.writeLock().lock();
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in write operation", e);
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Cache key for search operations
     */
    private static class CacheKey implements Serializable {
        private static final long serialVersionUID = 1L;
        
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
            if (!(obj instanceof CacheKey other)) return false;
            return Objects.equals(minSize, other.minSize) &&
                   Objects.equals(maxSize, other.maxSize) &&
                   Objects.equals(minPrice, other.minPrice) &&
                   Objects.equals(maxPrice, other.maxPrice) &&
                   Objects.equals(blockNumber, other.blockNumber) &&
                   Objects.equals(status, other.status);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minSize, maxSize, minPrice, maxPrice, blockNumber, status);
        }
        
        @Override
        public String toString() {
            return "SearchCriteria[" +
                   "minSize=" + minSize +
                   ", maxSize=" + maxSize +
                   ", minPrice=" + minPrice +
                   ", maxPrice=" + maxPrice +
                   ", block=" + blockNumber +
                   ", status='" + status + "']";
        }
    }
}
