package com.davidleston.fireflower;

import com.google.common.collect.EnumMultiset;

final class PlayedTiles {
  private final EnumMultiset<Color> playedTiles = EnumMultiset.create(Color.class);

  PlayedTiles() {
  }

  int score() {
    return playedTiles.size();
  }

  int nextPlayable(Color color) {
    return playedTiles.count(color) + 1;
  }

  Event.Visitor eventVisitor() {
    return new Event.Visitor() {
      @Override
      public void doColorHint(ColorHintEvent colorHintEvent) {
      }

      @Override
      public void doDiscard(DiscardEvent discardEvent) {
      }

      @Override
      public void doDraw(DrawEvent drawEvent) {
      }

      @Override
      public void doNumberHint(NumberHintEvent numberHintEvent) {
      }

      @Override
      public void doPlay(PlayEvent playEvent) {
        if (playEvent.wasSuccessful) {
          playedTiles.add(playEvent.tile.color);
        }
      }

      @Override
      public void doReorder(ReorderEvent reorderEvent) {
      }
    };
  }

  @Override
  public String toString() {
    return playedTiles.toString();
  }
}
