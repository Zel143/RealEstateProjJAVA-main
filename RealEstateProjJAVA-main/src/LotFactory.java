import java.util.*;

/**
 * Factory class for creating different types of lots with various features
 */
public class LotFactory {
    // Feature constants
    public static final String FEATURE_POOL = "Swimming Pool";
    public static final String FEATURE_LANDSCAPING = "Premium Landscaping";
    public static final String FEATURE_FENCING = "Perimeter Fencing";
    
    // Feature prices
    public static final double PRICE_POOL = 25000.0;
    public static final double PRICE_LANDSCAPING = 12000.0;
    public static final double PRICE_FENCING = 8000.0;
    
    // Status constants
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_SOLD = "SOLD";
    
    // Predefined lot templates
    private static final Map<String, LotTemplate> lotTemplates = new HashMap<>();
    
    // Initialize templates
    static {
        // Standard lot templates by size category
        lotTemplates.put("small", new LotTemplate(200, 250, 100000, 150000));
        lotTemplates.put("medium", new LotTemplate(250, 350, 150000, 225000));
        lotTemplates.put("large", new LotTemplate(350, 500, 225000, 300000));
        lotTemplates.put("premium", new LotTemplate(500, 750, 300000, 500000));
        
        // Special package templates
        lotTemplates.put("starter", new LotTemplate(200, 250, 100000, 150000, 
                                                 Arrays.asList(FEATURE_FENCING)));
        lotTemplates.put("family", new LotTemplate(300, 400, 200000, 275000, 
                                                Arrays.asList(FEATURE_FENCING, FEATURE_LANDSCAPING)));
        lotTemplates.put("luxury", new LotTemplate(400, 600, 275000, 450000, 
                                                Arrays.asList(FEATURE_FENCING, FEATURE_LANDSCAPING, FEATURE_POOL)));
    }
    
    /**
     * Create a basic lot with specific parameters
     */
    public static Lot createBasicLot(int block, int lotNumber, double size, double price) {
        return new Lot(block, lotNumber, size, price);
    }
    
    /**
     * Create a lot based on a template with random size and price within template ranges
     */
    public static LotComponent createFromTemplate(String templateName, int block, int lotNumber) {
        LotTemplate template = lotTemplates.get(templateName.toLowerCase());
        if (template == null) {
            throw new IllegalArgumentException("Unknown lot template: " + templateName);
        }
        
        Random random = new Random();
        double size = template.minSize + random.nextDouble() * (template.maxSize - template.minSize);
        double price = template.minPrice + random.nextDouble() * (template.maxPrice - template.minPrice);
        
        // Round to reasonable values
        size = Math.round(size * 10) / 10.0; // Round to 1 decimal place
        price = Math.round(price / 1000) * 1000; // Round to nearest thousand
        
        // Create the base lot
        LotComponent lot = new Lot(block, lotNumber, size, price);
        
        // Add features if specified in the template
        if (template.features != null) {
            for (String feature : template.features) {
                lot = addFeature(lot, feature);
            }
        }
        
        return lot;
    }
    
    /**
     * Add a feature to a lot
     */
    public static LotComponent addFeature(LotComponent lot, String feature) {
        return switch (feature.toLowerCase()) {
            case "pool" -> new FeatureDecorator(lot, FEATURE_POOL, PRICE_POOL);
            case "fencing" -> new FeatureDecorator(lot, FEATURE_FENCING, PRICE_FENCING);
            case "landscaping" -> new FeatureDecorator(lot, FEATURE_LANDSCAPING, PRICE_LANDSCAPING);
            default -> lot; // No changes if feature not recognized
        };
    }
    
    /**
     * Change status of a lot if needed
     */
    public static LotComponent changeStatus(LotComponent lot, String status) {
        String newStatus = switch (status.toLowerCase()) {
            case "reserved" -> STATUS_RESERVED;
            case "sold" -> STATUS_SOLD;
            default -> null; // No valid status provided
        };
        
        if (newStatus == null) {
            return lot;
        }
        
        // Only create a new decorator if the status would actually change
        if (newStatus.equals(lot.getStatus())) {
            return lot;
        }
        
        return new StatusDecorator(lot, newStatus);
    }
    
    /**
     * Get all available template names
     */
    public static Set<String> getTemplateNames() {
        return lotTemplates.keySet();
    }
    
    /**
     * Get the base lot from potentially decorated component
     */
    public static Lot getBaseLot(LotComponent component) {
        if (component instanceof Lot lot) {
            return lot;
        } else if (component instanceof LotDecorator decorator) {
            return getBaseLot(decorator.getDecoratedLot());
        }
        return null;
    }
    
    /**
     * Check if lot has a specific feature
     */
    public static boolean hasFeature(LotComponent lot, String featureName) {
        return lot.getDescription().contains("+ " + featureName);
    }
    
    /**
     * Template class for lot creation
     */
    private static class LotTemplate {
        final double minSize;
        final double maxSize;
        final double minPrice;
        final double maxPrice;
        final List<String> features;
        
        LotTemplate(double minSize, double maxSize, double minPrice, double maxPrice) {
            this(minSize, maxSize, minPrice, maxPrice, null);
        }
        
        LotTemplate(double minSize, double maxSize, double minPrice, double maxPrice, List<String> features) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.features = features;
        }
    }
}
