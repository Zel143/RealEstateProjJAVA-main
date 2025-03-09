import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized configuration manager for system settings
 */
public final class ConfigManager {
    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_FILE = "real_estate_config.properties";
    private static final Properties properties = new Properties();
    
    // Thread-safe cache for parsed values
    private static final Map<String, Object> valueCache = new ConcurrentHashMap<>();
    
    // Default configuration values with types
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
    
    // Singleton instance with thread-safe lazy initialization
    private static class InstanceHolder {
        static final ConfigManager INSTANCE = new ConfigManager();
    }
    
    // Private constructor prevents instantiation
    private ConfigManager() {
        loadConfig();
    }
    
    /**
     * Get the singleton instance
     */
    public static ConfigManager getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfig() {
        if (Files.exists(Paths.get(CONFIG_FILE))) {
            try (InputStream input = Files.newInputStream(Paths.get(CONFIG_FILE))) {
                properties.load(input);
                LOGGER.info("Loaded configuration from " + CONFIG_FILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load configuration, using defaults", e);
            }
        }
        
        // Ensure all default values are present
        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            if (!properties.containsKey(entry.getKey())) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
        }
        
        // Create missing config file if needed
        if (!Files.exists(Paths.get(CONFIG_FILE))) {
            saveConfig();
        }
    }
    
    /**
     * Save configuration to file
     */
    public boolean saveConfig() {
        try (OutputStream output = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            properties.store(output, "Real Estate Management System Configuration");
            LOGGER.info("Configuration saved to " + CONFIG_FILE);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save configuration", e);
            return false;
        }
    }
    
    /**
     * Get a string property
     */
    public String getProperty(String key) {
        return properties.getProperty(key, DEFAULTS.getOrDefault(key, null));
    }

    /**
     * Get an integer property with caching
     */
    public int getIntProperty(String key) {
        // Check cache first
        return (Integer) valueCache.computeIfAbsent(key + ".int", k -> {
            try {
                return Integer.parseInt(getProperty(key));
            } catch (NumberFormatException e) {
                return Integer.parseInt(DEFAULTS.getOrDefault(key, "0"));
            }
        });
    }

    /**
     * Get a double property with caching
     */
    public double getDoubleProperty(String key) {
        // Check cache first
        return (Double) valueCache.computeIfAbsent(key + ".double", k -> {
            try {
                return Double.parseDouble(getProperty(key));
            } catch (NumberFormatException e) {
                return Double.parseDouble(DEFAULTS.getOrDefault(key, "0.0"));
            }
        });
    }
    
    /**
     * Get a boolean property with caching
     */
    public boolean getBooleanProperty(String key) {
        // Check cache first - removed unnecessary temporary
        return (Boolean) valueCache.computeIfAbsent(key + ".boolean", k ->
            Boolean.parseBoolean(getProperty(key)));
    }
    
    /**
     * Set a property and invalidate cache
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        
        // Invalidate all cached values for this key
        valueCache.remove(key + ".int");
        valueCache.remove(key + ".double");
        valueCache.remove(key + ".boolean");
    }
    
    /**
     * Get all property names
     */
    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
    
    /**
     * Get all properties as a map
     */
    public Map<String, String> getAllProperties() {
        Map<String, String> result = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
        }
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Reset a property to its default value
     */
    public void resetProperty(String key) {
        String defaultValue = DEFAULTS.get(key);
        if (defaultValue != null) {
            setProperty(key, defaultValue);
        } else {
            properties.remove(key);
        }
        
        // Clear cached values
        valueCache.remove(key + ".int");
        valueCache.remove(key + ".double");
        valueCache.remove(key + ".boolean");
    }
    
    /**
     * Reset all properties to defaults
     */
    public void resetAll() {
        properties.clear();
        valueCache.clear();
        
        for (Map.Entry<String, String> entry : DEFAULTS.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        
        saveConfig();
    }
}
