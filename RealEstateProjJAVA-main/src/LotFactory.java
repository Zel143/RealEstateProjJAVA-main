import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Factory class for creating different types of lots with various features
 */
public final class LotFactory {
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
    
    // Regular expressions for feature detection (compiled once for performance)
    private static final Pattern POOL_PATTERN = Pattern.compile("\\+ " + Pattern.quote(FEATURE_POOL));
    private static final Pattern LANDSCAPING_PATTERN = Pattern.compile("\\+ " + Pattern.quote(FEATURE_LANDSCAPING));
    private static final Pattern FENCING_PATTERN = Pattern.compile("\\+ " + Pattern.quote(FEATURE_FENCING));
    
    // Cache for feature detection
    private static final Map<String, Boolean> featureCache = new ConcurrentHashMap<>();
    
    // Map of feature name to decorator function
    private static final Map<String, Function<LotComponent, LotComponent>> featureDecorators = new HashMap<>();
    
    // Map of status name to decorator function  
    private static final Map<String, Function<LotComponent, LotComponent>> statusDecorators = new HashMap<>();
    
    // Predefined lot templates
    private static final Map<String, LotTemplate> lotTemplates = new HashMap<>();
    
    // Static initialization
    static {
        // Initialize feature decorators
        featureDecorators.put("pool", lot -> new FeatureDecorator(lot, FEATURE_POOL, PRICE_POOL));
        featureDecorators.put("fencing", lot -> new FeatureDecorator(lot, FEATURE_FENCING, PRICE_FENCING));
        featureDecorators.put("landscaping", lot -> new FeatureDecorator(lot, FEATURE_LANDSCAPING, PRICE_LANDSCAPING));
        
        // Initialize status decorators
        statusDecorators.put("reserved", lot -> new StatusDecorator(lot, STATUS_RESERVED));
        statusDecorators.put("sold", lot -> new StatusDecorator(lot, STATUS_SOLD));
        
        // Initialize lot templates
        lotTemplates.put("small", new LotTemplate(200, 250, 100000, 150000));
        lotTemplates.put("medium", new LotTemplate(250, 350, 150000, 225000));
        lotTemplates.put("large", new LotTemplate(350, 500, 225000, 300000));
        lotTemplates.put("premium", new LotTemplate(500, 750, 300000, 500000));
        
        // Special package templates
        lotTemplates.put("starter", new LotTemplate(200, 250, 100000, 150000, 
                                                 List.of(FEATURE_FENCING)));
        lotTemplates.put("family", new LotTemplate(300, 400, 200000, 275000, 
                                                List.of(FEATURE_FENCING, FEATURE_LANDSCAPING)));
        lotTemplates.put("luxury", new LotTemplate(400, 600, 275000, 450000, 
                                                List.of(FEATURE_FENCING, FEATURE_LANDSCAPING, FEATURE_POOL)));
    }
    
    // Private constructor to prevent instantiation
    private LotFactory() {
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
     * Add a feature to a lot, only if not already present
     * 
     * @param lot The lot to decorate
     * @param feature Feature name to add
     * @return Decorated lot or original if feature already present
     */
    public static LotComponent addFeature(LotComponent lot, String feature) {
        if (lot == null || feature == null) {
            return lot;
        }
        
        // Normalize feature name for lookup
        String normalizedFeature = feature.toLowerCase().trim();
        
        // Check if lot already has this feature
        String featureFullName = getFeatureFullName(normalizedFeature);
        if (featureFullName != null && hasFeature(lot, featureFullName)) {
            return lot; // Don't add duplicate features
        }
        
        // Apply decorator if available
        Function<LotComponent, LotComponent> decorator = featureDecorators.get(normalizedFeature);
        return decorator != null ? decorator.apply(lot) : lot;
    }
    
    /**
     * Get the full feature name from short name
     */
    private static String getFeatureFullName(String shortName) {
        return switch (shortName) {
            case "pool" -> FEATURE_POOL;
            case "fencing" -> FEATURE_FENCING;
            case "landscaping" -> FEATURE_LANDSCAPING;
            default -> null;
        };
    }
    
    /**
     * Change status of a lot if needed
     */
    public static LotComponent changeStatus(LotComponent lot, String status) {
        if (lot == null || status == null) {
            return lot;
        }
        
        // Normalize status for lookup
        String normalizedStatus = status.toLowerCase().trim();
        
        // Map status aliases to canonical forms
        normalizedStatus = switch (normalizedStatus) {
            case "sell" -> "sold";
            case "reserve" -> "reserved";
            default -> normalizedStatus;
        };
        
        // Verify status is different before applying
        String newStatus = switch (normalizedStatus) {
            case "reserved" -> STATUS_RESERVED;
            case "sold" -> STATUS_SOLD;
            default -> null;
        };
        
        if (newStatus == null || newStatus.equals(lot.getStatus())) {
            return lot;
        }
        
        // Apply decorator
        Function<LotComponent, LotComponent> decorator = statusDecorators.get(normalizedStatus);
        return decorator != null ? decorator.apply(lot) : lot;
    }
    
    /**
     * Get all available template names
     */
    public static Set<String> getTemplateNames() {
        return Collections.unmodifiableSet(lotTemplates.keySet());
    }
    
    /**
     * Get the base lot from potentially decorated component
     * @param component The potentially decorated lot component
     * @return Base lot or null if not found
     */
    public static Lot getBaseLot(LotComponent component) {
        if (component == null) {
            return null;
        }
        
        LotComponent current = component;
        while (current instanceof LotDecorator decorator) {
            current = decorator.getDecoratedLot();
        }
        
        return (current instanceof Lot) ? (Lot)current : null;
    }
    
    /**
     * Find all decorators of a specific type in the decorator chain
     * 
     * @param <T> Type of decorator to find
     * @param lot Component to search in
     * @param type Type of decorator to find
     * @return List of all matching decorators
     */
    @SuppressWarnings("unchecked")
    public static <T extends LotDecorator> List<T> findDecoratorsOfType(LotComponent lot, Class<T> type) {
        List<T> found = new ArrayList<>();
        
        LotComponent current = lot;
        while (current instanceof LotDecorator decorator) {
            if (type.isInstance(decorator)) {
                found.add((T)decorator);
            }
            current = decorator.getDecoratedLot();
        }
        
        return found;
    }
    
    /**
     * Check if lot has a specific feature - using optimized detection
     */
    public static boolean hasFeature(LotComponent lot, String featureName) {
        if (lot == null || featureName == null) {
            return false;
        }
        
        // Check in feature decorators first (faster)
        List<FeatureDecorator> features = findDecoratorsOfType(lot, FeatureDecorator.class);
        for (FeatureDecorator decorator : features) {
            if (decorator.hasFeature(featureName)) {
                return true;
            }
        }
        
        // Cache key for description-based checks
        String cacheKey = System.identityHashCode(lot) + ":" + featureName;
        
        // Check cache
        Boolean cached = featureCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Fallback to description-based check
        boolean hasFeature = featurePatternCheck(lot.getDescription(), featureName);
        
        // Update cache
        featureCache.put(cacheKey, hasFeature);
        
        return hasFeature;
    }
    
    /**
     * Check for feature using pattern matching on description
     */
    private static boolean featurePatternCheck(String description, String featureName) {
        return switch(featureName) {
            case FEATURE_POOL -> POOL_PATTERN.matcher(description).find();
            case FEATURE_LANDSCAPING -> LANDSCAPING_PATTERN.matcher(description).find();
            case FEATURE_FENCING -> FENCING_PATTERN.matcher(description).find();
            default -> description.contains("+ " + featureName);
        };
    }
    
    /**
     * Create a premium lot with all features
     */
    public static LotComponent createPremiumLot(int block, int lotNumber, double size, double basePrice) {
        LotComponent lot = createBasicLot(block, lotNumber, size, basePrice);
        lot = addFeature(lot, "fencing");
        lot = addFeature(lot, "landscaping");
        lot = addFeature(lot, "pool");
        return lot;
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
