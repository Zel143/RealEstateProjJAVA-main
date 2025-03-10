package realestate.lot;

/**
 * Abstract base decorator class for all realestate.lot.Lot decorators
 */
public abstract class LotDecorator implements LotComponent {
    private static final long serialVersionUID = 1L;

    protected final LotComponent decoratedLot;

    /**
     * Creates a new decorator around a component
     *
     * @param decoratedLot The component to decorate
     * @throws IllegalArgumentException if the component is null
     */
    public LotDecorator(LotComponent decoratedLot) {
        if (decoratedLot == null) {
            throw new IllegalArgumentException("Cannot decorate null component");
        }
        this.decoratedLot = decoratedLot;
    }

    /**
     * Default implementation delegates to decorated component
     */
    @Override
    public String getDescription() {
        return decoratedLot.getDescription();
    }

    /**
     * Default implementation delegates to decorated component
     */
    @Override
    public double getPrice() {
        return decoratedLot.getPrice();
    }

    /**
     * Default implementation delegates to decorated component
     */
    @Override
    public String getStatus() {
        return decoratedLot.getStatus();
    }

    /**
     * Get the decorated component
     *
     * @return The component that this decorator wraps
     */
    public LotComponent getDecoratedLot() {
        return decoratedLot;
    }

    /**
     * Unwraps all decorators to find the base lot
     *
     * @return The base realestate.lot.Lot or null if not found
     */
    public Lot getBaseLot() {
        return LotFactory.getBaseLot(this);
    }
}
