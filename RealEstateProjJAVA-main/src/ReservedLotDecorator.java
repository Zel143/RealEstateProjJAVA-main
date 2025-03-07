public class ReservedLotDecorator extends LotDecorator {
    
    public ReservedLotDecorator(LotComponent decoratedLot) {
        super(decoratedLot);
    }
    
    @Override
    public String getDescription() {
        // Only change if the lot is not already sold
        if (decoratedLot.getStatus().equals("SOLD")) {
            return decoratedLot.getDescription();
        }
        
        // Use regex replacement to ensure all status formats are handled
        return decoratedLot.getDescription()
                .replaceAll("Status: \\w+", "Status: RESERVED");
    }
    
    @Override
    public double getPrice() {
        // Price remains unchanged when reserved
        return decoratedLot.getPrice();
    }
    
    @Override
    public String getStatus() {
        // If the decorated lot is sold, keep it sold
        if (decoratedLot.getStatus().equals("SOLD")) {
            return "SOLD";
        }
        return "RESERVED";
    }
}
