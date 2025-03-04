public class ReservedLotDecorator extends LotDecorator {
    
    public ReservedLotDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        // Only change if the lot is not already sold
        if (decoratedLot.getDescription().contains("Status: SOLD")) {
            return decoratedLot.getDescription();
        }
        return decoratedLot.getDescription().replace("Status: Available", "Status: Reserved");
    }
    
    @Override
    public double getPrice() {
        // Price remains unchanged when reserved
        return decoratedLot.getPrice();
    }
}
