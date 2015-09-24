package com.davidleston.fireflower;

import java.util.*;

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
    List<Tile> tiles = new ArrayList<>();
    int[] distribution = new int[]{3,2,2,2,1};
    for (int number = 0; number < distribution.length; number++) {
      for (int quantity = 0; quantity < distribution[number]; quantity++) {
        for (Color color : colors()) {
          tiles.add(new Tile(color, number + 1));
        }
      }
    }
    Collections.shuffle(tiles, new Random(randomSeed));
    return tiles.iterator();
  }
}
