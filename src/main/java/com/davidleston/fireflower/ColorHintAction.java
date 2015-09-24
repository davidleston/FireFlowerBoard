package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

final class ColorHintAction extends HintAction {
  final Color color;

  ColorHintAction(int playerReceivingHint, Color color) {
    super(playerReceivingHint);
    this.color = color;
  }

  @Override
  void visit(Visitor visitor) {
    visitor.doColorHint(this);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("playerReceivingHint", playerReceivingHint)
        .add("color", color)
        .toString();
  }
}
