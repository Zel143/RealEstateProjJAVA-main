public class FencingDecorator extends LotDecorator {
    private static final double FENCING_PRICE = 8000.0;
    
    public FencingDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        return decoratedLot.getDescription() + " + Perimeter Fencing";
    }
    
    @Override
    public double getPrice() {
        return decoratedLot.getPrice() + FENCING_PRICE;
    }

    @Override
    public String getStatus() {
        return decoratedLot.getStatus(); // Pass through the status
    }
}
