package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

final class DiscardAction extends Action {
  final int position;
  final ReorderAction<DiscardEvent> reorderAction;

  DiscardAction(int position, ReorderAction<DiscardEvent> reorderAction) {
    this.position = position;
    this.reorderAction = reorderAction;
  }

  @Override
  void visit(Visitor visitor) {
    visitor.doDiscard(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("position", position)
        .toString();
  }
}
