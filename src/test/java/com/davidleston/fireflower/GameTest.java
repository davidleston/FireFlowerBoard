package com.davidleston.fireflower;

import com.google.common.collect.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.davidleston.stream.GuavaCollectors.immutableSet;
import static org.assertj.core.api.Assertions.assertThat;

public final class GameTest {

  private final Player nullPlayer = Player.create(events -> null, events -> ImmutableSet.of());

  private final Function<Stream<Event>, ImmutableSet<Integer>> failOnReceiveHint = events -> {
    throw new RuntimeException();
  };

  private final ImmutableSet<Integer> unorderedPositions = IntStream
      .range(0, Game.handSizeForThreeOrFewerPlayers).boxed()
      .collect(immutableSet());
  private final Function<Stream<Event>, ImmutableSet<Integer>> doNotReorderOnHint = events -> unorderedPositions;


  private PeekingIterator<Tile> allTilesInOrder;

  @Before
  public final void resetTileIterator() {
    allTilesInOrder = Iterators.peekingIterator(TileTest.distinctTiles().iterator());
  }

  @Test
  public void OnFirstTurn_PlayerIsToldOfOtherPlayersFirstDraws() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              Iterator<Event> eventIterator = events.iterator();
              IntStream.range(0, Game.handSizeForThreeOrFewerPlayers)
                  .forEachOrdered(i -> {
                    DrawEvent drawEvent = (DrawEvent) eventIterator.next();
                    assertThat(drawEvent.tile)
                        .isEqualTo(new Tile(Color.values()[1], i + 1));
                  });
              assertThat(eventIterator)
                  .isEmpty();
              throw new EndGameFromTestException();
            },
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void canPlayOneAsFirstMove() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(4, playEvent -> {
              assertThat(playEvent.tile.number)
                  .isEqualTo(1);
              assertThat(playEvent.wasSuccessful)
                  .isTrue();
              throw new EndGameFromTestException();
            }),
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void whenPlayerOneProvidesHintToPlayerTwo_PlayerTwoReceivesTheHint() {
    Tile firstTileInPlayerOneHand = allTilesInOrder.next();

    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, firstTileInPlayerOneHand.number + 1),
            events -> {
              Event lastEvent = Iterators.getLast(events.iterator());
              HintEvent hint = (HintEvent) lastEvent;
              assertThat(hint.sourcePlayer)
                  .isEqualTo(1);
              assertThat(hint.playerReceivingHint)
                  .isEqualTo(0);
              hint.handleHint(
                  null,
                  number -> assertThat(number)
                      .isEqualTo(firstTileInPlayerOneHand.number)
              );
              assertThat(hint.hintedPositions)
                  .containsExactly(0);
              throw new EndGameFromTestException();
            }
        ),
        Player.create(
            events -> Action.hint(0, firstTileInPlayerOneHand.number),
            doNotReorderOnHint
        )
    );

    Game.newGame(allTilesInOrder, players);
  }

  // TODO reorder after Discard
  @Test
  public void reorderAfterPlayReflectedWhenDiscarding() {
    AtomicReference<Tile> peekedTile = new AtomicReference<>();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              if (peekedTile.get() == null) {
                peekedTile.set(allTilesInOrder.peek());
                return Action.play(
                    Game.handSizeForThreeOrFewerPlayers - 1,
                    event -> ImmutableSet.of(4, 0, 1, 2, 3)
                );
              }
              return Action.discard(1, event -> {
                assertThat(event.tile)
                    .isEqualTo(peekedTile.get());
                throw new EndGameFromTestException();
              });
            },
            doNotReorderOnHint
        ),
        Player.create(
            events -> Action.hint(0, Color.values()[0]),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void turnOrder() {
    List<Integer> turnOrder = new ArrayList<>();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              turnOrder.add(0);
              return Action.hint(1, 1);
            },
            doNotReorderOnHint
        ),
        Player.create(
            events -> {
              if (turnOrder.size() == 3) {
                throw new EndGameFromTestException();
              }
              turnOrder.add(1);
              return Action.hint(0, 1);
            },
            doNotReorderOnHint
        )
    );
    Game.newGame(allTilesInOrder, players);
    assertThat(turnOrder)
        .containsExactly(0, 1, 0);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void cannotHintNonExistentPlayer() {
    Tile firstTile = allTilesInOrder.peek();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(events -> Action.hint(2, firstTile.color), failOnReceiveHint),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void cannotDiscardNonExistentTile() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, 1),
            failOnReceiveHint
        ),
        Player.create(
            events -> Action.discard(5, null),
            doNotReorderOnHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void cannotPlayNonExistentTile() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(5, null),
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void threeIncorrectPlaysEndsGameWithZeroScore() {
    AtomicInteger turnCounter = new AtomicInteger(0);
    Player player = Player.create(
        events -> {
          turnCounter.incrementAndGet();
          return Action.play(1, playEvent -> {
            assertThat(playEvent.wasSuccessful)
                .isFalse();
            return unorderedPositions;
          });
        },
        failOnReceiveHint
    );
    ImmutableList<Player> players = ImmutableList.of(
        player,
        player
    );
    int score = Game.newGame(allTilesInOrder, players);
    assertThat(turnCounter.get())
        .isEqualTo(3);
    assertThat(score)
        .isEqualTo(0);
  }

  // TODO: test reorder after turn when no tiles were drawn cause there were no tiles left to draw

  // Todo: validate Game.newGame for valid args

  @Test(expected = InvalidCollectionOfPositionsException.class)
  public void reorderMustContainAllPositions() {
    reorder(IntStream
        .range(0, Game.handSizeForThreeOrFewerPlayers - 1)
    );
  }

  @Test(expected = InvalidCollectionOfPositionsException.class)
  public void reorderCannotContainInvalidPositions() {
    reorder(IntStream
        .range(0, Game.handSizeForThreeOrFewerPlayers)
        .map(position -> ++position)
    );
  }

  @Test(expected = InvalidCollectionOfPositionsException.class)
  public void reorderCannotContainExtraPositions() {
    reorder(IntStream
        .range(0, Game.handSizeForThreeOrFewerPlayers + 1)
    );
  }

  public void reorder(IntStream range) {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(0, event -> ImmutableSet.copyOf(range.iterator())),
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void reorderAfterHintReflectedViaDiscard() {
    AtomicReference<Tile> drawnTile = new AtomicReference<>();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              if (drawnTile.get() == null) {
                Tile toBeDrawnNext = allTilesInOrder.peek();
                drawnTile.set(toBeDrawnNext);
                return Action.play(0, null);
              }
              return Action.discard(1, event -> {
                assertThat(event.tile)
                    .isEqualTo(drawnTile.get());
                throw new EndGameFromTestException();
              });
            },
            events -> ImmutableSet.of(4, 0, 1, 2, 3)
        ),
        Player.create(
            events -> Action.hint(0, drawnTile.get().color),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  // TODO: event order enforcement with reordering

  @Test
  public void startGameWithTileSet() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            eventStream -> {
              throw new EndGameFromTestException();
            },
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(TileSet.WithRainbow, 0, players);
    Game.newGame(TileSet.WithoutRainbow, 0, players);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testToStringsForCodeCoverage() {
    Tile tile = new Tile(Color.values()[0], 1);
    PlayEvent playEvent = new PlayEvent(0, 0, tile, true);
    HintAction colorHint = new HintAction(1, Color.values()[0]);
    colorHint.toString();
    new HintEvent(0, unorderedPositions, colorHint).toString();
    new DiscardAction(0, null).toString();
    new DiscardEvent(0, 0, tile).toString();
    new DrawEvent(0, tile).toString();
    new EventQueueCollection(0).toString();
    new Game(allTilesInOrder, ImmutableList.of(nullPlayer, nullPlayer)).toString();
    new HandCollection(2, 5).toString();
    new HintCountEnforcer().toString();
    HintAction numberHint = new HintAction(1, 2);
    numberHint.toString();
    new HintEvent(0, unorderedPositions, numberHint).toString();
    new PlayAction(0, null).toString();
    new PlayedTiles().toString();
    playEvent.toString();
    new ReorderEvent(0, unorderedPositions).toString();
    tile.toString();
  }
}
