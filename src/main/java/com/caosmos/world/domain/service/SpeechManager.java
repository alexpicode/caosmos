package com.caosmos.world.domain.service;

import com.caosmos.common.domain.contracts.TemporalElementManager;
import com.caosmos.common.domain.model.world.SpeechElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechManager implements TemporalElementManager {

  private final SpatialHash spatialHash;

  @Value("${caosmos.world.speech-ttl-ticks:2}")
  private int ttlTicks;

  // index by expiration tick: TreeMap<ExpirationTick, List<SpeechId>>
  private final Map<Long, List<String>> expirationIndex = new ConcurrentHashMap<>();
  // reverse index for early consumption
  private final Map<String, Long> speechToExpirationTick = new ConcurrentHashMap<>();

  public void register(SpeechElement speech) {
    long expirationTick = speech.getStartTick() + ttlTicks;

    spatialHash.register(speech);

    expirationIndex.computeIfAbsent(expirationTick, k -> new ArrayList<>()).add(speech.getId());
    speechToExpirationTick.put(speech.getId(), expirationTick);

    log.debug(
        "Registered speech {} from {} (expiring at tick {})",
        speech.getId(), speech.getSourceId(), expirationTick
    );
  }

  public void consumeEarly(String speechId) {
    Long expirationTick = speechToExpirationTick.remove(speechId);
    if (expirationTick != null) {
      List<String> ids = expirationIndex.get(expirationTick);
      if (ids != null) {
        ids.remove(speechId);
      }
      spatialHash.remove(speechId);
      log.debug("Early consumed speech {}", speechId);
    }
  }

  @Override
  public void cleanup(long currentTick) {
    // Collect all ticks that are due for cleanup
    List<Long> pastTicks = expirationIndex.keySet().stream()
        .filter(tick -> tick <= currentTick)
        .toList();

    for (Long tick : pastTicks) {
      List<String> ids = expirationIndex.remove(tick);
      if (ids != null) {
        for (String id : ids) {
          spatialHash.remove(id);
          speechToExpirationTick.remove(id);
        }
        log.debug("Cleaned up {} speech elements for tick {}", ids.size(), tick);
      }
    }
  }
}
