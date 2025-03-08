/**
 * A decorator that adds features to lots with associated price increases
 */
public class FeatureDecorator extends LotDecorator {
    private final String featureName;
    private final double additionalCost;

    public FeatureDecorator(LotComponent decoratedLot, String featureName, double additionalCost) {
        super(decoratedLot);
        this.featureName = featureName;
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
    
    public String getFeatureName() {
        return featureName;
    }
}
