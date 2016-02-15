package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

import java.util.Iterator;

final class GameEndDetector implements Event.Operation {
  static final int numberOfStrikesThatEndsTheGame = 3;
  static final int scoreAfterStrikeOut = 0;
  private static final int perfectScore = 30;
  private final Iterator<Tile> tilesToBeDrawn;
  private final PlayedTiles playedTiles;
  private int strikeCount = 0;
  private int turnsRemainingAfterLastTileDrawn = 0;

  GameEndDetector(Iterator<Tile> tilesToBeDrawn, PlayedTiles playedTiles, int numberOfPlayers) {
    this.tilesToBeDrawn = tilesToBeDrawn;
    this.playedTiles = playedTiles;
    this.turnsRemainingAfterLastTileDrawn = numberOfPlayers;
  }

  boolean isGameOver() {
    return score() != -1;
  }

  int score() {
    if (playedTiles.score() == perfectScore) {
      return perfectScore;
    }

    if (strikeCount == numberOfStrikesThatEndsTheGame) {
      return scoreAfterStrikeOut;
    }

    if (!tilesToBeDrawn.hasNext() && turnsRemainingAfterLastTileDrawn == 0) {
      return playedTiles.score();
    }

    return -1;
  }

  @Override
  public void doDiscard(DiscardEvent discardEvent) {
    if (!tilesToBeDrawn.hasNext()) {
      turnsRemainingAfterLastTileDrawn--;
    }
  }

  @Override
  public void doDraw(DrawEvent drawEvent) {}

  @Override
  public void doHint(HintEvent hintEvent) {
    if (!tilesToBeDrawn.hasNext()) {
      turnsRemainingAfterLastTileDrawn--;
    }
  }

  @Override
  public void doPlay(PlayEvent playEvent) {
    if (!tilesToBeDrawn.hasNext()) {
      turnsRemainingAfterLastTileDrawn--;
    }
    if (!playEvent.wasSuccessful) {
      strikeCount++;
    }
  }

  @Override
  public void doReorder(ReorderEvent reorderEvent) {}

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("strikeCount", strikeCount)
        .add("turnsRemainingAfterLastTileDrawn", turnsRemainingAfterLastTileDrawn)
        .toString();
  }

}
