import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class RealEstateFrame extends JFrame {
    private JTextArea displayArea;
    private LotManager lotManager;

    public RealEstateFrame() {
        setTitle("Real Estate Management System");
        lotManager = new LotManager();
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        setLayout(new BorderLayout());
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Create tabbed panel for different control groups
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Main control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(6, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new GridLayout(6, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Welcome message
        displayArea.setText("""
                           Welcome to Real Estate Management System
                           
                           Use the controls below to manage properties:
                           - Add Lot: Create a new property listing
                           - Search Lot: Find a property by ID
                           - Sell Lot: Mark a property as sold
                           - Reserve Lot: Place a hold on a property
                           - Generate Report: View all property listings
                           - Search by Block/Size/Price: Find lots by specific criteria
                           """);

        addControl(controlPanel, "Add Lot:", e -> {
            JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            JTextField blockField = new JTextField();
            JTextField lotNumberField = new JTextField();
            JTextField sizeField = new JTextField();
            JTextField priceField = new JTextField();
            
            inputPanel.add(new JLabel("Block Number (1-5):"));
            inputPanel.add(blockField);
            inputPanel.add(new JLabel("Lot Number (1-20):"));
            inputPanel.add(lotNumberField);
            inputPanel.add(new JLabel("Size (sqm):"));
            inputPanel.add(sizeField);
            inputPanel.add(new JLabel("Price ($):"));
            inputPanel.add(priceField);
            
            int result = JOptionPane.showConfirmDialog(this, inputPanel, 
                    "Enter Lot Details", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String lotDetails = blockField.getText() + "," + 
                                        lotNumberField.getText() + "," + 
                                        sizeField.getText() + "," + 
                                        priceField.getText();
                    displayArea.setText(lotManager.addLot(lotDetails));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Please enter valid numbers for all fields", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        addControl(controlPanel, "Search Lot:", e -> {
            String lotId = getInput("Enter lot ID (e.g., Lot1):", "Search Lot");
            if (lotId != null && !lotId.isEmpty()) {
                displayArea.setText(lotManager.searchLot(lotId));
            }
        });

        addControl(controlPanel, "Sell Lot:", e -> {
            String lotId = getInput("Enter lot ID to mark as sold (e.g., Lot1):", "Sell Lot");
            if (lotId != null && !lotId.isEmpty()) {
                displayArea.setText(lotManager.sellLot(lotId));
            }
        });

        addControl(controlPanel, "Reserve Lot:", e -> {
            String lotId = getInput("Enter lot ID to reserve (e.g., Lot1):", "Reserve Lot");
            if (lotId != null && !lotId.isEmpty()) {
                displayArea.setText(lotManager.reserveLot(lotId));
            }
        });

        JLabel reportLabel = new JLabel("View all properties:");
        JButton reportButton = new JButton("Generate Report");
        reportButton.addActionListener(e -> displayArea.setText(lotManager.generateReport()));
        controlPanel.add(reportLabel);
        controlPanel.add(reportButton);
        
        JLabel clearLabel = new JLabel("Clear display:");
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> displayArea.setText(""));
        controlPanel.add(clearLabel);
        controlPanel.add(clearButton);
        
        // Search by block
        addControl(searchPanel, "Search by Block:", e -> {
            String blockStr = getInput("Enter block number (1-5):", "Search by Block");
            if (blockStr != null && !blockStr.isEmpty()) {
                try {
                    int block = Integer.parseInt(blockStr);
                    displayArea.setText(lotManager.searchLotsByBlock(block));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Please enter a valid block number", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Search by size
        addControl(searchPanel, "Search by Size:", e -> {
            JPanel sizePanel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField minSizeField = new JTextField("0");
            JTextField maxSizeField = new JTextField("1000");
            
            sizePanel.add(new JLabel("Minimum Size (sqm):"));
            sizePanel.add(minSizeField);
            sizePanel.add(new JLabel("Maximum Size (sqm):"));
            sizePanel.add(maxSizeField);
            
            int result = JOptionPane.showConfirmDialog(this, sizePanel, 
                    "Enter Size Range", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double minSize = Double.parseDouble(minSizeField.getText());
                    double maxSize = Double.parseDouble(maxSizeField.getText());
                    displayArea.setText(lotManager.searchLotsBySize(minSize, maxSize));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Please enter valid numbers", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Search by price
        addControl(searchPanel, "Search by Price:", e -> {
            JPanel pricePanel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField minPriceField = new JTextField("0");
            JTextField maxPriceField = new JTextField("1000000");
            
            pricePanel.add(new JLabel("Minimum Price ($):"));
            pricePanel.add(minPriceField);
            pricePanel.add(new JLabel("Maximum Price ($):"));
            pricePanel.add(maxPriceField);
            
            int result = JOptionPane.showConfirmDialog(this, pricePanel, 
                    "Enter Price Range", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    double minPrice = Double.parseDouble(minPriceField.getText());
                    double maxPrice = Double.parseDouble(maxPriceField.getText());
                    displayArea.setText(lotManager.searchLotsByPrice(minPrice, maxPrice));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, 
                            "Please enter valid numbers", 
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Add more search options to fill the search panel
        JLabel placeholder1 = new JLabel("");
        JLabel placeholder2 = new JLabel("");
        JLabel placeholder3 = new JLabel("");
        JLabel placeholder4 = new JLabel("");
        JLabel placeholder5 = new JLabel("");
        JLabel placeholder6 = new JLabel("");
        searchPanel.add(placeholder1);
        searchPanel.add(placeholder2);
        searchPanel.add(placeholder3);
        searchPanel.add(placeholder4);
        searchPanel.add(placeholder5);
        searchPanel.add(placeholder6);
        
        // Create a features panel for decorating lots
        JPanel featuresPanel = new JPanel();
        featuresPanel.setLayout(new GridLayout(6, 2, 5, 5));
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add features to existing lots
        addControl(featuresPanel, "Add Pool:", e -> {
            String lotId = getInput("Enter lot ID to add pool (e.g., Lot101):", "Add Pool Feature");
            if (lotId != null && !lotId.isEmpty()) {
                LotComponent decoratedLot = lotManager.addFeatureToLot(lotId, "pool");
                if (decoratedLot != null) {
                    displayArea.setText("Pool added to lot:\n" + decoratedLot.getDescription());
                } else {
                    displayArea.setText("Lot not found with ID: " + lotId);
                }
            }
        });

        addControl(featuresPanel, "Add Fencing:", e -> {
            String lotId = getInput("Enter lot ID to add fencing (e.g., Lot101):", "Add Fencing Feature");
            if (lotId != null && !lotId.isEmpty()) {
                LotComponent decoratedLot = lotManager.addFeatureToLot(lotId, "fencing");
                if (decoratedLot != null) {
                    displayArea.setText("Fencing added to lot:\n" + decoratedLot.getDescription());
                } else {
                    displayArea.setText("Lot not found with ID: " + lotId);
                }
            }
        });

        addControl(featuresPanel, "Add Landscaping:", e -> {
            String lotId = getInput("Enter lot ID to add landscaping (e.g., Lot101):", "Add Landscaping Feature");
            if (lotId != null && !lotId.isEmpty()) {
                LotComponent decoratedLot = lotManager.addFeatureToLot(lotId, "landscaping");
                if (decoratedLot != null) {
                    displayArea.setText("Landscaping added to lot:\n" + decoratedLot.getDescription());
                } else {
                    displayArea.setText("Lot not found with ID: " + lotId);
                }
            }
        });
        
        // Add placeholders for layout
        JLabel placeholder7 = new JLabel("");
        JLabel placeholder8 = new JLabel("");
        JLabel placeholder9 = new JLabel("");
        JLabel placeholder10 = new JLabel("");
        JLabel placeholder11 = new JLabel("");
        JLabel placeholder12 = new JLabel("");
        featuresPanel.add(placeholder7);
        featuresPanel.add(placeholder8);
        featuresPanel.add(placeholder9);
        featuresPanel.add(placeholder10);
        featuresPanel.add(placeholder11);
        featuresPanel.add(placeholder12);
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Main Controls", controlPanel);
        tabbedPane.addTab("Search Options", searchPanel);
        tabbedPane.addTab("Lot Features", featuresPanel);
        
        add(tabbedPane, BorderLayout.SOUTH);
    }

    private void addControl(JPanel panel, String label, ActionListener actionListener) {
        JLabel jLabel = new JLabel(label);
        JButton button = new JButton(label.split(" ")[0]);
        button.addActionListener(actionListener);
        panel.add(jLabel);
        panel.add(button);
    }

    private String getInput(String message, String title) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
    }
}