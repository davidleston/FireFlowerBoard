package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;

public final class NumberHintEvent extends HintEvent {
  public final int number;

  NumberHintEvent(int sourcePlayer, int playerReceivingHint, ImmutableSet<Integer> hintedPositions, int number) {
    super(sourcePlayer, playerReceivingHint, hintedPositions);
    this.number = number;
  }

  @Override
  public void visit(Visitor visitor) {
    visitor.doNumberHint(this);
  }

  @Override
  public String toString() {
    return toStringHelper()
        .add("number", number)
        .toString();
  }
}
