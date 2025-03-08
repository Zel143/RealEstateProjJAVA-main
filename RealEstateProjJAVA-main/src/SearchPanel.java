import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class SearchPanel extends JPanel {
    private JTextField minSizeField, maxSizeField;
    private JTextField minPriceField, maxPriceField;
    private JComboBox<Integer> blockComboBox;
    private JComboBox<String> statusComboBox;
    private JButton searchButton;
    private JButton showAllButton;
    private JButton resetButton;
    private JLabel validationLabel;
    
    public SearchPanel(ActionListener listener) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Search Properties", 
            TitledBorder.CENTER, TitledBorder.TOP));
        
        JPanel criteriaPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        
        // Size criteria
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.add(new JLabel("Size (sqm): "));
        minSizeField = new JTextField(5);
        maxSizeField = new JTextField(5);
        sizePanel.add(new JLabel("Min:"));
        sizePanel.add(minSizeField);
        sizePanel.add(new JLabel("Max:"));
        sizePanel.add(maxSizeField);
        criteriaPanel.add(sizePanel);
        
        // Price criteria
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.add(new JLabel("Price ($): "));
        minPriceField = new JTextField(7);
        maxPriceField = new JTextField(7);
        pricePanel.add(new JLabel("Min:"));
        pricePanel.add(minPriceField);
        pricePanel.add(new JLabel("Max:"));
        pricePanel.add(maxPriceField);
        criteriaPanel.add(pricePanel);
        
        // Block criteria
        JPanel blockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        blockPanel.add(new JLabel("Block: "));
        Integer[] blockOptions = {null, 1, 2, 3, 4, 5}; // null means "Any"
        blockComboBox = new JComboBox<>(blockOptions);
        blockComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) value = "Any";
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        blockPanel.add(blockComboBox);
        criteriaPanel.add(blockPanel);
        
        // Status criteria
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status: "));
        String[] statusOptions = {"Any", "AVAILABLE", "RESERVED", "SOLD"};
        statusComboBox = new JComboBox<>(statusOptions);
        statusPanel.add(statusComboBox);
        criteriaPanel.add(statusPanel);
        
        // Validation message
        validationLabel = new JLabel(" ");
        validationLabel.setForeground(Color.RED);
        criteriaPanel.add(validationLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(listener);
        
        showAllButton = new JButton("Show All");
        showAllButton.setActionCommand("showAll");
        showAllButton.addActionListener(listener);
        
        resetButton = new JButton("Reset");
        resetButton.setActionCommand("reset");
        resetButton.addActionListener(listener);
        
        buttonPanel.add(searchButton);
        buttonPanel.add(showAllButton);
        buttonPanel.add(resetButton);
        
        add(criteriaPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void resetFields() {
        minSizeField.setText("");
        maxSizeField.setText("");
        minPriceField.setText("");
        maxPriceField.setText("");
        blockComboBox.setSelectedIndex(0);
        statusComboBox.setSelectedIndex(0);
        validationLabel.setText(" ");
    }
    
    public void setControlsEnabled(boolean enabled) {
        minSizeField.setEnabled(enabled);
        maxSizeField.setEnabled(enabled);
        minPriceField.setEnabled(enabled);
        maxPriceField.setEnabled(enabled);
        blockComboBox.setEnabled(enabled);
        statusComboBox.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        showAllButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }
    
    public Double getMinSize() {
        try {
            return minSizeField.getText().isEmpty() ? null : Double.parseDouble(minSizeField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getMaxSize() {
        try {
            return maxSizeField.getText().isEmpty() ? null : Double.parseDouble(maxSizeField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getMinPrice() {
        try {
            return minPriceField.getText().isEmpty() ? null : Double.parseDouble(minPriceField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getMaxPrice() {
        try {
            return maxPriceField.getText().isEmpty() ? null : Double.parseDouble(maxPriceField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getBlockNumber() {
        return (Integer) blockComboBox.getSelectedItem();
    }
    
    public String getStatus() {
        String status = (String) statusComboBox.getSelectedItem();
        return "Any".equals(status) ? null : status;
    }
}
