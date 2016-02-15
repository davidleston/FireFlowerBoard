package com.davidleston.fireflower;

public final class DiscardEvent extends TileRevealedEvent {

  DiscardEvent(int sourcePlayer, int positionDiscarded, Tile tileDiscarded) {
    super(sourcePlayer, positionDiscarded, tileDiscarded);
  }

  @Override
  public void handleEvent(Operation operation) {
    operation.doDiscard(this);
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }
}
