package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

import java.util.function.Consumer;

final class DiscardAction extends Action {
  final int position;
  final ReorderAction<DiscardEvent> reorderAction;

  DiscardAction(int position, ReorderAction<DiscardEvent> reorderAction) {
    this.position = position;
    this.reorderAction = reorderAction;
  }

  @Override
  void handleAction(Consumer<PlayAction> doPlay, Consumer<DiscardAction> doDiscard, Consumer<HintAction> doHint) {
    doDiscard.accept(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("position", position)
        .toString();
  }
}
