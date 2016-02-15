package com.davidleston.fireflower;

import com.google.common.base.MoreObjects;

import java.util.function.Consumer;

public abstract class Event {
  public final int sourcePlayer;

  Event(int sourcePlayer) {
    this.sourcePlayer = sourcePlayer;
  }

  public abstract void handleEvent(Operation operation);

  protected MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects
        .toStringHelper(this)
        .add("sourcePlayer", sourcePlayer);
  }

  public interface Operation extends Consumer<Event> {
    void doDiscard(DiscardEvent discardEvent);
    void doDraw(DrawEvent drawEvent);
    void doHint(HintEvent hintEvent);
    void doPlay(PlayEvent playEvent);
    void doReorder(ReorderEvent reorderEvent);
    default void accept(Event event) {
      event.handleEvent(this);
    }
    static Operation create(
        Consumer<DiscardEvent> doDiscard,
        Consumer<DrawEvent> doDraw,
        Consumer<HintEvent> doHint,
        Consumer<PlayEvent> doPlay,
        Consumer<ReorderEvent> doReorder) {
      return new Operation() {
        @Override
        public void doDiscard(DiscardEvent discardEvent) {
          doDiscard.accept(discardEvent);
        }

        @Override
        public void doDraw(DrawEvent drawEvent) {
          doDraw.accept(drawEvent);
        }

        @Override
        public void doHint(HintEvent hintEvent) {
          doHint.accept(hintEvent);
        }

        @Override
        public void doPlay(PlayEvent playEvent) {
          doPlay.accept(playEvent);
        }

        @Override
        public void doReorder(ReorderEvent reorderEvent) {
          doReorder.accept(reorderEvent);
        }
      };
    }
  }
}
