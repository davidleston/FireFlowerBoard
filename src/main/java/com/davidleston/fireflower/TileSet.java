package com.davidleston.fireflower;

import java.util.*;
import java.util.stream.IntStream;

public enum TileSet {
  WithRainbow() {
    @Override
    protected Set<Color> colors() {
      return EnumSet.allOf(Color.class);
    }
  },
  WithoutRainbow() {
    @Override
    protected Set<Color> colors() {
      EnumSet<Color> set = EnumSet.allOf(Color.class);
      set.remove(Color.Rainbow);
      return set;
    }
  };

  protected abstract Set<Color> colors();

  Iterator<Tile> shuffle(long randomSeed) {
    int[] distribution = new int[]{3,2,2,2,1};
    // save array resizes and memory utilization
    int numberOfTiles = IntStream.of(distribution).sum() * colors().size();
    List<Tile> tiles = new ArrayList<>(numberOfTiles);
    for (Color color : colors()) {
      for (int number = 1; number <= distribution.length; number++) {
        int quantityOfNumber = distribution[number];
        // add the same object multiple times to save memory
        Tile tile = new Tile(color, number);
        for (int quantity = 0; quantity < quantityOfNumber; quantity++) {
          tiles.add(tile);
        }
      }
    }
    Collections.shuffle(tiles, new Random(randomSeed));
    return tiles.iterator();
  }
}
