package com.davidleston.fireflower;

public abstract class Event {
  public final int sourcePlayer;

  Event(int sourcePlayer) {
    this.sourcePlayer = sourcePlayer;
  }

  public abstract void visit(Visitor visitor);

  public interface Visitor {
    void doColorHint(ColorHintEvent colorHintEvent);
    void doDiscard(DiscardEvent discardEvent);
    void doDraw(DrawEvent drawEvent);
    void doNumberHint(NumberHintEvent numberHintEvent);
    void doPlay(PlayEvent playEvent);
    void doReorder(ReorderEvent reorderEvent);
  }
}
