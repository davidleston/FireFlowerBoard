package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

final class HintAction extends Action implements Predicate<Tile> {
  final int playerReceivingHint;
  private final Predicate<Tile> tileMatcher;
  private final BiConsumer<Consumer<Color>, IntConsumer> doOperation;

  HintAction(int playerReceivingHint, Color color) {
    this.playerReceivingHint = playerReceivingHint;
    tileMatcher = tile -> tile.color == color;
    doOperation = (doColor, doNumber) -> doColor.accept(color);
  }

  HintAction(int playerReceivingHint, int number) {
    this.playerReceivingHint = playerReceivingHint;
    tileMatcher = tile -> tile.number == number;
    doOperation = (doColor, doNumber) -> doNumber.accept(number);
  }

  void handleHint(Consumer<Color> doColor, IntConsumer doNumber) {
    doOperation.accept(doColor, doNumber);
  }

  @Override
  void handleAction(Consumer<PlayAction> doPlay, Consumer<DiscardAction> doDiscard, Consumer<HintAction> doHint) {
    doHint.accept(this);
  }

  @Override
  public boolean test(Tile tile) {
    return tileMatcher.test(tile);
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper toStringHelper = MoreObjects
        .toStringHelper(this);
    doOperation.accept(
        color -> toStringHelper.add("hint", color),
        number -> toStringHelper.add("hint", number));
    return toStringHelper
        .add("playerReceivingHint", playerReceivingHint)
        .toString();
  }
}
