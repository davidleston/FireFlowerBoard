package com.davidleston.fireflower;

public final class Tile {
  public final Color color;
  public final int number;

  public Tile(Color color, int number) {
    this.color = color;
    this.number = number;
  }

  @Override
  public String toString() {
    return color.toString() + " " + number;
  }
}
