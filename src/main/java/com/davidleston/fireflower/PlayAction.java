package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

import java.util.function.Consumer;

final class PlayAction extends Action {
  final int position;
  final ReorderAction<PlayEvent> reorderAction;

  PlayAction(int position, ReorderAction<PlayEvent> reorderAction) {
    this.position = position;
    this.reorderAction = reorderAction;
  }

  @Override
  void handleAction(Consumer<PlayAction> doPlay, Consumer<DiscardAction> doDiscard, Consumer<HintAction> doHint) {
    doPlay.accept(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("position", position)
        .toString();
  }
}
