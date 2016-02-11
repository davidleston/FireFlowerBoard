package com.davidleston.fireflower;

public final class DiscardEvent extends TileRevealedEvent {

  DiscardEvent(int sourcePlayer, int positionDiscarded, Tile tileDiscarded) {
    super(sourcePlayer, positionDiscarded, tileDiscarded);
  }

  @Override
  public void visit(Event.Visitor visitor) {
    visitor.doDiscard(this);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }
}
