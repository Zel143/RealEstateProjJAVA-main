public class LandscapingDecorator extends LotDecorator {
    private static final double LANDSCAPING_PRICE = 12000.0;
    
    public LandscapingDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        return decoratedLot.getDescription() + " + Premium Landscaping";
    }
    
    @Override
    public double getPrice() {
        return decoratedLot.getPrice() + LANDSCAPING_PRICE;
    }
}
