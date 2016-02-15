package com.davidleston.fireflower;

import com.google.common.collect.EnumMultiset;
import com.google.common.collect.Multiset;

final class PlayedTiles {
  private final Multiset<Color> playedTiles = EnumMultiset.create(Color.class);

  PlayedTiles() {
  }

  int score() {
    return playedTiles.size();
  }

  /**
   * @return true if tile was successfully played
   */
  boolean play(Tile tile) {
    if (playedTiles.count(tile.color) + 1 == tile.number) {
      playedTiles.add(tile.color);
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return playedTiles.toString();
  }

}
