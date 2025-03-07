public class SoldLotDecorator extends LotDecorator {
    
    public SoldLotDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        // Use regex replacement to ensure all status formats are handled
        return decoratedLot.getDescription()
                .replaceAll("Status: \\w+", "Status: SOLD");
    }
    
    @Override
    public double getPrice() {
        // Price remains unchanged when sold
        return decoratedLot.getPrice();
    }
    
    // Add a getStatus method for consistent status retrieval
    public String getStatus() {
        return "SOLD";
    }
}
