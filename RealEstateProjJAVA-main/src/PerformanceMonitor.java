import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Utility class for monitoring and logging performance of critical operations
 */
public class PerformanceMonitor {
    private static final Logger LOGGER = Logger.getLogger(PerformanceMonitor.class.getName());
    private static final Map<String, OperationStats> stats = new ConcurrentHashMap<>();
    
    /**
     * Time an operation and record its statistics
     * @param operationName The name of the operation being timed
     * @param runnable The operation to time
     */
    public static void time(String operationName, Runnable runnable) {
        long start = System.nanoTime();
        try {
            runnable.run();
        } finally {
            long duration = System.nanoTime() - start;
            recordOperation(operationName, duration);
        }
    }
    
    /**
     * Time an operation that returns a result and record its statistics
     * @param <T> The type of the result
     * @param operationName The name of the operation being timed
     * @param supplier The operation to time
     * @return The result of the operation
     */
    public static <T> T timeSupplier(String operationName, Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long duration = System.nanoTime() - start;
            recordOperation(operationName, duration);
        }
    }
    
    private static void recordOperation(String operationName, long durationNanos) {
        stats.computeIfAbsent(operationName, k -> new OperationStats())
             .record(durationNanos);
        
        // Log slow operations (> 100ms)
        if (durationNanos > 100_000_000) {
            LOGGER.warning(String.format("Slow operation '%s': %.2f ms", 
                    operationName, durationNanos / 1_000_000.0));
        }
    }
    
    /**
     * Get a report of all timed operations
     * @return A formatted performance report
     */
    public static String getReport() {
        StringBuilder report = new StringBuilder("Performance Report\n");
        report.append("===============================\n");
        report.append(String.format("%-30s %-10s %-10s %-10s %-10s\n", 
                "Operation", "Count", "Avg (ms)", "Max (ms)", "Total (ms)"));
        report.append("---------------------------------------------------------------\n");
        
        stats.forEach((name, stat) -> {
            report.append(String.format("%-30s %-10d %-10.2f %-10.2f %-10.2f\n",
                    name,
                    stat.count.get(),
                    stat.getTotalTimeNanos() / (stat.count.get() * 1_000_000.0),
                    stat.maxTimeNanos.get() / 1_000_000.0,
                    stat.getTotalTimeNanos() / 1_000_000.0));
        });
        
        return report.toString();
    }
    
    /**
     * Reset all performance statistics
     */
    public static void reset() {
        stats.clear();
    }
    
    /**
     * Helper class to track statistics for a single operation
     */
    private static class OperationStats {
        final AtomicLong count = new AtomicLong(0);
        final AtomicLong totalTimeNanos = new AtomicLong(0);
        final AtomicLong maxTimeNanos = new AtomicLong(0);
        
        void record(long timeNanos) {
            count.incrementAndGet();
            totalTimeNanos.addAndGet(timeNanos);
            updateMax(timeNanos);
        }
        
        private void updateMax(long timeNanos) {
            long current;
            do {
                current = maxTimeNanos.get();
                if (timeNanos <= current) {
                    return;
                }
            } while (!maxTimeNanos.compareAndSet(current, timeNanos));
        }
        
        long getTotalTimeNanos() {
            return totalTimeNanos.get();
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
