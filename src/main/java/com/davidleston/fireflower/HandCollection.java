package com.davidleston.fireflower;

import com.davidleston.stream.GuavaCollectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class HandCollection {
  private final ImmutableList<List<Tile>> hands;

  HandCollection(int numberOfPlayers, int handSize) {
    this.hands = Stream
        .<List<Tile>>generate(() -> new ArrayList<>(handSize))
        .limit(numberOfPlayers)
        .collect(GuavaCollectors.immutableList());
  }

  Tile get(int player, int position) {
    return hands.get(player).get(position);
  }

  ImmutableSet<Integer> indeciesOfMatchingTiles(int player, Color color) {
    return indeciesOfMatchingTiles(player, tile -> tile.color == color);
  }

  ImmutableSet<Integer> indeciesOfMatchingTiles(int player, int number) {
    return indeciesOfMatchingTiles(player, tile -> tile.number == number);
  }

  private ImmutableSet<Integer> indeciesOfMatchingTiles(int player, Predicate<Tile> predicate) {
    List<Tile> tilesInHand = hands.get(player);
    return IntStream.range(0, tilesInHand.size())
        .filter(index -> predicate.test(tilesInHand.get(index)))
        .boxed()
        .collect(GuavaCollectors.immutableSet());
  }

  Event.Visitor eventVisitor() {
    return new Event.Visitor() {
      @Override
      public void doColorHint(ColorHintEvent colorHintEvent) {
      }

      @Override
      public void doDiscard(DiscardEvent discardEvent) {
        hands.get(discardEvent.sourcePlayer).remove(discardEvent.positionDiscarded);
      }

      @Override
      public void doDraw(DrawEvent drawEvent) {
        hands.get(drawEvent.sourcePlayer).add(drawEvent.tile);
      }

      @Override
      public void doNumberHint(NumberHintEvent numberHintEvent) {
      }

      @Override
      public void doPlay(PlayEvent playEvent) {
        hands.get(playEvent.sourcePlayer).remove(playEvent.position);
      }

      @Override
      public void doReorder(ReorderEvent reorderEvent) {
        List<Tile> hand = hands.get(reorderEvent.sourcePlayer);
        List<Tile> oldHand = new ArrayList<>(hand);
        hand.clear();
        for (int newPosition : reorderEvent.newPositions) {
          hand.add(oldHand.get(newPosition));
        }
      }
    };
  }

  @Override
  public String toString() {
    return hands.toString();
  }
}
