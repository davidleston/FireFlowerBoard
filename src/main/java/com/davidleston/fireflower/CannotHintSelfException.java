package com.davidleston.fireflower;

public final class CannotHintSelfException extends RuntimeException {
  CannotHintSelfException() {
    super("One cannot provide a hint to oneself.");
  }
}
