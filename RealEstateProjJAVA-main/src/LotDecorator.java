/**
 * Abstract base decorator class for all Lot decorators
 */
public abstract class LotDecorator implements LotComponent {
    protected LotComponent decoratedLot;

    public LotDecorator(LotComponent decoratedLot) {
        this.decoratedLot = decoratedLot;
    }

    @Override
    public String getDescription() {
        return decoratedLot.getDescription();
    }

    @Override
    public double getPrice() {
        return decoratedLot.getPrice();
    }
    
    @Override
    public String getStatus() {
        return decoratedLot.getStatus();
    }

    public LotComponent getDecoratedLot() {
        return decoratedLot;
    }
}
