import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LotManager {
    private final Map<String, LotComponent> lots = new ConcurrentHashMap<>();
    private final PerformanceCache<SearchCriteria, List<LotComponent>> searchCache;
    
    // Pre-compiled predicates for common queries
    private final Map<String, Predicate<LotComponent>> commonPredicates = new ConcurrentHashMap<>();

    public LotManager() {
        // Initialize search cache (50 entries, 30 second expiration)
        searchCache = new PerformanceCache<>(50, 30000);
        
        // Initialize common predicates
        setupCommonPredicates();
        
        // Try to load existing data first
        Map<String, LotComponent> loadedLots = DataHandler.loadLots();
        if (loadedLots != null && !loadedLots.isEmpty()) {
            lots.putAll(loadedLots);
        } else {
            initializeDefaultLots();
        }
        
        // Add shutdown hook to save data on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveData));
    }
    
    private void setupCommonPredicates() {
        // Available lots
        commonPredicates.put("available", 
            lot -> LotFactory.STATUS_AVAILABLE.equals(lot.getStatus()));
        
        // Reserved lots
        commonPredicates.put("reserved", 
            lot -> LotFactory.STATUS_RESERVED.equals(lot.getStatus()));
        
        // Sold lots
        commonPredicates.put("sold", 
            lot -> LotFactory.STATUS_SOLD.equals(lot.getStatus()));
            
        // Lots with pools
        commonPredicates.put("withPool", 
            lot -> lot.getDescription().contains(LotFactory.FEATURE_POOL));
            
        // Premium lots (> $250k)
        commonPredicates.put("premium", 
            lot -> lot.getPrice() > 250000);
    }

    public boolean saveData() {
        return DataHandler.saveLots(lots);
    }

    private void initializeDefaultLots() {
        // Create 5 blocks with 20 lots each
        for (int block = 1; block <= 5; block++) {
            for (int lotNum = 1; lotNum <= 20; lotNum++) {
                double baseSize = 200 + (block * 20) + (lotNum * 5); 
                double basePrice = 100000 + (block * 15000) + (lotNum * 2500);
                
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
            if (block < 1 || block > 5 || lotNumber < 1 || lotNumber > 20 || 
                size <= 0 || price <= 0) {
                return "Invalid input values";
            }

            String checkId = "Lot" + block + " " + lotNumber;
            if (lots.containsKey(checkId)) {
                return "Lot already exists with this block and lot number";
            }

            Lot newLot = new Lot(block, lotNumber, size, price);
            lots.put(newLot.getId(), newLot);
            clearSearchCache();
            
            return "Lot added successfully:\n" + newLot.getDescription();
        } catch (NumberFormatException e) {
            return "Invalid input: Please enter numeric values";
        }
    }

    public String changeLotStatus(String lotId, String status) {
        LotComponent lotComponent = lots.get(lotId);
        if (lotComponent == null) {
            return "Lot not found";
        }
        
        // Validate status change
        if ("reserve".equalsIgnoreCase(status) && 
            LotFactory.STATUS_SOLD.equals(lotComponent.getStatus())) {
            return "Cannot reserve a sold lot";
        }
        
        // Apply status change using the optimized decorator
        String newStatus = "sell".equals(status.toLowerCase()) || "sold".equals(status.toLowerCase()) 
            ? LotFactory.STATUS_SOLD 
            : LotFactory.STATUS_RESERVED;
        
        // Only apply if it would change the status
        if (!newStatus.equals(lotComponent.getStatus())) {
            lots.put(lotId, new StatusDecorator(lotComponent, newStatus));
            clearSearchCache();
        }
        
        return "Lot has been " + (status.toLowerCase().endsWith("d") ? status : status + "d");
    }
    
    // Simplified status methods that delegate to changeLotStatus
    public String sellLot(String lotId) {
        return changeLotStatus(lotId, "sell");
    }

    public String reserveLot(String lotId) {
        return changeLotStatus(lotId, "reserve");
    }

    public LotComponent addFeatureToLot(String lotId, String feature) {
        LotComponent lot = lots.get(lotId);
        if (lot == null) {
            return null;
        }
        
        // First check if the lot already has this feature
        String featureName = switch (feature.toLowerCase()) {
            case "pool" -> LotFactory.FEATURE_POOL;
            case "fencing" -> LotFactory.FEATURE_FENCING;
            case "landscaping" -> LotFactory.FEATURE_LANDSCAPING;
            default -> null;
        };
        
        if (featureName != null && !LotFactory.hasFeature(lot, featureName)) {
            LotComponent decoratedLot = LotFactory.addFeature(lot, feature);
            lots.put(lotId, decoratedLot);
            clearSearchCache();
            return decoratedLot;
        }
        
        return lot;
    }

    private Lot findBaseLot(LotComponent component) {
        if (component instanceof Lot lot) {
            return lot;
        } else if (component instanceof LotDecorator decorator) {
            return findBaseLot(decorator.getDecoratedLot());
        }
        return null;
    }
    
    // Get lots by a named predicate
    public List<LotComponent> getLotsByPredicate(String predicateName) {
        Predicate<LotComponent> predicate = commonPredicates.get(predicateName.toLowerCase());
        if (predicate == null) {
            return new ArrayList<>();
        }
        
        return lots.values().stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }
    
    // Optimized search method using predicate composition for efficiency
    public List<LotComponent> searchLots(Double minSize, Double maxSize, Double minPrice, 
                                       Double maxPrice, Integer blockNumber, String status) {
        // Create a search criteria key for caching
        SearchCriteria criteria = new SearchCriteria(minSize, maxSize, minPrice, maxPrice, blockNumber, status);
        
        // Use cached results if available
        return searchCache.get(criteria, () -> {
            // Start with an empty predicate (always true)
            Predicate<LotComponent> predicate = lot -> true;
            
            // Apply each filter condition if specified
            if (blockNumber != null) {
                predicate = predicate.and(lot -> getBaseLotBlock(lot) == blockNumber);
            }
            
            if (minSize != null || maxSize != null) {
                predicate = predicate.and(lot -> {
                    double size = getBaseLotSize(lot);
                    return (minSize == null || size >= minSize) && 
                           (maxSize == null || size <= maxSize);
                });
            }
            
            if (minPrice != null || maxPrice != null) {
                predicate = predicate.and(lot -> {
                    double price = lot.getPrice();
                    return (minPrice == null || price >= minPrice) && 
                           (maxPrice == null || price <= maxPrice);
                });
            }
            
            if (status != null) {
                predicate = predicate.and(lot -> lot.getStatus().equals(status));
            }
            
            // Apply the predicate to filter lots
            return lots.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
        });
    }
    
    // Helper methods
    private int getBaseLotBlock(LotComponent lot) {
        Lot baseLot = LotFactory.getBaseLot(lot);
        return baseLot != null ? baseLot.getBlock() : 0;
    }
    
    private double getBaseLotSize(LotComponent lot) {
        Lot baseLot = LotFactory.getBaseLot(lot);
        return baseLot != null ? baseLot.getSize() : 0;
    }
    
    public List<LotComponent> getAllLots() {
        return new ArrayList<>(lots.values());
    }
    
    private void clearSearchCache() {
        searchCache.clear();
    }
    
    // Search criteria class for caching
    private static class SearchCriteria {
        final Double minSize;
        final Double maxSize;
        final Double minPrice;
        final Double maxPrice;
        final Integer blockNumber;
        final String status;
        
        SearchCriteria(Double minSize, Double maxSize, Double minPrice, Double maxPrice, 
                      Integer blockNumber, String status) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.blockNumber = blockNumber;
            this.status = status;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SearchCriteria other)) return false;
            return equals(minSize, other.minSize) &&
                   equals(maxSize, other.maxSize) &&
                   equals(minPrice, other.minPrice) &&
                   equals(maxPrice, other.maxPrice) &&
                   equals(blockNumber, other.blockNumber) &&
                   equals(status, other.status);
        }
        
        private boolean equals(Object a, Object b) {
            return (a == null && b == null) || (a != null && a.equals(b));
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (minSize == null ? 0 : minSize.hashCode());
            hash = 31 * hash + (maxSize == null ? 0 : maxSize.hashCode());
            hash = 31 * hash + (minPrice == null ? 0 : minPrice.hashCode());
            hash = 31 * hash + (maxPrice == null ? 0 : maxPrice.hashCode());
            hash = 31 * hash + (blockNumber == null ? 0 : blockNumber.hashCode());
            hash = 31 * hash + (status == null ? 0 : status.hashCode());
            return hash;
        }
    }
}
