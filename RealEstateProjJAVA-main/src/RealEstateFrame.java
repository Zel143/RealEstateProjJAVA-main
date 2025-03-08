import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableRowSorter;

public class RealEstateFrame extends JFrame implements ActionListener {
    private List<LotComponent> lots = new ArrayList<>();
    private LotManager lotManager;
    private JTextArea displayArea;
    private JPanel controlPanel;
    private SearchPanel searchPanel;
    private JTable resultsTable;
    private LotTableModel tableModel;
    private TableRowSorter<LotTableModel> sorter;
    
    public RealEstateFrame() {
        super("Real Estate Management System");
        setLayout(new BorderLayout());
        
        // Initialize lot manager
        lotManager = new LotManager();
        
        // Create tabbed pane for different sections
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1: Property Management
        JPanel managementPanel = createManagementPanel();
        tabbedPane.addTab("Property Management", managementPanel);
        
        // Tab 2: Search Properties
        JPanel searchTab = createSearchTab();
        tabbedPane.addTab("Search Properties", searchTab);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Initialize the display area
        displayArea = new JTextArea(20, 50);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create control panel
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 1, 5, 5));
        
        // Add lot button
        JButton addLotButton = new JButton("Add New Lot");
        addLotButton.addActionListener(e -> addNewLot());
        
        // View lots button
        JButton viewLotsButton = new JButton("View All Lots");
        viewLotsButton.addActionListener(e -> displayLots());
        
        // Add decorations button
        JButton addDecorationsButton = new JButton("Add Decorations to Lot");
        addDecorationsButton.addActionListener(e -> addDecorations());
        
        // Status change buttons
        JButton reserveLotButton = new JButton("Reserve Lot");
        reserveLotButton.addActionListener(e -> changeLotStatus("reserve"));
        
        JButton sellLotButton = new JButton("Sell Lot");
        sellLotButton.addActionListener(e -> changeLotStatus("sell"));
        
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
        
        // Add search panel
        searchPanel = new SearchPanel(this);
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // Create table for search results
        tableModel = new LotTableModel();
        resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set up sorting
        sorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(sorter);
        
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
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
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayLots() {
        if (lots.isEmpty()) {
            displayArea.setText("No lots available.");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Available Lots:\n\n");
        
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
        
        switch (choice) {
            case 0: // Landscaping
                decoratedLot = new LandscapingDecorator(decoratedLot);
                break;
            case 1: // Fencing
                decoratedLot = new FencingDecorator(decoratedLot);
                break;
            case 2: // Pool
                decoratedLot = new PoolDecorator(decoratedLot);
                break;
            default:
                return;
        }
        
        lots.set(index, decoratedLot);
        displayArea.setText("Decoration added successfully!\n" + decoratedLot.getDescription());
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
        
        if (action.equals("reserve") && lot.getStatus().equals("SOLD")) {
            JOptionPane.showMessageDialog(this, "Cannot reserve a sold lot", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LotComponent updatedLot;
        if (action.equals("reserve")) {
            updatedLot = new ReservedLotDecorator(lot);
            displayArea.setText("Lot reserved successfully!\n" + updatedLot.getDescription());
        } else {
            updatedLot = new SoldLotDecorator(lot);
            displayArea.setText("Lot sold successfully!\n" + updatedLot.getDescription());
        }
        
        lots.set(index, updatedLot);
        
        // Get base lot to get ID
        Lot baseLot = findBaseLot(lot);
        if (baseLot != null) {
            if (action.equals("reserve")) {
                lotManager.reserveLot(baseLot.getId());
            } else {
                lotManager.sellLot(baseLot.getId());
            }
        }
    }
    
    private String getLotBaseDescription(LotComponent lot) {
        // Get the base lot description without decorations
        if (lot instanceof LotDecorator) {
            return getLotBaseDescription(((LotDecorator) lot).getDecoratedLot());
        } else {
            return ((Lot) lot).getId(); 
        }
    }
    
    private Lot findBaseLot(LotComponent component) {
        if (component instanceof Lot) {
            return (Lot) component;
        } else if (component instanceof LotDecorator) {
            return findBaseLot(((LotDecorator) component).getDecoratedLot());
        }
        return null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch (command) {
            case "search":
                performSearch();
                break;
            case "showAll":
                showAllLots();
                break;
            case "reset":
                searchPanel.resetFields();
                tableModel.setLots(new ArrayList<>());
                break;
        }
    }
    
    private void performSearch() {
        Double minSize = searchPanel.getMinSize();
        Double maxSize = searchPanel.getMaxSize();
        Double minPrice = searchPanel.getMinPrice();
        Double maxPrice = searchPanel.getMaxPrice();
        Integer blockNumber = searchPanel.getBlockNumber();
        String status = searchPanel.getStatus();
        
        List<LotComponent> results = lotManager.searchLots(minSize, maxSize, minPrice, maxPrice, blockNumber, status);
        tableModel.setLots(results);
    }
    
    private void showAllLots() {
        List<LotComponent> allLots = lotManager.getAllLots();
        tableModel.setLots(allLots);
    }
}