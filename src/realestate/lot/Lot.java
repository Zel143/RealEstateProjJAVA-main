package realestate.lot;

/**
 * Concrete component class representing a basic lot
 */
public class Lot implements LotComponent {
    // Use a different serialVersionUID name to avoid hiding warning
    private static final long serialVersionUIDLot = 1L;
    
    private final String id;
    private final int block;
    private final int lotNumber;
    private final double size;
    private double price;
    private String status;

    public Lot(int block, int lotNumber, double size, double price) {
        this.id = "B" + block + " L" + lotNumber;
        this.block = block;
        this.lotNumber = lotNumber;
        this.size = size;
        this.price = price;
        this.status = LotFactory.STATUS_AVAILABLE;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public int getBlock() {
        return block;
    }

    public int getLotNumber() {
        return lotNumber;
    }

    public double getSize() {
        return size;
    }

    @Override
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getDescription() {
        return String.format("Lot %d-%d (%.1f sqm) - $%.2f - Status: %s",
            block, lotNumber, size, price, status);
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Block: %d, Lot: %d, Size: %.1f sqm, Price: $%.2f, Status: %s",
            id, block, lotNumber, size, price, status);
    }
}
