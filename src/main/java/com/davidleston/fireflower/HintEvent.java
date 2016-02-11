package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

abstract class HintEvent extends Event {
  public final int playerReceivingHint;
  public final ImmutableSet<Integer> hintedPositions;

  HintEvent(int sourcePlayer, int playerReceivingHint, ImmutableSet<Integer> hintedPositions) {
    super(sourcePlayer);
    if (sourcePlayer == playerReceivingHint) {
      throw new CannotHintSelfException();
    }
    this.playerReceivingHint = playerReceivingHint;
    this.hintedPositions = hintedPositions;
  }

  @Override
  protected final MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("playerReceivingHing", playerReceivingHint)
        .add("hintedPositions", hintedPositions);
  }
}
