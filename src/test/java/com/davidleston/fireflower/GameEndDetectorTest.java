package com.davidleston.fireflower;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public final class GameEndDetectorTest {
  private final Tile aTile = new Tile(Color.values()[0], 1);

  @Test
  public void gameOverWhenPerfectGameIsPlayed() {
    PlayedTiles playedTiles = new PlayedTiles();
    TileTest.distinctTiles()
        .forEachOrdered(playedTiles::play);
    GameEndDetector gameEndDetector = new GameEndDetector(ImmutableList.<Tile>of().iterator(), playedTiles, 1);

    assertThat(gameEndDetector.isGameOver())
        .isTrue();
    assertThat(gameEndDetector.score())
        .isEqualTo(30);
  }

  @Test
  public void gameOverWhenTurnsRunOutFromDiscarding() {
    gameOverWhenTurnsRunOut(new DiscardEvent(0, 0, aTile));
  }

  @Test
  public void gameOverWhenTurnsRunOutFromHinting() {
    gameOverWhenTurnsRunOut(new HintEvent(0, ImmutableSet.of(0), new HintAction(1, 1)));
  }

  @Test
  public void gameOverWhenTurnsRunOutFromPlaying() {
    gameOverWhenTurnsRunOut(new PlayEvent(0, 0, aTile, true));
  }

  private void gameOverWhenTurnsRunOut(Event event) {
    PlayedTiles playedTiles = new PlayedTiles();
    GameEndDetector gameEndDetector = new GameEndDetector(ImmutableList.<Tile>of().iterator(), playedTiles, 1);
    assertThat(gameEndDetector.isGameOver())
        .isFalse();
    event.handleEvent(gameEndDetector);
    assertThat(gameEndDetector.isGameOver())
        .isTrue();
    assertThat(gameEndDetector.score())
        .isEqualTo(0);
  }
}
