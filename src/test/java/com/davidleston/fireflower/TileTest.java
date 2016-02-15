package com.davidleston.fireflower;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class TileTest {
  @Test
  public void hash() {
    Set<Integer> allItems = new HashSet<>();
    List<Integer> duplicateHashCodes = distinctTiles()
        .map(Tile::hashCode)
        .filter(hash -> !allItems.add(hash))
        .collect(Collectors.toList());
    assertThat(duplicateHashCodes)
        .isEmpty();
  }

  public static Stream<Tile> distinctTiles() {
    return Stream.of(Color.values())
        .flatMap(color -> IntStream.rangeClosed(1, 5)
            .mapToObj(number -> new Tile(color, number)));
  }
}
