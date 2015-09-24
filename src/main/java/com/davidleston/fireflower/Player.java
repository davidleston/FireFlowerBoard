package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Player {
  Action takeTurn(Stream<Event> events);
  ImmutableSet<Integer> receiveHint(Stream<Event> events);
  static Player create(Function<Stream<Event>, Action> takeTurn, Function<Stream<Event>, ImmutableSet<Integer>> receiveHint) {
    return new Player() {
      @Override
      public Action takeTurn(Stream<Event> events) {
        return takeTurn.apply(events);
      }

      @Override
      public ImmutableSet<Integer> receiveHint(Stream<Event> events) {
        return receiveHint.apply(events);
      }
    };
  }
}
