package com.davidleston.fireflower;

public final class HintMatchesNoTilesException extends RuntimeException {
  HintMatchesNoTilesException(HintAction hintAction) {
    super("Hint matches no tiles: " + hintAction);
  }
}
