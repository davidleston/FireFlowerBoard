
package com.davidleston.fireflower;

import java.util.function.Consumer;

// abstract class instead of interface to disallow outsiders from making sub-classes
// and so visit doesn't have to be public
public abstract class Action {
  Action() {
  }

  abstract void handleAction(Consumer<PlayAction> doPlay, Consumer<DiscardAction> doDiscard, Consumer<HintAction> doHint);

  public static Action hint(int playerReceivingHint, Color color) {
    return new HintAction(playerReceivingHint, color);
  }

  public static Action hint(int playerReceivingHint, int number) {
    return new HintAction(playerReceivingHint, number);
  }

  public static Action discard(int position, ReorderAction<DiscardEvent> reorderAction) {
    return new DiscardAction(position, reorderAction);
  }

  public static Action play(int position, ReorderAction<PlayEvent> reorderAction) {
    return new PlayAction(position, reorderAction);
  }
}
