public class Lot implements LotComponent {
    private String id;
    private int block;
    private int lotNumber;
    private double size;
    private double price;
    private String status;

    public Lot(int block, int lotNumber, double size, double price) {
        this.id = "Lot" + block + " " + lotNumber;
        this.block = block;
        this.lotNumber = lotNumber;
        this.size = size;
        this.price = price;
        this.status = "AVAILABLE"; // Changed to all caps for consistency
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public int getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(int lotNumber) {
        this.lotNumber = lotNumber;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
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
        return status; // Return the actual status field
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getDescription() {
        return "Lot " + block + "-" + lotNumber + 
               " (" + size + " sqm) - $" + price + 
               " - Status: " + status; // Use the status field
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Block: " + block + ", Lot: " + lotNumber + ", Size: " + size + " sqm, Price: $" + price + ", Status: " + status;
    }
}
