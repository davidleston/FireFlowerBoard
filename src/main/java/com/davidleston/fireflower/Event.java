package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

import java.util.function.Consumer;

public abstract class Event {
  public final int sourcePlayer;

  Event(int sourcePlayer) {
    this.sourcePlayer = sourcePlayer;
  }

  public abstract void visit(Visitor visitor);

  protected MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects
        .toStringHelper(this)
        .add("sourcePlayer", sourcePlayer);
  }

  public interface Visitor extends Consumer<Event> {
    void doColorHint(ColorHintEvent colorHintEvent);
    void doDiscard(DiscardEvent discardEvent);
    void doDraw(DrawEvent drawEvent);
    void doNumberHint(NumberHintEvent numberHintEvent);
    void doPlay(PlayEvent playEvent);
    void doReorder(ReorderEvent reorderEvent);
    default void accept(Event event) {
      event.visit(this);
    }
  }
}
