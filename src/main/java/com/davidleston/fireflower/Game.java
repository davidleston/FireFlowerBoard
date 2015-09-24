package com.davidleston.fireflower;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.stream.Stream;

public final class Game {
  public static final int handSizeForThreeOrFewerPlayers = 5;
  public static final int handSizeForFourOrMorePlayers = 4;
  public static final int numberOfHintTokens = 8;
  public static final int numberOfStrikesThatEndsTheGame = 3;
  public static final int scoreAfterStrikeOut = 0;
  static final int gameEndedByTest = -1;
  private static final int perfectScore = 30;

  private final HandCollection hands;
  private final Iterator<Tile> tilesToBeDrawn;
  private final Collection<Event.Visitor> eventVisitors = new ArrayList<>();
  private final EventQueueCollection eventQueues;
  private final PlayedTiles playedTiles;
  private final int numberOfPlayers;
  private final int handSize;
  private final ImmutableList<Player> players;
  private int currentPlayer = 0;
  private int strikeCount = 0;
  private int turnsSinceLastTileDrawn = 0;

  @VisibleForTesting
  Game(int numberOfPlayers, Iterator<Tile> tilesToBeDrawn, ImmutableList<Player> players) {
    this.tilesToBeDrawn = tilesToBeDrawn;
    this.numberOfPlayers = numberOfPlayers;
    this.players = players;
    this.handSize = numberOfPlayers > 3 ? handSizeForFourOrMorePlayers : handSizeForThreeOrFewerPlayers;

    this.hands = new HandCollection(numberOfPlayers, handSize);
    eventVisitors.add(this.hands.eventVisitor());

    this.eventQueues = new EventQueueCollection(numberOfPlayers);
    eventVisitors.add(this.eventQueues.eventVisitor());

    eventVisitors.add(new HintCountEnforcer());

    this.playedTiles = new PlayedTiles();
    eventVisitors.add(this.playedTiles.eventVisitor());
  }


  /**
   * @return score
   */
  public static int newGame(TileSet setOfTilesToPlayWith, long randomSeed, ImmutableList<Player> players) {
    return newGame(setOfTilesToPlayWith.shuffle(randomSeed), players);
  }

  /**
   * @return score
   */
  @VisibleForTesting
  static int newGame(Iterator<Tile> tilesToBeDrawn, ImmutableList<Player> players) {
    Game game = new Game(players.size(), tilesToBeDrawn, players);
    game.drawFirstTiles();
    return game.start();
  }

  /**
   * @return score
   */
  @VisibleForTesting
  static int newGame(Iterator<Tile> tilesToBeDrawn, ImmutableList<Player> players, Iterable<Event> events) {
    Game game = new Game(players.size(), tilesToBeDrawn, players);
    for (Event event : events) {
      game.addEvent(event);
    }
    return game.start();
  }

  private void drawFirstTiles() {
    for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
      for (int positionDrawnInto = 0; positionDrawnInto < handSize; positionDrawnInto++) {
        addEvent(new DrawEvent(playerIndex, tilesToBeDrawn.next()));
      }
    }
  }

  /**
   * @return score
   */
  private int start() {
    try {
      while (true) {
        if (!tilesToBeDrawn.hasNext()) {
          turnsSinceLastTileDrawn++;
        }

        Action action = players.get(currentPlayer).takeTurn(eventQueues.eventsFor(currentPlayer));
        action.visit(new Action.Visitor() {
          @Override
          public void doPlay(PlayAction playAction) {
            Tile playedTile = hands.get(currentPlayer, playAction.position);
            boolean wasSuccessful = playedTiles.nextPlayable(playedTile.color) == playedTile.number;
            PlayEvent event = new PlayEvent(currentPlayer, playedTile, playAction.position, wasSuccessful);
            addEvent(event);
            if (!wasSuccessful) {
              strikeCount++;
            }
            draw();
            reorder(currentPlayer, playAction.reorderAction, event);
          }

          @Override
          public void doDiscard(DiscardAction discardAction) {
            Tile discardedTile = hands.get(currentPlayer, discardAction.position);
            DiscardEvent event = new DiscardEvent(currentPlayer, discardAction.position, discardedTile);
            addEvent(event);
            draw();
            reorder(currentPlayer, discardAction.reorderAction, event);
          }

          @Override
          public void doColorHint(ColorHintAction hintAction) {
            ImmutableSet<Integer> hintedPositions
                = hands.indeciesOfMatchingTiles(hintAction.playerReceivingHint, hintAction.color);
            if (hintedPositions.isEmpty()) {
              throw new HintMatchesNoTilesException(hintAction.color);
            }
            addEvent(new ColorHintEvent(currentPlayer, hintAction.playerReceivingHint, hintedPositions,
                hintAction.color));
            informHintedPlayer(hintAction);
          }

          @Override
          public void doNumberHint(NumberHintAction hintAction) {
            ImmutableSet<Integer> hintedPositions
                = hands.indeciesOfMatchingTiles(hintAction.playerReceivingHint, hintAction.number);
            if (hintedPositions.isEmpty()) {
              throw new HintMatchesNoTilesException(hintAction.number);
            }
            addEvent(new NumberHintEvent(currentPlayer, hintAction.playerReceivingHint, hintedPositions, hintAction.number));
            informHintedPlayer(hintAction);
          }

          private void informHintedPlayer(HintAction hintAction) {
            Stream<Event> eventsForHintedPlayer = eventQueues.eventsFor(hintAction.playerReceivingHint);
            ImmutableSet<Integer> newPositionsFromHintedPlayer
                = players.get(hintAction.playerReceivingHint).receiveHint(eventsForHintedPlayer);
            reorder(hintAction.playerReceivingHint, newPositionsFromHintedPlayer);
          }

          private <E extends Event> void reorder(int player, ReorderAction<E> reorderAction, E event) {
            // TODO: handle reorder when there are fewer than hand size in hand
            if (reorderAction != null) {
              ImmutableSet<Integer> newPositions = reorderAction.reorder(event);
              reorder(player, newPositions);
            }
          }

          private void reorder(int player, ImmutableSet<Integer> newPositions) {
            ensureReorderContainsAllPositions(newPositions);
            if (isReordering(newPositions)) {
              addEvent(new ReorderEvent(player, newPositions));
            }
          }

          private void draw() {
            if (tilesToBeDrawn.hasNext()) {
              addEvent(new DrawEvent(currentPlayer, tilesToBeDrawn.next()));
            }
          }

          private void ensureReorderContainsAllPositions(ImmutableSet<Integer> newPositions) {
            Set<Integer> copy = new HashSet<>(newPositions);
            for (int i = 0; i < handSize; i++) {
              if (!copy.remove(i)) {
                throw new InvalidCollectionOfPositionsException("Missing position " + i);
              }
            }
            if (!copy.isEmpty()) {
              throw new InvalidCollectionOfPositionsException("Unexpected positions: " + copy);
            }
          }

          private boolean isReordering(ImmutableSet<Integer> newPositions) {
            Iterator<Integer> iterator = newPositions.iterator();
            for (int i = 0; i < handSize; i++) {
              if (iterator.next() != i) {
                return true;
              }
            }
            return false;
          }
        });

        if (playedTiles.score() == perfectScore) {
          return perfectScore;
        }

        if (strikeCount == numberOfStrikesThatEndsTheGame) {
          return scoreAfterStrikeOut;
        }

        if (!tilesToBeDrawn.hasNext() && turnsSinceLastTileDrawn == numberOfPlayers) {
          return playedTiles.score();
        }

        if (currentPlayer == players.size() - 1) {
          currentPlayer = 0;
        } else {
          currentPlayer++;
        }
      }
    } catch (EndGameFromTestException ignore) {
      return gameEndedByTest;
    }
  }

  private void addEvent(Event event) {
    eventVisitors.forEach(event::visit);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("currentPlayer", currentPlayer)
        .add("strikeCount", strikeCount)
        .add("turnsSinceLastTileDrawn", turnsSinceLastTileDrawn)
        .toString();
  }
}
