package com.davidleston.fireflower;


import com.google.common.collect.ImmutableSet;

public final class ColorHintEvent extends HintEvent {
  public final Color color;

  ColorHintEvent(int sourcePlayer, int playerReceivingHint, ImmutableSet<Integer> hintedPositions, Color color) {
    super(sourcePlayer, playerReceivingHint, hintedPositions);
    this.color = color;
  }

  @Override
  public void visit(Visitor visitor) {
    visitor.doColorHint(this);
  }

  @Override
  public String toString() {
    return toStringHelper()
        .add("color", color)
        .toString();
  }
}
