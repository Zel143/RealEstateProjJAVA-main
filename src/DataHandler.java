import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;  // Added missing import
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles data persistence for lot information
 */
public class DataHandler {
    private static final String DATA_FILE = "real_estate_data.dat";
    private static final Logger LOGGER = Logger.getLogger(DataHandler.class.getName());
    
    private DataHandler() {
        // Utility class should not be instantiated
    }
    
    /**
     * Save lots to data file
     * @param lots Map of lot IDs to lot components
     * @return true if save was successful
     */
    public static boolean saveLots(Map<String, LotComponent> lots) {
        Path dataPath = Paths.get(DATA_FILE);
        
        // Create backup if file exists
        if (Files.exists(dataPath)) {
            try {
                createBackup(dataPath);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not create backup before saving", e);
            }
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(dataPath))) {
            oos.writeObject(lots);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving lots", e);
            return false;
        }
    }
    
    /**
     * Create a backup of the data file
     */
    private static void createBackup(Path dataPath) throws IOException {
        Path backupPath = Paths.get(DATA_FILE + ".bak");
        Files.copy(dataPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Load lots from data file
     * @return Map of lot IDs to lot components
     */
    @SuppressWarnings("unchecked")
    public static Map<String, LotComponent> loadLots() {
        Path dataPath = Paths.get(DATA_FILE);
        if (!Files.exists(dataPath)) {
            return new ConcurrentHashMap<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(dataPath))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                return (Map<String, LotComponent>) obj;
            }
            LOGGER.warning("Data file format is invalid");
        } catch (InvalidClassException ice) {
            LOGGER.log(Level.WARNING, "Data file version mismatch. Creating new data.", ice);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error loading lots", e);
        }
        
        return new ConcurrentHashMap<>();
    }
}
