package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

public final class HintEvent extends Event implements Predicate<Tile> {
  public final int playerReceivingHint;
  public final ImmutableSet<Integer> hintedPositions;
  private final HintAction hintAction;

  HintEvent(int sourcePlayer, ImmutableSet<Integer> hintedPositions, HintAction hintAction) {
    super(sourcePlayer);
    if (hintedPositions.isEmpty()) {
      throw new HintMatchesNoTilesException(hintAction);
    }
    if (sourcePlayer == hintAction.playerReceivingHint) {
      throw new CannotHintSelfException();
    }
    this.playerReceivingHint = hintAction.playerReceivingHint;
    this.hintedPositions = hintedPositions;
    this.hintAction = hintAction;
  }

  public void handleHint(Consumer<Color> doColor, IntConsumer doNumber) {
    hintAction.handleHint(doColor, doNumber);
  }

  @Override
  public void handleEvent(Operation operation) {
    operation.doHint(this);
  }

  @Override
  public boolean test(Tile tile) {
    return hintAction.test(tile);
  }

  @Override
  public String toString() {
    handleHint(
        color -> toStringHelper().add("hint", color),
        number -> toStringHelper().add("hint", number)
    );
    return toStringHelper()
        .add("playerReceivingHing", playerReceivingHint)
        .add("hintedPositions", hintedPositions)
        .toString();
  }
}
