package com.davidleston.fireflower;

import org.junit.Test;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class TileTest {
  @Test
  public void hash() {
    Stream<Integer> duplicateHashCodes = distinctTiles()
        .map(Tile::hashCode);
    assertThat(duplicateHashCodes)
        .doesNotHaveDuplicates();
  }

  static Stream<Tile> distinctTiles() {
    return Stream.of(Color.values())
        .flatMap(color -> IntStream.rangeClosed(1, 5)
            .mapToObj(number -> new Tile(color, number)));
  }
}
