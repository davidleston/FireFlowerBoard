package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;

@FunctionalInterface
public interface ReorderAction<E extends Event> {
  ImmutableSet<Integer> reorder(E event);
}
