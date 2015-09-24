package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

public final class DiscardEvent extends Event {
  public final int positionDiscarded;
  public final Tile tileDiscarded;

  DiscardEvent(int sourcePlayer, int positionDiscarded, Tile tileDiscarded) {
    super(sourcePlayer);
    this.positionDiscarded = positionDiscarded;
    this.tileDiscarded = tileDiscarded;
  }

  @Override
  public void visit(Visitor visitor) {
    visitor.doDiscard(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("sourcePlayer", sourcePlayer)
        .add("positionDiscarded", positionDiscarded)
        .add("tileDiscarded", tileDiscarded)
        .toString();
  }
}
