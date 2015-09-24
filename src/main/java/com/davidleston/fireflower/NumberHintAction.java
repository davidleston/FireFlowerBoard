package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

class NumberHintAction extends HintAction {
  final int number;

  NumberHintAction(int playerReceivingHint, int number) {
    super(playerReceivingHint);
    this.number = number;
  }

  @Override
  void visit(Visitor visitor) {
    visitor.doNumberHint(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("playerReceivingHint", playerReceivingHint)
        .add("number", number)
        .toString();
  }
}
