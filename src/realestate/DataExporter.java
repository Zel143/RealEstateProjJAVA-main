package realestate;

import realestate.lot.Lot;
import realestate.lot.LotComponent;
import realestate.lot.LotFactory;
import realestate.lot.LotManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

/**
 * Utility for exporting/importing lot data in various formats
 */
public final class DataExporter {
    private static final Logger LOGGER = Logger.getLogger(DataExporter.class.getName());
    private static final int BATCH_SIZE = 500; // Number of rows to process at once
    
    // Common CSV delimiters and patterns
    private static final char DELIMITER = ',';
    private static final char QUOTE = '"';
    private static final Pattern QUOTE_PATTERN = Pattern.compile("\"");
    private static final Pattern FEATURE_POOL_PATTERN = Pattern.compile("\\b" + Pattern.quote("Pool") + "\\b");
    private static final Pattern FEATURE_LANDSCAPING_PATTERN = Pattern.compile("\\b" + Pattern.quote("Landscaping") + "\\b");
    private static final Pattern FEATURE_FENCING_PATTERN = Pattern.compile("\\b" + Pattern.quote("Fencing") + "\\b");
    
    // Private constructor to prevent instantiation
    private DataExporter() {}
    
    /**
     * Export lots to CSV file
     * @param lots List of lots to export
     * @param file Destination file
     * @return true if export was successful
     */
    public static boolean exportToCsv(List<LotComponent> lots, File file) {
        if (lots == null || file == null) {
            LOGGER.warning("Invalid parameters provided to exportToCsv");
            return false;
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            // Write header
            writer.write("ID,Block,LotNumber,Size,Price,Status,Features");
            writer.newLine();
            
            // Process in batches for large datasets
            for (int i = 0; i < lots.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, lots.size());
                List<LotComponent> batch = lots.subList(i, end);
                
                // Write data rows
                for (LotComponent component : batch) {
                    Lot baseLot = LotFactory.getBaseLot(component);
                    if (baseLot != null) {
                        String features = extractFeatures(component);
                        
                        // Create and write the CSV record
                        String record = String.format("%s,%d,%d,%.2f,%.2f,%s,%s",
                            escapeField(baseLot.getId()),
                            baseLot.getBlock(),
                            baseLot.getLotNumber(),
                            baseLot.getSize(),
                            component.getPrice(),
                            escapeField(component.getStatus()),
                            escapeField(features)
                        );
                        
                        writer.write(record);
                        writer.newLine();
                    }
                }
            }
            
            LOGGER.info(() -> "Exported " + lots.size() + " lots to CSV: " + file.getPath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export to CSV", e);
            return false;
        }
    }
    
    /**
     * Export JTable data to CSV asynchronously
     * @param model TableModel containing data
     * @param file Destination file
     * @param progressCallback Callback for export progress (0-100)
     * @param completionCallback Callback with success result
     */
    public static void exportTableToCsvAsync(TableModel model, File file, 
                                           Consumer<Integer> progressCallback,
                                           Consumer<Boolean> completionCallback) {
        SwingWorker<Boolean, Integer> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                    int rowCount = model.getRowCount();
                    int colCount = model.getColumnCount();
                    
                    // Write header
                    for (int col = 0; col < colCount; col++) {
                        if (col > 0) writer.write(DELIMITER);
                        writer.write(escapeField(model.getColumnName(col)));
                    }
                    writer.newLine();
                    
                    // Process rows in batches
                    int totalBatches = (int) Math.ceil(rowCount / (double) BATCH_SIZE);
                    for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                        int startRow = batchIndex * BATCH_SIZE;
                        int endRow = Math.min(startRow + BATCH_SIZE, rowCount);
                        
                        // Process rows in this batch
                        for (int row = startRow; row < endRow; row++) {
                            for (int col = 0; col < colCount; col++) {
                                if (col > 0) writer.write(DELIMITER);
                                
                                Object value = model.getValueAt(row, col);
                                if (value != null) {
                                    writer.write(escapeField(value.toString()));
                                }
                            }
                            writer.newLine();
                            
                            // Update progress every few rows
                            if (row % 50 == 0 || row == rowCount - 1) {
                                int progress = (int) ((row + 1) * 100.0 / rowCount);
                                publish(progress);
                            }
                        }
                    }
                    
                    LOGGER.info(() -> "Exported table with " + rowCount + " rows to CSV: " + file.getPath());
                    return true;
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to export table to CSV", e);
                    return false;
                }
            }
            
            @Override
            protected void process(List<Integer> chunks) {
                // Report progress (take the latest value)
                if (!chunks.isEmpty() && progressCallback != null) {
                    progressCallback.accept(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (completionCallback != null) {
                        completionCallback.accept(success);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error completing export", e);
                    if (completionCallback != null) {
                        completionCallback.accept(false);
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Export table data to CSV synchronously
     */
    public static boolean exportTableToCsv(TableModel model, File file) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            int rowCount = model.getRowCount();
            int colCount = model.getColumnCount();
            
            // Write header
            for (int col = 0; col < colCount; col++) {
                if (col > 0) writer.write(DELIMITER);
                writer.write(escapeField(model.getColumnName(col)));
            }
            writer.newLine();
            
            // Write data rows
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    if (col > 0) writer.write(DELIMITER);
                    
                    Object value = model.getValueAt(row, col);
                    if (value != null) {
                        writer.write(escapeField(value.toString()));
                    }
                }
                writer.newLine();
            }
            
            LOGGER.info(() -> "Exported table with " + rowCount + " rows to CSV: " + file.getPath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export table to CSV", e);
            return false;
        }
    }
    
    /**
     * Escape a field for CSV format
     */
    private static String escapeField(String value) {
        if (value == null) {
            return "";
        }
        
        // If the field contains quotes or commas, it needs quoting
        boolean needsQuotes = value.indexOf(QUOTE) >= 0 || 
                              value.indexOf(DELIMITER) >= 0 ||
                              value.indexOf('\n') >= 0 ||
                              value.indexOf('\r') >= 0;
        
        if (needsQuotes) {
            // Escape any quotes by doubling them
            String escaped = QUOTE_PATTERN.matcher(value).replaceAll("\"\"");
            return QUOTE + escaped + QUOTE;
        }
        
        return value;
    }
    
    /**
     * Create a backup of the current data file
     */
    public static boolean createBackup() {
        ConfigManager config = ConfigManager.getInstance();
        String dataFile = config.getProperty("data.file");
        
        Path srcPath = Paths.get(dataFile);
        if (!Files.exists(srcPath)) {
            LOGGER.warning(() -> "Cannot backup, source file does not exist: " + dataFile);
            return false;
        }
        
        try {
            // Create timestamp-based backup filename
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            String backupFile = "backup_" + timestamp + "_" + dataFile;
            Path backupPath = Paths.get(backupFile);
            
            // Copy with atomic file operations where possible
            Files.copy(srcPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info(() -> "Created backup: " + backupFile);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create backup", e);
            return false;
        }
    }
    
    /**
     * Import lots from CSV file asynchronously
     * @param file Source CSV file
     * @param lotManager realestate.lot.LotManager to add lots to
     * @param progressCallback Callback for import progress (0-100)
     * @param resultCallback Callback with import results
     */
    public static void importFromCsvAsync(File file, LotManager lotManager,
                                        Consumer<Integer> progressCallback,
                                        Consumer<Map<String, Object>> resultCallback) {
        CompletableFuture.supplyAsync(() -> importFromCsv(file, lotManager, progressCallback))
                         .thenAccept(resultCallback);
    }
    
    /**
     * Import lots from CSV file
     * @return Map with count of imported lots and any error messages
     */
    public static Map<String, Object> importFromCsv(File file, LotManager lotManager) {
        return importFromCsv(file, lotManager, null);
    }
    
    /**
     * Import lots from CSV file with progress reporting
     */
    private static Map<String, Object> importFromCsv(File file, LotManager lotManager, 
                                                  Consumer<Integer> progressCallback) {
        List<String> errors = new ArrayList<>();
        AtomicInteger importedCount = new AtomicInteger(0);
        
        try {
            // Count total lines for progress calculation
            long totalLines = Files.lines(file.toPath()).count() - 1; // Exclude header
            
            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                // Skip header
                reader.readLine();
                
                String line;
                int lineNumber = 1;
                int processedLines = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    processedLines++;
                    
                    try {
                        String[] parts = parseCsvLine(line);
                        if (parts.length < 4) {
                            errors.add("Line " + lineNumber + ": Not enough fields");
                            continue;
                        }
                        
                        // Minimum required fields: block,lotNumber,size,price
                        String lotDetails = parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4];
                        String result = lotManager.addLot(lotDetails);
                        
                        if (result.startsWith("realestate.lot.Lot added successfully")) {
                            importedCount.incrementAndGet();
                            
                            // Extract lot ID from result or construct it
                            String lotId = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : 
                                          "realestate.lot.Lot" + parts[1] + " " + parts[2];
                            
                            // Apply status if provided
                            if (parts.length > 5 && !parts[5].isEmpty()) {
                                applyStatus(lotManager, lotId, parts[5]);
                            }
                            
                            // Apply features if provided
                            if (parts.length > 6 && !parts[6].isEmpty()) {
                                applyFeatures(lotManager, lotId, parts[6]);
                            }
                        } else {
                            errors.add("Line " + lineNumber + ": " + result);
                        }
                    } catch (Exception e) {
                        errors.add("Line " + lineNumber + ": " + e.getMessage());
                    }
                    
                    // Report progress every 10 lines or at the end
                    if (progressCallback != null && (processedLines % 10 == 0 || processedLines == totalLines)) {
                        int progressPercent = (int) (processedLines * 100.0 / totalLines);
                        progressCallback.accept(progressPercent);
                    }
                }
            }
            
            LOGGER.info(() -> "Imported " + importedCount.get() + " lots from CSV with " + errors.size() + " errors");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to import from CSV", e);
            errors.add("File error: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("importedCount", importedCount.get());
        result.put("errors", errors);
        return result;
    }
    
    /**
     * Apply status to a lot
     */
    private static void applyStatus(LotManager lotManager, String lotId, String status) {
        switch(status.toUpperCase()) {
            case "RESERVED" -> lotManager.reserveLot(lotId);
            case "SOLD" -> lotManager.sellLot(lotId);
            default -> {} // Ignore unknown statuses
        }
    }
    
    /**
     * Apply features to a lot
     */
    private static void applyFeatures(LotManager lotManager, String lotId, String features) {
        if (FEATURE_POOL_PATTERN.matcher(features).find()) {
            lotManager.addFeatureToLot(lotId, "pool");
        }
        if (FEATURE_LANDSCAPING_PATTERN.matcher(features).find()) {
            lotManager.addFeatureToLot(lotId, "landscaping");
        }
        if (FEATURE_FENCING_PATTERN.matcher(features).find()) {
            lotManager.addFeatureToLot(lotId, "fencing");
        }
    }
    
    /**
     * Parse a CSV line, handling quoted fields correctly
     */
    private static String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }
        
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == QUOTE) {
                // Handle escaped quotes (two double quotes in a row)
                if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                    field.append(QUOTE);
                    i++; // Skip the next quote
                } else {
                    // Toggle quote mode
                    inQuotes = !inQuotes;
                }
            } else if (c == DELIMITER && !inQuotes) {
                // End of field
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        
        // Add the last field
        result.add(field.toString());
        
        // Use more efficient toArray pattern
        return result.toArray(String[]::new);
    }
    
    /**
     * Extract features from lot component
     */
    private static String extractFeatures(LotComponent lot) {
        String description = lot.getDescription();
        List<String> features = new ArrayList<>();
        
        if (description.contains("+ " + LotFactory.FEATURE_POOL)) {
            features.add("Pool");
        }
        if (description.contains("+ " + LotFactory.FEATURE_LANDSCAPING)) {
            features.add("Landscaping");
        }
        if (description.contains("+ " + LotFactory.FEATURE_FENCING)) {
            features.add("Fencing");
        }
        
        return features.isEmpty() ? "" : String.join(", ", features);
    }
}
