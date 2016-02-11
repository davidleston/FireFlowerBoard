package com.davidleston.fireflower;

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
    return toStringHelper()
        .add("tile", tile)
        .toString();
  }
}
