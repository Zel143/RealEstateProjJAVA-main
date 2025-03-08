package com.realestate;

import com.formdev.flatlaf.FlatLightLaf;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Main application entry point
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    /**
     * Application entry point
     */
    public static void main(String[] args) {
        try {
            // Configure logging
            setupLogging();
            
            // Set system look and feel
            setLookAndFeel();
            
            // Launch application
            launchApplication();
            
        } catch (Exception e) {
            // Handle any uncaught startup errors
            LOGGER.log(Level.SEVERE, "Fatal error during application startup", e);
            JOptionPane.showMessageDialog(null, 
                "A fatal error occurred during application startup:\n" + e.getMessage(),
                "Startup Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Setup logging configuration
     */
    private static void setupLogging() throws IOException {
        // Get configuration for log file
        ConfigManager config = ConfigManager.getInstance();
        String logFile = config.getProperty("log.file");
        
        // Create file handler with simple formatter
        FileHandler fileHandler = new FileHandler(logFile, true);
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(Level.INFO);
        
        // Add console handler for development
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        
        // Configure the root logger
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        rootLogger.addHandler(fileHandler);
        rootLogger.addHandler(consoleHandler);
        
        LOGGER.info("Logging initialized");
    }
    
    /**
     * Set the application look and feel
     */
    private static void setLookAndFeel() {
        try {
            // Use FlatLaf instead of system look and feel
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
            LOGGER.info("Using FlatLaf light theme");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not set FlatLaf, using system look and feel", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to set system look and feel", ex);
            }
        }
    }
    
    /**
     * Launch the application UI
     */
    private static void launchApplication() {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new RealEstateFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1024, 768);
                frame.setLocationRelativeTo(null); // Center on screen
                frame.setVisible(true);
                
                LOGGER.info("Application started successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error launching application UI", e);
                JOptionPane.showMessageDialog(null, 
                    "Error launching application:\n" + e.getMessage(),
                    "Launch Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
