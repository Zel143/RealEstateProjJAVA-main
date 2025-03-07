public class PoolDecorator extends LotDecorator {
    private static final double POOL_PRICE = 25000.0;
    
    public PoolDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        return decoratedLot.getDescription() + " + Swimming Pool";
    }
    
    @Override
    public double getPrice() {
        return decoratedLot.getPrice() + POOL_PRICE;
    }

    @Override
    public String getStatus() {
        return decoratedLot.getStatus(); // Pass through the status
    }
}
