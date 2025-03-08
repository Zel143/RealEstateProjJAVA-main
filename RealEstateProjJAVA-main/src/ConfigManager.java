import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Centralized configuration manager for system settings
 */
public class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_FILE = "real_estate_config.properties";
    private static Properties properties = new Properties();
    private static ConfigManager instance;

    // Default configuration values
    private static final Map<String, String> DEFAULTS = Map.of(
        "data.file", "real_estate_data.dat",
        "backup.file", "real_estate_data.bak",
        "auto.save.interval", "5",
        "log.file", "real_estate_app.log",
        "cache.size", "50",
        "cache.expiration", "30000",
        "ui.fontsize", "12",
        "ui.table.rows", "20"
    );

    private ConfigManager() {
        loadConfig();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        
        // If config file exists, load it
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(CONFIG_FILE)) {
                properties.load(input);
                LOGGER.info("Loaded configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                LOGGER.warning("Failed to load configuration: " + e.getMessage());
                // Continue with defaults
            }
        }
        
        // Ensure all default values are present
        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            if (!properties.containsKey(entry.getKey())) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
        }
        
        // Save if we added any defaults
        if (configFile.length() == 0 || !configFile.exists()) {
            saveConfig();
        }
    }

    public boolean saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Real Estate Management System Configuration");
            LOGGER.info("Configuration saved to " + CONFIG_FILE);
            return true;
        } catch (IOException e) {
            LOGGER.warning("Failed to save configuration: " + e.getMessage());
            return false;
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key, DEFAULTS.getOrDefault(key, null));
    }

    public int getIntProperty(String key) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULTS.getOrDefault(key, "0"));
        }
    }

    public double getDoubleProperty(String key) {
        try {
            return Double.parseDouble(getProperty(key));
        } catch (NumberFormatException e) {
            return Double.parseDouble(DEFAULTS.getOrDefault(key, "0.0"));
        }
    }

    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }

    public Map<String, String> getAllProperties() {
        Map<String, String> result = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
        }
        return result;
    }
}
