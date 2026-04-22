package com.caosmos.common.application.telemetry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Service;

@Service
public class EntityTelemetryService {

  private static final int MAX_COGNITION_ENTRIES = 10;
  private static final int MAX_BIOMETRICS_ENTRIES = 10;

  private final Map<UUID, Queue<CognitionEntry>> cognitionBuffer = new ConcurrentHashMap<>();
  private final Map<UUID, Queue<BiometricsEntry>> biometricsBuffer = new ConcurrentHashMap<>();

  public void registerThought(CognitionEntry entry) {
    Queue<CognitionEntry> queue = cognitionBuffer.computeIfAbsent(
        entry.entityId(), k -> new ConcurrentLinkedDeque<>());

    queue.offer(entry);

    // Evict oldest if exceeding limit
    while (queue.size() > MAX_COGNITION_ENTRIES) {
      queue.poll();
    }
  }

  public void registerBiometrics(BiometricsEntry entry) {
    Queue<BiometricsEntry> queue = biometricsBuffer.computeIfAbsent(
        entry.entityId(), k -> new ConcurrentLinkedDeque<>());

    queue.offer(entry);

    while (queue.size() > MAX_BIOMETRICS_ENTRIES) {
      queue.poll();
    }
  }

  public Collection<CognitionEntry> getCognitionDelta(UUID entityId, Long sinceTick) {
    Queue<CognitionEntry> queue = cognitionBuffer.get(entityId);
    if (queue == null) {
      return Collections.emptyList();
    }

    // If no specific tick is requested, return the entire window (the last N)
    if (sinceTick == null) {
      return List.copyOf(queue);
    }

    // Return only new entries since the last tick seen by the client
    return queue.stream()
                .filter(entry -> entry.tick() > sinceTick)
                .toList();
  }

  public Collection<BiometricsEntry> getBiometricsHistory(UUID entityId, boolean clearAfter) {
    Queue<BiometricsEntry> queue = biometricsBuffer.get(entityId);
    if (queue == null) {
      return Collections.emptyList();
    }

    List<BiometricsEntry> snapshot = List.copyOf(queue);

    if (clearAfter) {
      for (int i = 0; i < snapshot.size(); i++) {
        queue.poll();
      }
    }

    return snapshot;
  }
}
