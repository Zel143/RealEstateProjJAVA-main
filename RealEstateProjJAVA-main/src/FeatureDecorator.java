/**
 * A decorator that adds features to lots with associated price increases
 */
public class FeatureDecorator extends LotDecorator {
    // Use a different serialVersionUID name to avoid hiding warning
    private static final long serialVersionUID = 2L;
    
    private final String featureName;
    private final double additionalCost;

    /**
     * Creates a new feature decorator
     * 
     * @param decoratedLot Base component to decorate
     * @param featureName Name of the feature to add
     * @param additionalCost Cost of the feature
     */
    public FeatureDecorator(LotComponent decoratedLot, String featureName, double additionalCost) {
        super(decoratedLot);
        this.featureName = featureName != null ? featureName : "Unknown Feature";
        this.additionalCost = additionalCost;
    }

    @Override
    public String getDescription() {
        return decoratedLot.getDescription() + " + " + featureName;
    }

    @Override
    public double getPrice() {
        return decoratedLot.getPrice() + additionalCost;
    }
    
    /**
     * Get the feature name
     * @return The name of the feature
     */
    public String getFeatureName() {
        return featureName;
    }
    
    /**
     * Get the additional cost
     * @return The cost of this feature
     */
    public double getAdditionalCost() {
        return additionalCost;
    }
    
    /**
     * Check if this decorator adds the specified feature
     * 
     * @param feature Feature to check for
     * @return true if this decorator adds the specified feature
     */
    public boolean hasFeature(String feature) {
        return featureName.equals(feature);
    }
}
