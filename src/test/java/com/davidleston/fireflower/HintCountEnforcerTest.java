package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

public final class HintCountEnforcerTest {
  private HintCountEnforcer hintCountEnforcer;

  @Before
  public void before() {
    hintCountEnforcer = new HintCountEnforcer();
  }

  @Test(expected = CannotDiscardException.class)
  public void cannotDiscardWhenNoDiscardsAvailable() {
    discard();
  }

  @Test(expected = CannotHintException.class)
  public void cannotHintWhenNoHintsAvailable() {
    consumeHints(Game.numberOfHintTokens + 1);
  }

  @Test(expected = CannotHintException.class)
  public void successfullyPlayingFiveDoesNotProvideHintWhenHintCountIsAtMax() {
    play5(true);
    consumeHints(Game.numberOfHintTokens + 1);
  }

  @Test
  public void successfullyPlayingFiveProvidesHintWhenHintCountIsBelowMax() {
    consumeHints(Game.numberOfHintTokens);
    play5(true);
    consumeHints(1);
  }

  @Test(expected = CannotHintException.class)
  public void unsuccessfullyPlayingFiveDoesNotProvideHintWhenHintCountIsBelowMax() {
    consumeHints(1);
    play5(false);
    consumeHints(Game.numberOfHintTokens);
  }


  private void play5(boolean wasSuccessful) {
    hintCountEnforcer.doPlay(new PlayEvent(0, 0, new Tile(Color.values()[0], 5), wasSuccessful));
  }

  private void discard() {
    hintCountEnforcer.doDiscard(new DiscardEvent(0, 0, new Tile(Color.values()[0], 1)));
  }

  private void consumeHints(int numberOfHintsToConsume) {
    IntStream.range(0, numberOfHintsToConsume)
        .forEach(i -> hintCountEnforcer.doHint(new HintEvent(0, ImmutableSet.of(1), new HintAction(1, 1))));
  }
}
