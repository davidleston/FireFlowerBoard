package com.davidleston.fireflower;

public final class Tile {
  public final Color color;
  public final int number;

  public Tile(Color color, int number) {
    this.color = color;
    this.number = number;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Tile && equals((Tile) o);
  }

  public boolean equals(Tile tile) {
    return number == tile.number && color == tile.color;
  }

  @Override
  public int hashCode() {
    // color.ordinal() << 3 will not overflow and assumes this.number is < 8
    // both safe assumptions given possible values
    return (color.ordinal() << 3) + number;
  }

  @Override
  public String toString() {
    return color.toString() + " " + number;
  }
}
