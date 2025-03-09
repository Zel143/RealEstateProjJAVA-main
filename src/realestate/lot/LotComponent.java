package realestate.lot;

import java.io.Serializable;

/**
 * Base component interface for the decorator pattern
 */
public interface LotComponent extends Serializable {
    /**
     * Serial version UID for serialization
     */
    long serialVersionUID = 1L;
    
    /**
     * Get the lot description
     * @return A human-readable description of the lot
     */
    String getDescription();
    
    /**
     * Get the lot price
     * @return The current price of the lot
     */
    double getPrice();
    
    /**
     * Get the lot status
     * @return The current status of the lot
     */
    String getStatus();
}
