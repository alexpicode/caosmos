package com.caosmos.directors.application;

import com.caosmos.common.domain.model.actions.ResolutionResult;
import com.caosmos.directors.domain.contracts.WisdomCacheRepository;
import com.caosmos.directors.domain.model.CacheKey;
import com.caosmos.directors.domain.model.WisdomEntry;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WisdomCacheService {

  private final WisdomCacheRepository repository;

  public Optional<ResolutionResult> lookup(CacheKey key) {
    Optional<WisdomEntry> entryOpt = repository.find(key);
    if (entryOpt.isPresent()) {
      // Re-store the entry incrementing its HIT counter. This helps future
      // eviting policies (like LRU/LFU) know which physical interactions are common.
      WisdomEntry entry = entryOpt.get();
      WisdomEntry updated = new WisdomEntry(key, entry.result(), entry.createdAt(), entry.hitCount() + 1);
      repository.store(updated);
      return Optional.of(updated.result());
    }
    return Optional.empty();
  }

  public void store(CacheKey key, ResolutionResult result) {
    WisdomEntry entry = new WisdomEntry(key, result, Instant.now(), 0);
    repository.store(entry);
  }
}
