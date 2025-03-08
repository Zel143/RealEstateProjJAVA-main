import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LotManager {
    private Map<String, LotComponent> lots;
    private int lotCounter;

    public LotManager() {
        lots = new HashMap<>();
        lotCounter = 0;
        
        // Initialize with 5 blocks of 20 lots each
        initializeDefaultLots();
    }

    private void initializeDefaultLots() {
        // Create 5 blocks with 20 lots each
        for (int block = 1; block <= 5; block++) {
            for (int lotNum = 1; lotNum <= 20; lotNum++) {
                // Calculate base size and price based on block and lot number
                // Higher block numbers and lot numbers get bigger sizes and higher prices
                double baseSize = 200 + (block * 20) + (lotNum * 5); // Sizes from 225 to 500 sqm
                double basePrice = 100000 + (block * 15000) + (lotNum * 2500); // Prices from $117,500 to $300,000
                
                // Create the lot and add it to the map
                Lot newLot = new Lot(block, lotNum, baseSize, basePrice);
                lots.put(newLot.getId(), newLot);
            }
        }
    }

    public String addLot(String lotDetails) {
        try {
            String[] details = lotDetails.split(",");
            if (details.length != 4) {
                return "Invalid lot details format. Please use: block,lotNumber,size,price";
            }

            int block = Integer.parseInt(details[0]);
            int lotNumber = Integer.parseInt(details[1]);
            double size = Double.parseDouble(details[2]);
            double price = Double.parseDouble(details[3]);

            // Validate input
            if (block < 1 || block > 5) {
                return "Block number must be between 1 and 5";
            }
            if (lotNumber < 1 || lotNumber > 20) {
                return "Lot number must be between 1 and 20";
            }
            if (size <= 0) {
                return "Size must be positive";
            }
            if (price <= 0) {
                return "Price must be positive";
            }

            // Check if the lot already exists - Fix the ID format
            String checkId = "Lot" + block + " " + lotNumber;
            if (lots.containsKey(checkId)) {
                return "Lot already exists with this block and lot number";
            }

            Lot newLot = new Lot(block, lotNumber, size, price);
            lots.put(newLot.getId(), newLot);
            
            return "Lot added successfully:\n" + newLot.getDescription();
        } catch (NumberFormatException e) {
            return "Invalid input: Please enter numeric values for block, lot number, size, and price";
        }
    }

    public String searchLot(String lotId) {
        LotComponent lot = lots.get(lotId);
        if (lot != null) {
            return "Lot found:\n" + lot.getDescription();
        } else {
            return "Lot not found with ID: " + lotId;
        }
    }

    public String sellLot(String lotId) {
        LotComponent lotComponent = lots.get(lotId);
        if (lotComponent != null) {
            // Instead of directly modifying status, use the decorator pattern consistently
            lots.put(lotId, new SoldLotDecorator(lotComponent));
            return "Lot has been sold:\n" + lots.get(lotId).getDescription();
        } else {
            return "Lot not found with ID: " + lotId;
        }
    }

    public String reserveLot(String lotId) {
        LotComponent lotComponent = lots.get(lotId);
        if (lotComponent != null) {
            // Check if already sold
            if (lotComponent.getDescription().contains("Status: SOLD")) {
                return "Cannot reserve lot. Current status: SOLD";
            }
            // Use decorator pattern consistently
            lots.put(lotId, new ReservedLotDecorator(lotComponent));
            return "Lot has been reserved:\n" + lots.get(lotId).getDescription();
        } else {
            return "Lot not found with ID: " + lotId;
        }
    }

    public String generateReport() {
        if (lots.isEmpty()) {
            return "No lots available in the system.";
        }

        StringBuilder report = new StringBuilder("REAL ESTATE PROPERTY REPORT\n");
        report.append("==============================\n");
        report.append(String.format("%-10s %-8s %-8s %-10s %-15s %-10s\n", 
                "ID", "Block", "Lot#", "Size(sqm)", "Price($)", "Status"));
        report.append("------------------------------------------------------\n");

        for (Map.Entry<String, LotComponent> entry : lots.entrySet()) {
            LotComponent component = entry.getValue();
            Lot baseLot = findBaseLot(component);
            
            if (baseLot != null) {
                // Format with base lot info but use the decorated description/price
                report.append(String.format("%-10s %-8d %-8d %-10.2f %-15.2f %-30s\n",
                        entry.getKey(), 
                        baseLot.getBlock(), 
                        baseLot.getLotNumber(), 
                        baseLot.getSize(), 
                        component.getPrice(),
                        component.getStatus())); // Use getStatus directly
            } else {
                report.append(component.getDescription()).append("\n");
            }
        }

        report.append("\nTotal Properties: ").append(lots.size());
        return report.toString();
    }

    public String searchLotsByBlock(int block) {
        List<LotComponent> foundLots = new ArrayList<>();
        for (LotComponent lot : lots.values()) {
            Lot baseLot = findBaseLot(lot);
            if (baseLot != null && baseLot.getBlock() == block) {
                foundLots.add(lot);
            }
        }
        
        return generateSearchResults(foundLots, "Lots in Block " + block);
    }

    public String searchLotsBySize(double minSize, double maxSize) {
        List<LotComponent> foundLots = new ArrayList<>();
        for (LotComponent lot : lots.values()) {
            Lot baseLot = findBaseLot(lot);
            if (baseLot != null) {
                double lotSize = baseLot.getSize();
                if (lotSize >= minSize && lotSize <= maxSize) {
                    foundLots.add(lot);
                }
            }
        }
        
        return generateSearchResults(foundLots, "Lots with size between " + minSize + " and " + maxSize + " sqm");
    }

    public String searchLotsByPrice(double minPrice, double maxPrice) {
        List<LotComponent> foundLots = new ArrayList<>();
        for (LotComponent lot : lots.values()) {
            double lotPrice = lot.getPrice();
            if (lotPrice >= minPrice && lotPrice <= maxPrice) {
                foundLots.add(lot);
            }
        }
        
        return generateSearchResults(foundLots, "Lots with price between $" + minPrice + " and $" + maxPrice);
    }

    private String generateSearchResults(List<LotComponent> lots, String title) {
        if (lots.isEmpty()) {
            return "No lots found matching your criteria.";
        }

        StringBuilder results = new StringBuilder(title + "\n");
        results.append("==============================\n");
        
        for (LotComponent lot : lots) {
            results.append(lot.getDescription()).append("\n");
        }
        
        results.append("\nTotal Properties Found: ").append(lots.size());
        return results.toString();
    }
    
    public LotComponent addFeatureToLot(String lotId, String feature) {
        LotComponent lot = lots.get(lotId);
        if (lot == null) {
            return null;
        }
        
        LotComponent decoratedLot;
        switch (feature.toLowerCase()) {
            case "pool":
                decoratedLot = new PoolDecorator(lot);
                break;
            case "fencing":
                decoratedLot = new FencingDecorator(lot);
                break;
            case "landscaping":
                decoratedLot = new LandscapingDecorator(lot);
                break;
            default:
                return lot; // No changes if feature not recognized
        }
        
        lots.put(lotId, decoratedLot);
        return decoratedLot;
    }

    private Lot findBaseLot(LotComponent component) {
        if (component instanceof Lot lot) {
            return lot;
        } else if (component instanceof LotDecorator decorator) {
            return findBaseLot(decorator.getDecoratedLot());
        }
        return null;
    }

    private String getStatusFromComponent(LotComponent component) {
        // Use the getStatus method we've added to all components
        return component.getStatus();
    }
    
    public List<LotComponent> searchLots(Double minSize, Double maxSize, Double minPrice, Double maxPrice, 
                                        Integer blockNumber, String status) {
        List<LotComponent> results = new ArrayList<>(lots.values());
        
        // Filter by block if specified
        if (blockNumber != null) {
            results = results.stream()
                .filter(lot -> {
                    Lot baseLot = findBaseLot(lot);
                    return baseLot != null && baseLot.getBlock() == blockNumber;
                })
                .collect(Collectors.toList());
        }
        
        // Filter by size range if specified
        if (minSize != null || maxSize != null) {
            double min = (minSize != null) ? minSize : 0;
            double max = (maxSize != null) ? maxSize : Double.MAX_VALUE;
            
            results = results.stream()
                .filter(lot -> {
                    Lot baseLot = findBaseLot(lot);
                    return baseLot != null && baseLot.getSize() >= min && baseLot.getSize() <= max;
                })
                .collect(Collectors.toList());
        }
        
        // Filter by price range if specified
        if (minPrice != null || maxPrice != null) {
            double min = (minPrice != null) ? minPrice : 0;
            double max = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
            
            results = results.stream()
                .filter(lot -> lot.getPrice() >= min && lot.getPrice() <= max)
                .collect(Collectors.toList());
        }
        
        // Filter by status if specified
        if (status != null && !status.isEmpty()) {
            results = results.stream()
                .filter(lot -> lot.getStatus().equals(status))
                .collect(Collectors.toList());
        }
        
        return results;
    }
    
    public List<LotComponent> getAllLots() {
        return new ArrayList<>(lots.values());
    }
}
