package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

public final class PlayEvent extends Event {
  public final Tile tile;
  public final int position;
  public final boolean wasSuccessful;

  PlayEvent(int sourcePlayer, Tile tile, int position, boolean wasSuccessful) {
    super(sourcePlayer);
    this.tile = tile;
    this.position = position;
    this.wasSuccessful = wasSuccessful;
  }


  @Override
  public void visit(Visitor visitor) {
    visitor.doPlay(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("tile", tile)
        .add("position", position)
        .add("wasSuccessful", wasSuccessful)
        .toString();
  }
}
