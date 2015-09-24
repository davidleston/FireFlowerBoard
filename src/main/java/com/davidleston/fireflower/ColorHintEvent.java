package com.davidleston.fireflower;


import com.google.common.base.MoreObjects;
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
    return MoreObjects
        .toStringHelper(this)
        .add("sourcePlayer", sourcePlayer)
        .add("playerReceivingHing", playerReceivingHint)
        .add("color", color)
        .add("hintedPositions", hintedPositions)
        .toString();
  }
}
