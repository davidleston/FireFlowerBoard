package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

final class PlayAction extends Action {
  final int position;
  final ReorderAction<PlayEvent> reorderAction;

  PlayAction(int position, ReorderAction<PlayEvent> reorderAction) {
    this.position = position;
    this.reorderAction = reorderAction;
  }

  @Override
  void visit(Visitor visitor) {
    visitor.doPlay(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("position", position)
        .toString();
  }
}
