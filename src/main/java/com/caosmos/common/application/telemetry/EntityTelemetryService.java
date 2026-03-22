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

  public Collection<CognitionEntry> getCognitionHistory(UUID entityId, boolean clearAfter) {
    Queue<CognitionEntry> queue = cognitionBuffer.get(entityId);
    if (queue == null) {
      return Collections.emptyList();
    }

    List<CognitionEntry> snapshot = List.copyOf(queue);

    if (clearAfter) {
      // Clear is thread-safe on ConcurrentLinkedDeque but we might lose concurrent insertions between copyOf and clear.
      // Given it's a volatile telemetry endpoint, this is acceptable. Alternately, use poll().
      for (int i = 0; i < snapshot.size(); i++) {
        queue.poll();
      }
    }

    return snapshot;
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
