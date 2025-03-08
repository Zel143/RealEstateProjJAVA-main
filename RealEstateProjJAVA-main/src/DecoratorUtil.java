import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class for working with the decorator pattern
 */
public final class DecoratorUtil {
    
    // Private constructor to prevent instantiation
    private DecoratorUtil() {}
    
    /**
     * Unwraps all decorators and returns the base component
     * 
     * @param component The decorated component
     * @param <B> Expected base component type
     * @param baseClass Class of the base component
     * @return The unwrapped base component, or null if not found or wrong type
     */
    @SuppressWarnings("unchecked")
    public static <B> B unwrapToBase(LotComponent component, Class<B> baseClass) {
        LotComponent current = component;
        
        while (current instanceof LotDecorator decorator) {
            current = decorator.getDecoratedLot();
        }
        
        return baseClass.isInstance(current) ? (B) current : null;
    }
    
    /**
     * Find all decorators that match a predicate
     * 
     * @param component The decorated component to search
     * @param predicate Predicate to match decorators
     * @return List of matching decorators
     */
    public static List<LotDecorator> findDecorators(LotComponent component, 
                                                   Predicate<LotDecorator> predicate) {
        List<LotDecorator> result = new ArrayList<>();
        LotComponent current = component;
        
        while (current instanceof LotDecorator decorator) {
            if (predicate.test(decorator)) {
                result.add(decorator);
            }
            current = decorator.getDecoratedLot();
        }
        
        return result;
    }
    
    /**
     * Find all decorators of a specific type
     * 
     * @param component The decorated component to search
     * @param decoratorClass The decorator class to find
     * @return List of matching decorators
     */
    @SuppressWarnings("unchecked")
    public static <T extends LotDecorator> List<T> findDecoratorsOfType(
            LotComponent component, Class<T> decoratorClass) {
        return findDecorators(component, decoratorClass::isInstance).stream()
            .map(d -> (T) d)
            .collect(Collectors.toList());
    }
    
    /**
     * Get the total additional cost from all feature decorators
     * 
     * @param component The decorated component
     * @return Sum of all feature costs
     */
    public static double getTotalFeatureCost(LotComponent component) {
        List<FeatureDecorator> features = findDecoratorsOfType(component, FeatureDecorator.class);
        return features.stream()
            .mapToDouble(FeatureDecorator::getAdditionalCost)
            .sum();
    }
    
    /**
     * Get list of all features applied to a lot
     * 
     * @param component The decorated component
     * @return List of feature names
     */
    public static List<String> getFeatureNames(LotComponent component) {
        List<FeatureDecorator> features = findDecoratorsOfType(component, FeatureDecorator.class);
        return features.stream()
            .map(FeatureDecorator::getFeatureName)
            .collect(Collectors.toList());
    }
}
