package com.davidleston.fireflower;

public final class HintMatchesNoTilesException extends RuntimeException {
  HintMatchesNoTilesException(Object hint) {
    super("Hint matches no tiles: " + hint);
  }
}
