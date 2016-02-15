package com.davidleston.fireflower;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class EventQueueCollection {
  private final Stream.Builder<Event>[] eventsToBeSeen;
  final Event.Operation eventVisitor = Event.Operation.create(
      this::informOtherPlayers,
      this::informOtherPlayers,
      this::informOtherPlayers,
      this::informOtherPlayers,
      this::informOtherPlayers);

  EventQueueCollection(int numberOfPlayers) {
    //noinspection unchecked
    this.eventsToBeSeen = Stream
        .generate(Stream::<Event>builder)
        .limit(numberOfPlayers)
        .toArray(Stream.Builder[]::new);
  }

  Stream<Event> eventsFor(int player) {
    Stream<Event> events = eventsToBeSeen[player].build();
    eventsToBeSeen[player] = Stream.builder();
    return events;
  }

  private void informOtherPlayers(Event event) {
    IntStream.range(0, eventsToBeSeen.length)
        .filter(i -> i != event.sourcePlayer)
        .forEach(i -> eventsToBeSeen[i].accept(event));
  }

  @Override
  public String toString() {
    return Arrays.toString(eventsToBeSeen);
  }
}
