package com.davidleston.fireflower;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Set;

public final class InvalidCollectionOfPositionsException extends RuntimeException {
  InvalidCollectionOfPositionsException(Set<Integer> expectedPositions, Set<Integer> actualPositions) {
    super(ImmutableMap.of(
        "Missing positions", Sets.difference(expectedPositions, actualPositions),
        "Unexpected positions", Sets.difference(actualPositions, expectedPositions)
    ).toString());
  }
}
