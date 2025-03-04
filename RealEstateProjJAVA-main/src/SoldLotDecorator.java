public class SoldLotDecorator extends LotDecorator {
    
    public SoldLotDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        return decoratedLot.getDescription().replace("Status: Available", "Status: SOLD")
                          .replace("Status: Reserved", "Status: SOLD");
    }
    
    @Override
    public double getPrice() {
        // Price remains unchanged when sold
        return decoratedLot.getPrice();
    }
}
