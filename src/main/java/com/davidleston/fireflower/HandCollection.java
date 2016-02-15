package com.davidleston.fireflower;

import com.davidleston.stream.GuavaCollectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class HandCollection {
  final Event.Operation eventVisitor;
  private final ImmutableList<List<Tile>> hands;

  HandCollection(int numberOfPlayers, int handSize) {
    this.hands = Stream
        .<List<Tile>>generate(() -> new ArrayList<>(handSize))
        .limit(numberOfPlayers)
        .collect(GuavaCollectors.immutableList());
    eventVisitor = Event.Operation.create(
        discardEvent -> hands.get(discardEvent.sourcePlayer).remove(discardEvent.position),
        drawEvent -> hands.get(drawEvent.sourcePlayer).add(0, drawEvent.tile),
        hintEvent -> {},
        playEvent -> hands.get(playEvent.sourcePlayer).remove(playEvent.position),
        reorderEvent -> {
          List<Tile> hand = hands.get(reorderEvent.sourcePlayer);
          List<Tile> oldHand = new ArrayList<>(hand);
          hand.clear();
          for (int newPosition : reorderEvent.newPositions) {
            hand.add(oldHand.get(newPosition));
          }
        });  }

  Tile get(int player, int position) {
    return hands.get(player).get(position);
  }

  ImmutableSet<Integer> positionsOfMatchingTiles(HintAction hint) {
    List<Tile> tilesInHand = hands.get(hint.playerReceivingHint);
    return IntStream.range(0, tilesInHand.size())
        .filter(index -> hint.test(tilesInHand.get(index)))
        .boxed()
        .collect(GuavaCollectors.immutableSet());
  }

  @Override
  public String toString() {
    return hands.toString();
  }
}
