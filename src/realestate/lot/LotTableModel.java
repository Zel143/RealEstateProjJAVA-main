package realestate.lot;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class LotTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Block", "realestate.lot.Lot#", "Size (sqm)", "Price ($)", "Status", "Features"};
    private List<LotComponent> lots;

    public LotTableModel() {
        this.lots = new ArrayList<>();
    }

    public LotTableModel(List<LotComponent> lots) {
        this.lots = lots;
    }

    @Override
    public int getRowCount() {
        return lots.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= lots.size() || rowIndex < 0) {
            return null;
        }
        
        LotComponent lot = lots.get(rowIndex);
        Lot baseLot = LotFactory.getBaseLot(lot);
        
        if (baseLot == null) {
            return null;
        }
        
        return switch (columnIndex) {
            case 0 -> baseLot.getId();
            case 1 -> baseLot.getBlock();
            case 2 -> baseLot.getLotNumber();
            case 3 -> baseLot.getSize();
            case 4 -> lot.getPrice();
            case 5 -> lot.getStatus();
            case 6 -> extractFeatures(lot);
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> String.class;
            case 1, 2 -> Integer.class;
            case 3, 4 -> Double.class;
            case 5, 6 -> String.class;
            default -> Object.class;
        };
    }

    public void setLots(List<LotComponent> lots) {
        this.lots = lots;
        fireTableDataChanged();
    }

    public LotComponent getLotAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < lots.size()) {
            return lots.get(rowIndex);
        }
        return null;
    }

    private String extractFeatures(LotComponent lot) {
        String description = lot.getDescription();
        StringBuilder features = new StringBuilder();
        
        if (description.contains("+ " + LotFactory.FEATURE_POOL)) {
            features.append("Pool ");
        }
        if (description.contains("+ " + LotFactory.FEATURE_LANDSCAPING)) {
            features.append("Landscaping ");
        }
        if (description.contains("+ " + LotFactory.FEATURE_FENCING)) {
            features.append("Fencing ");
        }
        
        return features.toString().trim();
    }
}
