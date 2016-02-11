package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

public abstract class TileRevealedEvent extends Event {
  public final int position;
  public final Tile tile;

  protected TileRevealedEvent(int sourcePlayer, int position, Tile tile) {
    super(sourcePlayer);
    this.position = position;
    this.tile = tile;
  }

  @Override
  protected final MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("position", position)
        .add("tile", tile);
  }
}
