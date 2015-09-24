package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;

public final class ReorderEvent extends Event {
  public final ImmutableSet<Integer> newPositions;

  ReorderEvent(int sourcePlayer, ImmutableSet<Integer> newPositions) {
    super(sourcePlayer);
    this.newPositions = newPositions;
  }

  @Override
  public void visit(Visitor visitor) {
    visitor.doReorder(this);
  }

  @Override
  public String toString() {
    return newPositions.toString();
  }
}
