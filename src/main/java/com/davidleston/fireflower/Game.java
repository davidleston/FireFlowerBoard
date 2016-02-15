package com.davidleston.fireflower;

import com.davidleston.stream.GuavaCollectors;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.davidleston.stream.GuavaCollectors.immutableSet;
import static com.google.common.base.Preconditions.checkArgument;

public final class Game {
  public static final int handSizeForThreeOrFewerPlayers = 5;
  public static final int handSizeForFourOrMorePlayers = 4;
  public static final int numberOfHintTokens = 8;
  static final int gameEndedByTest = -1;

  private final HandCollection hands;
  private final Iterator<Tile> tilesToBeDrawn;
  private final Consumer<Event> eventVisitors;
  private final EventQueueCollection eventQueues;
  private final PlayedTiles playedTiles;
  private final int numberOfPlayers;
  private final int handSize;
  private final ImmutableList<Player> players;
  private final Iterator<Integer> nextPlayer;
  private final GameEndDetector gameEndDetector;
  private int currentPlayer;

  @VisibleForTesting
  Game(Iterator<Tile> tilesToBeDrawn, ImmutableList<Player> players) {
    this.numberOfPlayers = players.size();
    checkArgument(numberOfPlayers > 1, "A minimum of two players is required.");
    this.tilesToBeDrawn = tilesToBeDrawn;
    this.players = players;
    this.handSize = numberOfPlayers > 3 ? handSizeForFourOrMorePlayers : handSizeForThreeOrFewerPlayers;

    this.hands = new HandCollection(numberOfPlayers, handSize);
    this.eventQueues = new EventQueueCollection(numberOfPlayers);
    this.playedTiles = new PlayedTiles();
    this.gameEndDetector = new GameEndDetector(tilesToBeDrawn, playedTiles, numberOfPlayers);

    eventVisitors = this.hands.eventVisitor
        .andThen(this.eventQueues.eventVisitor)
        .andThen(new HintCountEnforcer())
        .andThen(gameEndDetector);
    nextPlayer = Iterators
        .cycle(IntStream
        .range(0, numberOfPlayers).boxed()
        .collect(GuavaCollectors.immutableList()));

    currentPlayer = nextPlayer.next();
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
    Game game = new Game(tilesToBeDrawn, players);
    game.drawFirstTiles();
    return game.start();
  }

  private void drawFirstTiles() {
    IntStream.range(0, numberOfPlayers)
        .forEachOrdered(playerIndex ->
            IntStream.range(0, handSize)
            .forEach(i -> addEvent(new DrawEvent(playerIndex, tilesToBeDrawn.next()))));
  }

  /**
   * @return score
   */
  private int start() {
    try {
      while (!gameEndDetector.isGameOver()) {
        Action action = players.get(currentPlayer).takeTurn(eventQueues.eventsFor(currentPlayer));
        action.handleAction(
            playAction -> {
              Tile playedTile = hands.get(currentPlayer, playAction.position);
              boolean wasSuccessful = playedTiles.play(playedTile);
              PlayEvent event = new PlayEvent(currentPlayer, playAction.position, playedTile, wasSuccessful);
              addEvent(event);
              draw();
              reorder(currentPlayer, playAction.reorderAction, event);
            },
            discardAction -> {
              Tile discardedTile = hands.get(currentPlayer, discardAction.position);
              DiscardEvent event = new DiscardEvent(currentPlayer, discardAction.position, discardedTile);
              addEvent(event);
              draw();
              reorder(currentPlayer, discardAction.reorderAction, event);
            },
            hintAction -> {
              ImmutableSet<Integer> hintedPositions = hands.positionsOfMatchingTiles(hintAction);
              addEvent(new HintEvent(currentPlayer, hintedPositions, hintAction));
              informHintedPlayer(hintAction.playerReceivingHint);
            });
        currentPlayer = nextPlayer.next();
      }
      return gameEndDetector.score();
    } catch (EndGameFromTestException ignore) {
      return gameEndedByTest;
    }
  }

  private void informHintedPlayer(int playerReceivingHint) {
    Stream<Event> eventsForHintedPlayer = eventQueues.eventsFor(playerReceivingHint);
    ImmutableSet<Integer> newPositionsFromHintedPlayer
        = players.get(playerReceivingHint).receiveHint(eventsForHintedPlayer);
    reorder(playerReceivingHint, newPositionsFromHintedPlayer);
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
    ImmutableSet<Integer> expectedPositions = IntStream.range(0, handSize).boxed().collect(immutableSet());
    if (!expectedPositions.equals(newPositions)) {
      throw new InvalidCollectionOfPositionsException(expectedPositions, newPositions);
    }
  }

  private boolean isReordering(ImmutableSet<Integer> newPositions) {
    return !Ordering.natural().isOrdered(newPositions);
  }

  private void addEvent(Event event) {
    eventVisitors.accept(event);
  }

  @Override
  public String toString() {
    return MoreObjects
        .toStringHelper(this)
        .add("currentPlayer", currentPlayer)
        .add("gameEndDetector", gameEndDetector)
        .toString();
  }
}
