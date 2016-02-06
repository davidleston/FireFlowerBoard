package com.davidleston.fireflower;

import javax.annotation.CheckReturnValue;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class EventQueueCollection {
  private final Stream.Builder<Event>[] eventsToBeSeen;

  EventQueueCollection(int numberOfPlayers) {
    //noinspection unchecked
    this.eventsToBeSeen = Stream
        .generate(Stream::<Event>builder)
        .limit(numberOfPlayers)
        .toArray(Stream.Builder[]::new);
  }

  @CheckReturnValue
  Stream<Event> eventsFor(int player) {
    Stream<Event> events = eventsToBeSeen[player].build();
    eventsToBeSeen[player] = Stream.builder();
    return events;
  }

  Event.Visitor eventVisitor() {
    return new Event.Visitor() {
      @Override
      public void doColorHint(ColorHintEvent colorHintEvent) {
        informOtherPlayers(colorHintEvent);
      }

      @Override
      public void doDiscard(DiscardEvent discardEvent) {
        informOtherPlayers(discardEvent);
      }

      @Override
      public void doDraw(DrawEvent drawEvent) {
        informOtherPlayers(drawEvent);
      }

      @Override
      public void doNumberHint(NumberHintEvent numberHintEvent) {
        informOtherPlayers(numberHintEvent);
      }

      @Override
      public void doPlay(PlayEvent playEvent) {
        informOtherPlayers(playEvent);
      }

      @Override
      public void doReorder(ReorderEvent reorderEvent) {
        informOtherPlayers(reorderEvent);
      }

      private void informOtherPlayers(Event event) {
        IntStream.range(0, eventsToBeSeen.length)
            .filter(i -> i != event.sourcePlayer)
            .forEach(i -> eventsToBeSeen[i].accept(event));
      }
    };
  }

  @Override
  public String toString() {
    return Arrays.toString(eventsToBeSeen);
  }
}
