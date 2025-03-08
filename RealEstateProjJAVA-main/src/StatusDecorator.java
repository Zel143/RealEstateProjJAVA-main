/**
 * A decorator that changes the status of lots
 */
public class StatusDecorator extends LotDecorator {
    private final String status;
    
    public StatusDecorator(LotComponent decoratedLot, String status) {
        super(decoratedLot);
        this.status = status;
    }
    
    @Override
    public String getDescription() {
        // Replace status in description using regex to handle all formats
        return decoratedLot.getDescription()
                .replaceAll("Status: \\w+", "Status: " + status);
    }
    
    @Override
    public String getStatus() {
        return status;
    }
    
    /**
     * Returns true if this decorator would cause a status change
     */
    public boolean wouldChangeStatus() {
        return !status.equals(decoratedLot.getStatus());
    }
}
