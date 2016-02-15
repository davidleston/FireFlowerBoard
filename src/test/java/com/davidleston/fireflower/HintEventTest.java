package com.davidleston.fireflower;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

public class HintEventTest {
  @Test(expected = HintMatchesNoTilesException.class)
  public void cannotProvideHintThatMatchesNoTiles() {
    new HintEvent(0, ImmutableSet.of(), new HintAction(1, 1));
  }

  @Test(expected = CannotHintSelfException.class)
  public void cannotHintSelf() {
    new HintEvent(0, ImmutableSet.of(0), new HintAction(0, 1));
  }
}
