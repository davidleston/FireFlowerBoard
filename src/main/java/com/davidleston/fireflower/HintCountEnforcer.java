package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

final class HintCountEnforcer implements Event.Operation {
  private int hintsLeft = Game.numberOfHintTokens;

  @Override
  public void doDiscard(DiscardEvent discardEvent) {
    if (hintsLeft == Game.numberOfHintTokens) {
      throw new CannotDiscardException();
    }
    hintsLeft++;
  }

  @Override
  public void doDraw(DrawEvent drawEvent) {
  }

  @Override
  public void doHint(HintEvent hintEvent) {
    if (hintsLeft == 0) {
      throw new CannotHintException();
    }
    hintsLeft--;
  }

  @Override
  public void doPlay(PlayEvent playEvent) {
    if (playEvent.wasSuccessful && playEvent.tile.number == 5 && hintsLeft < Game.numberOfHintTokens) {
      hintsLeft++;
    }
  }

  @Override
  public void doReorder(ReorderEvent reorderEvent) {
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("hintsLeft", hintsLeft)
        .toString();
  }
}
