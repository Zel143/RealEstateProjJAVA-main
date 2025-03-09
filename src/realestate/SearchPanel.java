package realestate;

import realestate.lot.LotFactory;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

/**
 * Panel for searching properties with various criteria
 */
public class SearchPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // Input fields
    private final JFormattedTextField minSizeField;
    private final JFormattedTextField maxSizeField;
    private final JFormattedTextField minPriceField;
    private final JFormattedTextField maxPriceField;
    private final JComboBox<Integer> blockComboBox;
    private final JComboBox<String> statusComboBox;

    // Action buttons
    private final JButton searchButton;
    private final JButton showAllButton;
    private final JButton resetButton;

    // Validation message
    private final JLabel validationLabel;

    /**
     * Create a new search panel
     * @param listener ActionListener for search controls
     */
    public SearchPanel(ActionListener listener) {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Search Properties",
                TitledBorder.CENTER, TitledBorder.TOP));

        // Create formatters for number fields
        NumberFormatter sizeFormatter = createOptionalNumberFormatter(0.0, 10000.0);
        NumberFormatter priceFormatter = createOptionalNumberFormatter(0.0, 10000000.0);

        // Initialize fields with formatters
        minSizeField = new JFormattedTextField(sizeFormatter);
        maxSizeField = new JFormattedTextField(sizeFormatter);
        minPriceField = new JFormattedTextField(priceFormatter);
        maxPriceField = new JFormattedTextField(priceFormatter);

        // Set sizes for text fields
        minSizeField.setColumns(6);
        maxSizeField.setColumns(6);
        minPriceField.setColumns(8);
        maxPriceField.setColumns(8);

        // Add tooltips
        minSizeField.setToolTipText("Minimum property size in square meters");
        maxSizeField.setToolTipText("Maximum property size in square meters");
        minPriceField.setToolTipText("Minimum property price in dollars");
        maxPriceField.setToolTipText("Maximum property price in dollars");

        // Create combo boxes
        blockComboBox = createBlockComboBox();
        statusComboBox = createStatusComboBox();

        // Add validation label
        validationLabel = new JLabel(" ");
        validationLabel.setForeground(Color.RED);

        // Create search buttons
        searchButton = createButton("Search", "search", listener);
        showAllButton = createButton("Show All", "showAll", listener);
        resetButton = createButton("Reset", "reset", listener);

        // Add cross-field validation
        setupValidation();

        // Create layout panels
        JPanel criteriaPanel = createCriteriaPanel();
        JPanel buttonPanel = createButtonPanel();

        // Add panels to main layout
        add(criteriaPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Create a formatter for optional number fields
     */
    private NumberFormatter createOptionalNumberFormatter(double min, double max) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(true);

        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setMinimum(min);
        formatter.setMaximum(max);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        formatter.setValueClass(Double.class);

        return formatter;
    }

    /**
     * Create the block number combo box
     */
    private JComboBox<Integer> createBlockComboBox() {
        Integer[] blockOptions = {null, 1, 2, 3, 4, 5}; // null means "Any"
        JComboBox<Integer> comboBox = new JComboBox<>(blockOptions);

        comboBox.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) value = "Any";
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        comboBox.setToolTipText("Select property block number");
        return comboBox;
    }

    /**
     * Create the status combo box
     */
    private JComboBox<String> createStatusComboBox() {
        String[] statusOptions = {"Any", LotFactory.STATUS_AVAILABLE, LotFactory.STATUS_RESERVED, LotFactory.STATUS_SOLD};
        JComboBox<String> comboBox = new JComboBox<>(statusOptions);
        comboBox.setToolTipText("Filter by property status");
        return comboBox;
    }

    /**
     * Create a button with action command
     */
    private JButton createButton(String text, String actionCommand, ActionListener listener) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Setup validation between min and max fields
     */
    private void setupValidation() {
        // Add document listeners for validation
        DocumentListener validationListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateFields(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateFields(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateFields(); }
        };

        minSizeField.getDocument().addDocumentListener(validationListener);
        maxSizeField.getDocument().addDocumentListener(validationListener);
        minPriceField.getDocument().addDocumentListener(validationListener);
        maxPriceField.getDocument().addDocumentListener(validationListener);
    }

    /**
     * Validate field values
     */
    private void validateFields() {
        searchButton.setEnabled(true);
        validationLabel.setText(" ");

        // Check size range
        Double minSize = getMinSize();
        Double maxSize = getMaxSize();
        if (minSize != null && maxSize != null && minSize > maxSize) {
            validationLabel.setText("Minimum size cannot exceed maximum size");
            searchButton.setEnabled(false);
            return;
        }

        // Check price range
        Double minPrice = getMinPrice();
        Double maxPrice = getMaxPrice();
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            validationLabel.setText("Minimum price cannot exceed maximum price");
            searchButton.setEnabled(false);
        }
    }

    /**
     * Create the criteria panel with all search fields
     */
    private JPanel createCriteriaPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));

        // Size criteria row
        panel.add(createLabeledPanel("Size (sqm):",
                new JLabel("Min:"), minSizeField,
                new JLabel("Max:"), maxSizeField));

        // Price criteria row
        panel.add(createLabeledPanel("Price ($):",
                new JLabel("Min:"), minPriceField,
                new JLabel("Max:"), maxPriceField));

        // Block criteria row
        panel.add(createLabeledPanel("Block:", blockComboBox));

        // Status criteria row
        panel.add(createLabeledPanel("Status:", statusComboBox));

        // Validation message row
        panel.add(validationLabel);

        return panel;
    }

    /**
     * Create a panel with label and components
     */
    private JPanel createLabeledPanel(String labelText, Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(labelText));

        for (Component component : components) {
            panel.add(component);
        }

        return panel;
    }

    /**
     * Create the button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.add(searchButton);
        panel.add(showAllButton);
        panel.add(resetButton);
        return panel;
    }

    /**
     * Reset all search fields
     */
    public void resetFields() {
        // Clear text fields
        minSizeField.setValue(null);
        maxSizeField.setValue(null);
        minPriceField.setValue(null);
        maxPriceField.setValue(null);

        // Reset combo boxes
        blockComboBox.setSelectedIndex(0);
        statusComboBox.setSelectedIndex(0);

        // Clear validation message
        validationLabel.setText(" ");
        searchButton.setEnabled(true);
    }

    /**
     * Enable/disable all controls
     */
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

    /**
     * Get minimum size value
     */
    public Double getMinSize() {
        Object value = minSizeField.getValue();
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    /**
     * Get maximum size value
     */
    public Double getMaxSize() {
        Object value = maxSizeField.getValue();
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    /**
     * Get minimum price value
     */
    public Double getMinPrice() {
        Object value = minPriceField.getValue();
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    /**
     * Get maximum price value
     */
    public Double getMaxPrice() {
        Object value = maxPriceField.getValue();
        return value instanceof Number ? ((Number) value).doubleValue() : null;
    }

    /**
     * Get selected block number
     */
    public Integer getBlockNumber() {
        return (Integer) blockComboBox.getSelectedItem();
    }

    /**
     * Get selected status
     */
    public String getStatus() {
        String status = (String) statusComboBox.getSelectedItem();
        return "Any".equals(status) ? null : status;
    }
}