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
    
    public LotComponent getDecoratedLot() {
        return decoratedLot;
    }
}
