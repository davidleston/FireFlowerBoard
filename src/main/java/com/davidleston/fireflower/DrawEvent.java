package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

public final class DrawEvent extends Event {
  public final Tile tile;

  DrawEvent(int sourcePlayer, Tile tile) {
    super(sourcePlayer);
    this.tile = tile;
  }

  @Override
  public void visit(Visitor visitor) {
    visitor.doDraw(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("sourcePlayer", sourcePlayer)
        .add("tile", tile)
        .toString();
  }
}
