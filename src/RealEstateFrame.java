import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class RealEstateFrame extends JFrame implements ActionListener {
    private List<LotComponent> lots = new ArrayList<>();
    private final LotManager lotManager;
    private JTextArea displayArea;
    private JPanel controlPanel;
    private SearchPanel searchPanel;
    private JTable resultsTable;
    private LotTableModel tableModel;
    private TableRowSorter<LotTableModel> sorter;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private boolean dataModified = false;
    
    public RealEstateFrame() {
        super("Real Estate Management System");
        setLayout(new BorderLayout());
        
        // Add status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
        
        // Initialize lot manager
        lotManager = new LotManager();
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Property Management", createManagementPanel());
        tabbedPane.addTab("Search Properties", createSearchTab());
        add(tabbedPane, BorderLayout.CENTER);
        
        // Initialize lots list
        lots = new ArrayList<>(lotManager.getAllLots());
        
        // Display lots
        showAllLots();
        displayLots();
        
        // Add window listener for saving on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (dataModified) {
                    saveDataBeforeExit();
                }
            }
        });
    }
    
    private void saveDataBeforeExit() {
        int response = JOptionPane.showConfirmDialog(this, 
                                                 "Save changes before exiting?",
                                                 "Save Changes",
                                                 JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            lotManager.saveData();
        }
    }
    
    private JPanel createManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Welcome image
        JLabel welcomeLabel = new JLabel(new ImageIcon("img/welcome_image.png"));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(welcomeLabel, BorderLayout.NORTH);
        
        displayArea = new JTextArea(20, 50);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        controlPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JButton addLotButton = new JButton("Add New Lot");
        addLotButton.addActionListener(_ -> addNewLot());
        
        JButton viewLotsButton = new JButton("View All Lots");
        viewLotsButton.addActionListener(_ -> displayLots());
        
        JButton addDecorationsButton = new JButton("Add Decorations to Lot");
        addDecorationsButton.addActionListener(_ -> addDecorations());
        
        JButton reserveLotButton = new JButton("Reserve Lot");
        reserveLotButton.addActionListener(_ -> changeLotStatus("reserve"));
        
        JButton sellLotButton = new JButton("Sell Lot");
        sellLotButton.addActionListener(_ -> changeLotStatus("sell"));
        
        controlPanel.add(addLotButton);
        controlPanel.add(viewLotsButton);
        controlPanel.add(addDecorationsButton);
        controlPanel.add(reserveLotButton);
        controlPanel.add(sellLotButton);
        
        panel.add(controlPanel, BorderLayout.WEST);
        return panel;
    }
    
    private JPanel createSearchTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        searchPanel = new SearchPanel(this);
        panel.add(searchPanel, BorderLayout.NORTH);
        
        tableModel = new LotTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);
        
        // Enable grid lines
        resultsTable.setShowGrid(true);
        resultsTable.setGridColor(Color.LIGHT_GRAY);
        
        // Center align table headers
        JTableHeader header = resultsTable.getTableHeader();
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        header.setDefaultRenderer(headerRenderer);
        
        panel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addNewLot() {
        try {
            String blockStr = JOptionPane.showInputDialog("Enter Block Number (1-5):");
            String lotStr = JOptionPane.showInputDialog("Enter Lot Number (1-20):");
            String sizeStr = JOptionPane.showInputDialog("Enter Size (sqm):");
            String priceStr = JOptionPane.showInputDialog("Enter Base Price ($):");
            
            int block = Integer.parseInt(blockStr);
            int lotNumber = Integer.parseInt(lotStr);
            double size = Double.parseDouble(sizeStr);
            double price = Double.parseDouble(priceStr);
            
            Lot newLot = new Lot(block, lotNumber, size, price);
            lots.add(newLot);
            lotManager.addLot(block + "," + lotNumber + "," + size + "," + price);
            
            displayArea.setText("New lot added successfully!\n" + newLot.getDescription());
            markDataModified();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayLots() {
        if (lots.isEmpty()) {
            displayArea.setText("No lots available.");
            return;
        }
        
        StringBuilder sb = new StringBuilder("Available Lots:\n\n");
        
        for (int i = 0; i < lots.size(); i++) {
            sb.append(i + 1).append(". ").append(lots.get(i).getDescription()).append("\n");
        }
        
        displayArea.setText(sb.toString());
    }
    
    private void addDecorations() {
        if (lots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No lots available to decorate", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] lotOptions = new String[lots.size()];
        for (int i = 0; i < lots.size(); i++) {
            LotComponent lot = lots.get(i);
            lotOptions[i] = (i + 1) + ". " + getLotBaseDescription(lot);
        }
        
        String selectedLot = (String) JOptionPane.showInputDialog(
            this, "Select a lot to decorate:", "Add Decorations",
            JOptionPane.QUESTION_MESSAGE, null, lotOptions, lotOptions[0]);
        
        if (selectedLot == null) return;
        
        int index = Integer.parseInt(selectedLot.split("\\.")[0]) - 1;
        
        String[] options = {"Landscaping", "Fencing", "Pool", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
            this, "Select decoration to add:", "Add Decorations",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
        
        LotComponent decoratedLot = lots.get(index);
        LotComponent updatedLot = decoratedLot;
        
        switch (choice) {
            case 0: updatedLot = LotFactory.addFeature(decoratedLot, "landscaping"); break;
            case 1: updatedLot = LotFactory.addFeature(decoratedLot, "fencing"); break;
            case 2: updatedLot = LotFactory.addFeature(decoratedLot, "pool"); break;
            default: return;
        }
        
        // Only update if something changed
        if (updatedLot != decoratedLot) {
            lots.set(index, updatedLot);
            displayArea.setText("Decoration added successfully!\n" + updatedLot.getDescription());
            markDataModified();
        } else {
            displayArea.setText("This feature is already applied to the lot.");
        }
    }
    
    private void changeLotStatus(String action) {
        if (lots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No lots available", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String[] lotOptions = new String[lots.size()];
        for (int i = 0; i < lots.size(); i++) {
            LotComponent lot = lots.get(i);
            lotOptions[i] = (i + 1) + ". " + getLotBaseDescription(lot);
        }
        
        String selectedLot = (String) JOptionPane.showInputDialog(
            this, "Select a lot to " + action + ":", "Change Status",
            JOptionPane.QUESTION_MESSAGE, null, lotOptions, lotOptions[0]);
        
        if (selectedLot == null) return;
        
        int index = Integer.parseInt(selectedLot.split("\\.")[0]) - 1;
        LotComponent lot = lots.get(index);
        
        if (action.equals("reserve") && lot.getStatus().equals(LotFactory.STATUS_SOLD)) {
            JOptionPane.showMessageDialog(this, "Cannot reserve a sold lot", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LotComponent updatedLot = LotFactory.changeStatus(lot, action);
        
        // Only update if status changed
        if (updatedLot != lot) {
            lots.set(index, updatedLot);
            displayArea.setText("Lot " + action + "d successfully!\n" + updatedLot.getDescription());
            
            // Update in manager
            Lot baseLot = LotFactory.getBaseLot(lot);
            if (baseLot != null) {
                if (action.equals("reserve")) {
                    lotManager.reserveLot(baseLot.getId());
                } else {
                    lotManager.sellLot(baseLot.getId());
                }
            }
            markDataModified();
        } else {
            displayArea.setText("Lot status already up to date.");
        }
    }
    
    private String getLotBaseDescription(LotComponent lot) {
        Lot baseLot = LotFactory.getBaseLot(lot);
        if (baseLot != null) {
            return baseLot.getId();
        }
        return "Unknown Lot";
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "search" -> performSearch();
            case "showAll" -> showAllLots();
            case "reset" -> {
                searchPanel.resetFields();
                tableModel.setLots(new ArrayList<>());
            }
            default -> { }
        }
    }
    
    private void performSearch() {
        statusLabel.setText("Searching...");
        
        Double minSize = searchPanel.getMinSize();
        Double maxSize = searchPanel.getMaxSize();
        Double minPrice = searchPanel.getMinPrice();
        Double maxPrice = searchPanel.getMaxPrice();
        Integer blockNumber = searchPanel.getBlockNumber();
        String status = searchPanel.getStatus();
        
        try {
            List<LotComponent> results = lotManager.searchLots(minSize, maxSize, minPrice, maxPrice, blockNumber, status);
            tableModel.setLots(results);
            statusLabel.setText("Found " + results.size() + " properties");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error performing search", "Search Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Search error");
        }
    }
    
    private void showAllLots() {
        statusLabel.setText("Loading all properties...");
        List<LotComponent> allLots = lotManager.getAllLots();
        tableModel.setLots(allLots);
        statusLabel.setText("Displaying all " + allLots.size() + " properties");
    }
    
    private void markDataModified() {
        dataModified = true;
    }
}
