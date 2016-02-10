package com.davidleston.fireflower;

import com.google.common.collect.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class GameTest {

  private final Player nullPlayer = Player.create(events -> null, events -> ImmutableSet.of());

  private final Function<Stream<Event>, Action> failOnTakeTurn = events -> {
    throw new RuntimeException();
  };

  private final Function<Stream<Event>, ImmutableSet<Integer>> failOnReceiveHint = events -> {
    throw new RuntimeException();
  };

  private final ImmutableSet<Integer> unorderedPositions = ImmutableSet.copyOf(IntStream.range(0, Game.handSizeForThreeOrFewerPlayers).iterator());
  private final Function<Stream<Event>, ImmutableSet<Integer>> doNotReorderOnHint = events -> unorderedPositions;


  private PeekingIterator<Tile> allTilesInOrder;

  @Before
  public final void resetTileIterator() {
    allTilesInOrder = Iterators.peekingIterator(new AbstractSequentialIterator<Tile>(new Tile(Color.values()[0], 1)) {
      @Override
      protected Tile computeNext(Tile tile) {
        if (tile.number < 5) {
          return new Tile(tile.color, tile.number + 1);
        }
        if (tile.color.ordinal() < Color.values().length - 1) {
          return new Tile(Color.values()[tile.color.ordinal() + 1], 1);
        }
        return null;
      }
    });
  }

  @Test
  public void OnFirstTurn_PlayerIsToldOfOtherPlayersFirstDraws() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              Iterator<Event> eventIterator = events.iterator();
              for (int i = 0; i < Game.handSizeForThreeOrFewerPlayers; i++) {
                DrawEvent drawEvent = (DrawEvent) eventIterator.next();
                assertThat(drawEvent.tile)
                    .isEqualTo(new Tile(Color.values()[1], i + 1));
              }
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
            events -> Action.play(0, playEvent -> {
              assertThat(playEvent.tile.color)
                  .isEqualTo(Color.values()[0]);
              assertThat(playEvent.tile.number)
                  .isEqualTo(1);
              assertThat(playEvent.wasSuccessful)
                  .isTrue();
              throw new EndGameFromTestException();
            }),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = CannotDiscardException.class)
  public void cannotDiscardWhenNoDiscardsAvailable() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.discard(0, null),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = HintMatchesNoTilesException.class)
  public void cannotProvideColorHintThatMatchesNoTiles() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, Color.values()[0]),
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = HintMatchesNoTilesException.class)
  public void cannotProvideNumberHintThatMatchesNoTiles() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, 6),
            failOnReceiveHint
        ),
        nullPlayer
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = CannotHintException.class)
  public void cannotColorHintWhenNoHintsAvailable() {
    Tile secondPlayerFirstTile = allTilesInOrder.next();
    cannotHintWhenNoHintsAvailable(
        Player.create(
            events -> Action.hint(1, secondPlayerFirstTile.color),
            failOnReceiveHint
        ),
        secondPlayerFirstTile
    );
  }

  @Test(expected = CannotHintException.class)
  public void cannotNumberHintWhenNoHintsAvailable() {
    Tile secondPlayerFirstTile = allTilesInOrder.next();
    cannotHintWhenNoHintsAvailable(
        Player.create(
            events -> Action.hint(1, secondPlayerFirstTile.number),
            failOnReceiveHint
        ),
        secondPlayerFirstTile
    );
  }

  private void cannotHintWhenNoHintsAvailable(Player player, Tile secondPlayerFirstTile) {
    Tile firstTile = allTilesInOrder.peek();
    Game.newGame(allTilesInOrder, ImmutableList.of(player, nullPlayer), ImmutableList.of(
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(1, secondPlayerFirstTile),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color),
        new ColorHintEvent(0, 1, ImmutableSet.of(), firstTile.color)
    ));
  }

  @Test
  public void whenPlayerOneProvidesColorHintToPlayerTwo_PlayerTwoReceivesTheHint() {
    Tile firstTileInPlayerTwoHand = allTilesInOrder.next();

    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, firstTileInPlayerTwoHand.color),
            failOnReceiveHint
        ),
        Player.create(
            failOnTakeTurn,
            events -> {
              Event lastEvent = Iterators.getLast(events.iterator());
              ColorHintEvent colorHint = (ColorHintEvent) lastEvent;
              assertThat(colorHint.sourcePlayer)
                  .isEqualTo(0);
              assertThat(colorHint.playerReceivingHint)
                  .isEqualTo(1);
              assertThat(colorHint.color)
                  .isEqualTo(firstTileInPlayerTwoHand.color);
              assertThat(colorHint.hintedPositions)
                  .containsExactly(0);
              throw new EndGameFromTestException();
            }
        )
    );

    startGameSpecifyingFirstTileForPlayerTwo(players, firstTileInPlayerTwoHand);
  }

  @Test
  public void whenPlayerOneProvidesNumberHintToPlayerTwo_PlayerTwoReceivesTheHint() {
    Tile firstTileInPlayerTwoHand = allTilesInOrder.next();

    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, firstTileInPlayerTwoHand.number),
            failOnReceiveHint
        ),
        Player.create(
            failOnTakeTurn,
            events -> {
              Event lastEvent = Iterators.getLast(events.iterator());
              NumberHintEvent numberHint = (NumberHintEvent) lastEvent;
              assertThat(numberHint.sourcePlayer)
                  .isEqualTo(0);
              assertThat(numberHint.playerReceivingHint)
                  .isEqualTo(1);
              assertThat(numberHint.number)
                  .isEqualTo(firstTileInPlayerTwoHand.number);
              assertThat(numberHint.hintedPositions)
                  .containsExactly(0);
              throw new EndGameFromTestException();
            }
        )
    );

    startGameSpecifyingFirstTileForPlayerTwo(players, firstTileInPlayerTwoHand);
  }

  private void startGameSpecifyingFirstTileForPlayerTwo(ImmutableList<Player> players, Tile firstTileInPlayerTwoHand) {
    Game.newGame(allTilesInOrder, players, ImmutableList.of(
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(1, firstTileInPlayerTwoHand),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next())
    ));
  }

  @Test(expected = CannotHintSelfException.class)
  public void cannotColorHintSelf() {
    Tile firstTileDrawn = allTilesInOrder.peek();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(0, firstTileDrawn.color),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = CannotHintSelfException.class)
  public void cannotNumberHintSelf() {
    Tile firstTileDrawn = allTilesInOrder.peek();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(0, firstTileDrawn.number),
            failOnReceiveHint
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
                return Action.play(0, null);
              }
              return Action.discard(Game.handSizeForThreeOrFewerPlayers - 1, event -> {
                assertThat(event.tileDiscarded)
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

  @Test(expected = Exception.class)
  public void cannotColorHintNonExistentPlayer() {
    Tile firstTile = allTilesInOrder.peek();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(events -> Action.hint(1, firstTile.color), failOnReceiveHint)
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = Exception.class)
  public void cannotNumberHintNonExistentPlayer() {
    Tile firstTile = allTilesInOrder.peek();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(events -> Action.hint(1, firstTile.number), failOnReceiveHint)
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test(expected = Exception.class)
  public void cannotDiscardNonExistentTile() {
    Tile firstTileInPlayerTwoHand = allTilesInOrder.peek();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.hint(1, firstTileInPlayerTwoHand.color),
            failOnReceiveHint
        ),
        Player.create(
            events -> Action.discard(5, null),
            failOnReceiveHint
        )
    );
    startGameSpecifyingFirstTileForPlayerTwo(players, firstTileInPlayerTwoHand);
  }

  @Test(expected = Exception.class)
  public void cannotPlayNonExistentTile() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(5, null),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void threeIncorrectPlaysEndsGameWithZeroScore() {
    AtomicInteger turnCounter = new AtomicInteger(0);
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              turnCounter.incrementAndGet();
              return Action.play(1, playEvent -> {
                assertThat(playEvent.wasSuccessful)
                    .isFalse();
                return unorderedPositions;
              });
            },
            failOnReceiveHint
        )
    );
    int score = Game.newGame(allTilesInOrder, players);
    assertThat(turnCounter.get())
        .isEqualTo(3);
    assertThat(score)
        .isEqualTo(0);
  }

  @Test
  public void successfullyPlayingFiveProvidesHintWhenHintCountIsBelowMax() {
    ImmutableList<Tile> tilesToPlay = ImmutableList.copyOf(Iterators.limit(allTilesInOrder, 4));
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(0, playEvent -> {
              assertThat(playEvent.wasSuccessful)
                  .isTrue();
              assertThat(playEvent.tile.number)
                  .isEqualTo(5);
              return unorderedPositions;
            }),
            events -> {
              throw new EndGameFromTestException();
            }
        ),
        Player.create(
            events -> Action.hint(0, ((DrawEvent) Iterators.getLast(
                events
                    .filter(event -> event instanceof DrawEvent)
                    .iterator()
            )).tile.color),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players, ImmutableList.of(
        new DrawEvent(0, tilesToPlay.get(0)),
        new DrawEvent(0, tilesToPlay.get(1)),
        new DrawEvent(0, tilesToPlay.get(2)),
        new DrawEvent(0, tilesToPlay.get(3)),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new PlayEvent(0, tilesToPlay.get(0), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(1), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(2), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(3), 0, true),
        new DrawEvent(0, allTilesInOrder.next())
    ));
  }

  @Test(expected = CannotDiscardException.class)
  public void successfullyPlayingFiveDoesNotProvideHintWhenHintCountIsAtMax() {
    ImmutableList<Tile> tilesToPlay = ImmutableList.copyOf(Iterators.limit(allTilesInOrder, 4));
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(0, playEvent -> {
              // sanity checks
              assertThat(playEvent.wasSuccessful)
                  .isTrue();
              assertThat(playEvent.tile.number)
                  .isEqualTo(5);
              return unorderedPositions;
            }),
            failOnReceiveHint
        ),
        Player.create(
            events -> Action.discard(0, null),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players, ImmutableList.of(
        new DrawEvent(0, tilesToPlay.get(0)),
        new DrawEvent(0, tilesToPlay.get(1)),
        new DrawEvent(0, tilesToPlay.get(2)),
        new DrawEvent(0, tilesToPlay.get(3)),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(0), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(1), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(2), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(3), 0, true),
        new DrawEvent(0, allTilesInOrder.next())
    ));

  }

  @Test(expected = CannotHintException.class)
  public void unsuccessfullyPlayingFiveDoesNotProvideHintWhenHintCountIsBelowMax() {
    ImmutableList<Tile> tilesToPlay = ImmutableList.copyOf(Iterators.limit(allTilesInOrder, 3));
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(1, playEvent -> {
              // sanity checks
              assertThat(playEvent.wasSuccessful)
                  .isFalse();
              assertThat(playEvent.tile.number)
                  .isEqualTo(5);
              return unorderedPositions;
            }),
            events -> {
              throw new EndGameFromTestException();
            }
        ),
        Player.create(
            events -> Action.hint(0, ((DrawEvent) Iterators.getLast(
                events
                    .filter(event -> event instanceof DrawEvent)
                    .iterator()
            )).tile.color),
            failOnReceiveHint
        )
    );
    Game.newGame(allTilesInOrder, players, ImmutableList.of(
        new DrawEvent(0, tilesToPlay.get(0)),
        new DrawEvent(0, tilesToPlay.get(1)),
        new DrawEvent(0, tilesToPlay.get(2)),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(0, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new DrawEvent(1, allTilesInOrder.next()),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new NumberHintEvent(0, 1, ImmutableSet.of(0), tilesToPlay.get(0).number),
        new PlayEvent(0, tilesToPlay.get(0), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(1), 0, true),
        new DrawEvent(0, allTilesInOrder.next()),
        new PlayEvent(0, tilesToPlay.get(2), 0, true),
        new DrawEvent(0, allTilesInOrder.next())
    ));
  }

  // TODO: test reorder after turn when no tiles were drawn cause there were no tiles left to draw

  @Test
  public void gameOverWhenEveryoneGetsOneMoreTurnAfterDrawingLastTile() {
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(0, null),
            failOnReceiveHint
        ),
        Player.create(
            events -> Action.play(0, null),
            failOnReceiveHint
        )
    );
    int score = Game.newGame(Iterators.limit(allTilesInOrder, Game.handSizeForThreeOrFewerPlayers * players.size()), players);
    assertThat(score)
        .isEqualTo(2);
  }

  @Test
  public void gameOverWhenPerfectGameIsPlayed() {
    List<Event> eventsInThePast = new ArrayList<>();
    for (int i = 0; i < 29; i++) {
      eventsInThePast.add(new DrawEvent(0, allTilesInOrder.peek()));
      eventsInThePast.add(new PlayEvent(0, allTilesInOrder.next(), 0, true));
    }
    eventsInThePast.add(new DrawEvent(0, allTilesInOrder.next()));

    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> Action.play(0, null),
            failOnReceiveHint
        )
    );
    resetTileIterator();
    int score = Game.newGame(allTilesInOrder, players, eventsInThePast);
    assertThat(score)
        .isEqualTo(30);
  }

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
        )
    );
    Game.newGame(allTilesInOrder, players);
  }

  @Test
  public void reorderReflectedViaDiscard() {
    AtomicReference<Tile> drawnTile = new AtomicReference<>();
    ImmutableList<Player> players = ImmutableList.of(
        Player.create(
            events -> {
              if (drawnTile.get() == null) {
                Tile toBeDrawnNext = allTilesInOrder.peek();
                drawnTile.set(toBeDrawnNext);
                return Action.play(0, null);
              }
              return Action.discard(0, event -> {
                assertThat(event.tileDiscarded)
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
    ImmutableList<Player> players = ImmutableList.of(Player.create(
        eventStream -> {
          throw new EndGameFromTestException();
        },
        failOnReceiveHint
    ));
    Game.newGame(TileSet.WithRainbow, 0, players);
    Game.newGame(TileSet.WithoutRainbow, 0, players);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  public void testToStringsForCodeCoverage() {
    Tile tile = new Tile(Color.values()[0], 1);
    PlayEvent playEvent = new PlayEvent(0, tile, 0, true);
    new ColorHintAction(0, Color.values()[0]).toString();
    new ColorHintEvent(0, 1, unorderedPositions, Color.values()[0]).toString();
    new DiscardAction(0, null).toString();
    new DiscardEvent(0, 0, tile).toString();
    new DrawEvent(0, tile).toString();
    new EventQueueCollection(0).toString();
    new Game(0, allTilesInOrder, ImmutableList.of()).toString();
    new HandCollection(2, 5).toString();
    new HintCountEnforcer().toString();
    new NumberHintAction(0, 1).toString();
    new NumberHintEvent(0, 1, unorderedPositions, 1).toString();
    new PlayAction(0, null).toString();
    new PlayedTiles().toString();
    playEvent.toString();
    new ReorderEvent(0, unorderedPositions).toString();
    tile.toString();
  }
}
