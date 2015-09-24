package com.davidleston.fireflower;

public final class CannotDiscardException extends RuntimeException {
  CannotDiscardException() {
    super("There must be a balance of at least one hint given in order to discard.");
  }
}
