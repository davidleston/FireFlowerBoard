
package com.davidleston.fireflower;

// abstract class instead of interface to disallow outsiders from making sub-classes
// and so visit doesn't have to be public
public abstract class Action {
  Action() {
  }

  abstract void visit(Visitor visitor);

  public static Action hint(int playerReceivingHint, Color color) {
    return new ColorHintAction(playerReceivingHint, color);
  }

  public static Action hint(int playerReceivingHint, int number) {
    return new NumberHintAction(playerReceivingHint, number);
  }

  public static Action discard(int position, ReorderAction<DiscardEvent> reorderAction) {
    return new DiscardAction(position, reorderAction);
  }

  public static Action play(int position, ReorderAction<PlayEvent> reorderAction) {
    return new PlayAction(position, reorderAction);
  }

  interface Visitor {
    void doPlay(PlayAction playAction);
    void doDiscard(DiscardAction discardAction);
    void doColorHint(ColorHintAction colorHintAction);
    void doNumberHint(NumberHintAction numberHintAction);
  }
}
