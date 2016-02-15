package com.davidleston.fireflower;

public final class PlayEvent extends TileRevealedEvent {
  public final boolean wasSuccessful;

  PlayEvent(int sourcePlayer, int positionPlayed, Tile tilePlayed, boolean wasSuccessful) {
    super(sourcePlayer, positionPlayed, tilePlayed);
    this.wasSuccessful = wasSuccessful;
  }

  @Override
  public void handleEvent(Operation operation) {
    operation.doPlay(this);
  }

  @Override
  public String toString() {
    return toStringHelper()
        .add("wasSuccessful", wasSuccessful)
        .toString();
  }
}
