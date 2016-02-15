package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HintEventTest {
  @Test(expected = HintMatchesNoTilesException.class)
  public void cannotProvideHintThatMatchesNoTiles() {
    new HintEvent(0, ImmutableSet.of(), new HintAction(1, 1));
  }

  @Test(expected = CannotHintSelfException.class)
  public void cannotHintSelf() {
    new HintEvent(0, ImmutableSet.of(0), new HintAction(0, 1));
  }

  @Test
  public void predicate() {
    HintEvent hint = new HintEvent(0, ImmutableSet.of(0), new HintAction(1, 1));
    assertThat(hint
        .test(new Tile(Color.values()[0], 1)))
        .isTrue();
    assertThat(hint
        .test(new Tile(Color.values()[0], 2)))
        .isFalse();
  }
}
