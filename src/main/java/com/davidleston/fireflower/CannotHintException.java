package com.davidleston.fireflower;

public final class CannotHintException extends RuntimeException {
  CannotHintException() {
    super("Hint balance is at maximum.");
  }
}
