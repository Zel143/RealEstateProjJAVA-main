package realestate;

import realestate.lot.LotComponent;
import realestate.lot.LotDecorator;
import realestate.lot.LotFactory;

import java.util.regex.Pattern;

/**
 * A decorator that changes the status of lots
 */
public class StatusDecorator extends LotDecorator {
    // Use a different serialVersionUID name to avoid hiding warning
    private static final long serialVersionUIDStatus = 1L;
    private static final Pattern STATUS_PATTERN = Pattern.compile("Status: \\w+");
    
    private final String status;
    
    /**
     * Creates a new status decorator
     * 
     * @param decoratedLot Base component to decorate
     * @param status New status to apply
     */
    public StatusDecorator(LotComponent decoratedLot, String status) {
        super(decoratedLot);
        this.status = status != null ? status : LotFactory.STATUS_AVAILABLE;
    }
    
    @Override
    public String getDescription() {
        // Replace status in description using regex for efficiency
        String baseDesc = decoratedLot.getDescription();
        return STATUS_PATTERN.matcher(baseDesc).replaceFirst("Status: " + status);
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
    
    /**
     * Creates a decorator only if it would change the status
     * 
     * @param component Component to potentially decorate
     * @param status Status to apply
     * @return New decorator if status changes, or original component if no change
     */
    public static LotComponent applyIfNeeded(LotComponent component, String status) {
        if (component != null && !status.equals(component.getStatus())) {
            return new StatusDecorator(component, status);
        }
        return component;
    }
}
