package com.caosmos.citizens.application.core;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Ordered, deduplicated buffer of unprocessed events. Eliminates the repeated
 * {@code if (!list.contains(e)) list.add(e)} pattern.
 */
public class EventBuffer {

  private final LinkedHashSet<String> events = new LinkedHashSet<>();

  public void add(String event) {
    events.add(event);
  }

  public void addAll(List<String> newEvents) {
    events.addAll(newEvents);
  }

  /**
   * Returns an immutable snapshot of the current events in insertion order.
   */
  public List<String> snapshot() {
    return List.copyOf(events);
  }

  public void clear() {
    events.clear();
  }
}
