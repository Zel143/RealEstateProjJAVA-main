package realestate;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Utility class for monitoring and logging performance of critical operations
 */
public final class PerformanceMonitor {
    private static final Logger LOGGER = Logger.getLogger(PerformanceMonitor.class.getName());
    
    // Store statistics in thread-safe map
    private static final Map<String, OperationStats> stats = new ConcurrentHashMap<>();
    
    // Threshold for logging slow operations
    private static final long SLOW_THRESHOLD_NS = 100_000_000; // 100ms
    
    // Flag to enable/disable performance monitoring
    private static volatile boolean enabled = true;
    
    // Private constructor to prevent instantiation
    private PerformanceMonitor() {}
    
    /**
     * Enable or disable monitoring
     */
    public static void setEnabled(boolean isEnabled) {
        enabled = isEnabled;
    }
    
    /**
     * Time an operation and record its statistics
     * 
     * @param operationName The name of the operation being timed
     * @param runnable The operation to time
     */
    public static void time(String operationName, Runnable runnable) {
        if (!enabled) {
            runnable.run();
            return;
        }
        
        long start = System.nanoTime();
        Exception exception = null;
        
        try {
            runnable.run();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.nanoTime() - start;
            recordOperation(operationName, duration, exception != null);
        }
    }
    
    /**
     * Time a function that returns a value
     * 
     * @param <T> The return type
     * @param operationName The name of the operation
     * @param supplier The function to execute
     * @return The result of the function
     */
    public static <T> T timeSupplier(String operationName, Supplier<T> supplier) {
        if (!enabled) {
            return supplier.get();
        }
        
        long start = System.nanoTime();
        Exception exception = null;
        T result;
        
        try {
            result = supplier.get();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.nanoTime() - start;
            recordOperation(operationName, duration, exception != null);
        }
        
        return result;
    }
    
    /**
     * Record an operation's timing
     */
    private static void recordOperation(String operationName, long durationNanos, boolean failed) {
        stats.computeIfAbsent(operationName, k -> new OperationStats())
             .record(durationNanos, failed);
        
        // Log slow operations
        if (durationNanos > SLOW_THRESHOLD_NS) {
            LOGGER.warning(String.format("Slow operation '%s': %.2f ms%s", 
                    operationName, durationNanos / 1_000_000.0,
                    failed ? " (FAILED)" : ""));
        }
    }
    
    /**
     * Get a report of all timed operations
     * @return A formatted performance report
     */
    public static String getReport() {
        if (stats.isEmpty()) {
            return "No performance data collected.";
        }
        
        StringBuilder report = new StringBuilder("Performance Report\n");
        report.append("===============================\n");
        report.append(String.format("%-30s %-10s %-10s %-10s %-10s %-10s\n", 
                "Operation", "Count", "Avg (ms)", "Max (ms)", "Total (ms)", "Errors"));
        report.append("---------------------------------------------------------------\n");
        
        // Sort by total time (descending)
        Map<String, OperationStats> sortedStats = new TreeMap<>((a, b) -> {
            OperationStats statsA = stats.get(a);
            OperationStats statsB = stats.get(b);
            return Long.compare(statsB.getTotalTimeNanos(), statsA.getTotalTimeNanos());
        });
        
        sortedStats.putAll(stats);
        
        for (Map.Entry<String, OperationStats> entry : sortedStats.entrySet()) {
            OperationStats stat = entry.getValue();
            long count = stat.getCount();
            
            if (count == 0) continue;
            
            report.append(String.format("%-30s %-10d %-10.2f %-10.2f %-10.2f %-10d\n",
                    entry.getKey(),
                    count,
                    stat.getAverageTimeMs(),
                    stat.getMaxTimeMs(),
                    stat.getTotalTimeMs(),
                    stat.getErrorCount()));
        }
        
        return report.toString();
    }
    
    /**
     * Reset all performance statistics
     */
    public static void reset() {
        stats.clear();
    }
    
    /**
     * Apply a function to all statistics
     */
    public static void forEachStat(Consumer<Map.Entry<String, OperationStats>> consumer) {
        stats.entrySet().forEach(consumer);
    }
    
    /**
     * Helper class to track statistics for a single operation
     */
    public static class OperationStats implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTimeNanos = new LongAdder();
        private final LongAdder errorCount = new LongAdder();
        private final AtomicLong maxTimeNanos = new AtomicLong(0);
        
        /**
         * Record a new timing sample
         */
        void record(long timeNanos, boolean failed) {
            count.increment();
            totalTimeNanos.add(timeNanos);
            
            if (failed) {
                errorCount.increment();
            }
            
            // Update max time if needed
            long current = maxTimeNanos.get();
            while (timeNanos > current) {
                if (maxTimeNanos.compareAndSet(current, timeNanos)) {
                    break;
                }
                current = maxTimeNanos.get();
            }
        }
        
        /**
         * Get total count of operations
         */
        public long getCount() {
            return count.sum();
        }
        
        /**
         * Get error count
         */
        public long getErrorCount() {
            return errorCount.sum();
        }
        
        /**
         * Get total time in nanoseconds
         */
        public long getTotalTimeNanos() {
            return totalTimeNanos.sum();
        }
        
        /**
         * Get total time in milliseconds
         */
        public double getTotalTimeMs() {
            return totalTimeNanos.sum() / 1_000_000.0;
        }
        
        /**
         * Get average time in milliseconds
         */
        public double getAverageTimeMs() {
            long countVal = count.sum();
            return countVal > 0 ? totalTimeNanos.sum() / (countVal * 1_000_000.0) : 0;
        }
        
        /**
         * Get max time in milliseconds
         */
        public double getMaxTimeMs() {
            return maxTimeNanos.get() / 1_000_000.0;
        }
    }
    
    /**
     * Functional interface for operations that return a value
     */
    @FunctionalInterface
    public interface Supplier<T> {
        T get();
    }
}
