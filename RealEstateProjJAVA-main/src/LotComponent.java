import java.io.Serializable;

public interface LotComponent extends Serializable {
    String getDescription();
    double getPrice();
    String getStatus(); // Add this method
    // ...other methods if needed...
}
