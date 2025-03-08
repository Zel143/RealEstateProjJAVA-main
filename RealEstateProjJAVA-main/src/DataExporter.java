import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.table.TableModel;

/**
 * Utility for exporting/importing lot data in various formats
 */
public class DataExporter {
    private static final Logger LOGGER = Logger.getLogger(DataExporter.class.getName());

    /**
     * Export lots to CSV file
     */
    public static boolean exportToCsv(List<LotComponent> lots, File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            writer.println("ID,Block,LotNumber,Size,Price,Status,Features");
            
            // Write data rows
            for (LotComponent component : lots) {
                Lot baseLot = findBaseLot(component);
                if (baseLot != null) {
                    String features = extractFeatures(component);
                    
                    writer.printf("%s,%d,%d,%.2f,%.2f,%s,%s\n",
                        baseLot.getId(),
                        baseLot.getBlock(),
                        baseLot.getLotNumber(),
                        baseLot.getSize(),
                        component.getPrice(),
                        component.getStatus(),
                        features
                    );
                }
            }
            LOGGER.info("Exported " + lots.size() + " lots to CSV: " + file.getPath());
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to export to CSV: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Export JTable data to CSV
     */
    public static boolean exportTableToCsv(TableModel model, File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write header
            for (int col = 0; col < model.getColumnCount(); col++) {
                writer.print(model.getColumnName(col));
                if (col < model.getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println();
            
            // Write data rows
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    if (value != null) {
                        String valueStr = value.toString();
                        // Escape commas and quotes
                        if (valueStr.contains(",") || valueStr.contains("\"")) {
                            valueStr = "\"" + valueStr.replace("\"", "\"\"") + "\"";
                        }
                        writer.print(valueStr);
                    }
                    
                    if (col < model.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
            
            LOGGER.info("Exported table with " + model.getRowCount() + " rows to CSV: " + file.getPath());
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to export table to CSV: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create a backup of the current data file
     */
    public static boolean createBackup() {
        ConfigManager config = ConfigManager.getInstance();
        String dataFile = config.getProperty("data.file");
        
        File srcFile = new File(dataFile);
        if (!srcFile.exists()) {
            LOGGER.warning("Cannot backup, source file does not exist: " + dataFile);
            return false;
        }
        
        // Create timestamp-based backup filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupFile = "backup_" + timestamp + "_" + dataFile;
        
        try {
            Files.copy(srcFile.toPath(), new File(backupFile).toPath());
            LOGGER.info("Created backup: " + backupFile);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to create backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Import lots from CSV file
     * @return Map with count of imported lots and any error messages
     */
    public static Map<String, Object> importFromCsv(File file, LotManager lotManager) {
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip header
            reader.readLine();
            
            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 4) {
                        errors.add("Line " + lineNumber + ": Not enough fields");
                        continue;
                    }
                    
                    // Minimum required fields: block,lotNumber,size,price
                    String lotDetails = parts[1] + "," + parts[2] + "," + parts[3] + "," + parts[4];
                    String result = lotManager.addLot(lotDetails);
                    
                    if (result.startsWith("Lot added successfully")) {
                        importedCount++;
                        
                        // Extract lot ID from result
                        String lotId = null;
                        if (parts.length > 0) {
                            lotId = parts[0];
                        } else {
                            // Try to extract from result
                            String[] resultParts = result.split("\n");
                            if (resultParts.length > 1) {
                                String desc = resultParts[1];
                                lotId = "Lot" + parts[1] + " " + parts[2]; // Construct ID from block and lot number
                            }
                        }
                        
                        // Apply status if provided
                        if (lotId != null && parts.length > 5) {
                            String status = parts[5];
                            if ("RESERVED".equals(status)) {
                                lotManager.reserveLot(lotId);
                            } else if ("SOLD".equals(status)) {
                                lotManager.sellLot(lotId);
                            }
                        }
                        
                        // Apply features if provided
                        if (lotId != null && parts.length > 6) {
                            String features = parts[6];
                            if (features.contains("Pool")) {
                                lotManager.addFeatureToLot(lotId, "pool");
                            }
                            if (features.contains("Fencing")) {
                                lotManager.addFeatureToLot(lotId, "fencing");
                            }
                            if (features.contains("Landscaping")) {
                                lotManager.addFeatureToLot(lotId, "landscaping");
                            }
                        }
                    } else {
                        errors.add("Line " + lineNumber + ": " + result);
                    }
                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            LOGGER.info("Imported " + importedCount + " lots from CSV with " + errors.size() + " errors");
        } catch (IOException e) {
            LOGGER.severe("Failed to import from CSV: " + e.getMessage());
            errors.add("File error: " + e.getMessage());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("importedCount", importedCount);
        result.put("errors", errors);
        return result;
    }
    
    /**
     * Parse a CSV line, handling quoted fields correctly
     */
    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                // Handle escaped quotes (two double quotes in a row)
                if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    field.append('\"');
                    i++; // Skip the next quote
                } else {
                    // Toggle quote mode
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        
        // Add the last field
        result.add(field.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Extract features from lot component
     */
    private static String extractFeatures(LotComponent lot) {
        String description = lot.getDescription();
        StringBuilder features = new StringBuilder();
        
        if (description.contains("+ Swimming Pool")) {
            features.append("Pool ");
        }
        if (description.contains("+ Premium Landscaping")) {
            features.append("Landscaping ");
        }
        if (description.contains("+ Perimeter Fencing")) {
            features.append("Fencing ");
        }
        
        return features.toString().trim();
    }
    
    /**
     * Find the base lot in a decorated component
     */
    private static Lot findBaseLot(LotComponent component) {
        if (component instanceof Lot) {
            return (Lot) component;
        } else if (component instanceof LotDecorator) {
            return findBaseLot(((LotDecorator) component).getDecoratedLot());
        }
        return null;
    }
}
