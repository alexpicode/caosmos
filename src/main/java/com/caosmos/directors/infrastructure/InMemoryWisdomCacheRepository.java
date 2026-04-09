package com.caosmos.directors.infrastructure;

import com.caosmos.directors.domain.contracts.WisdomCacheRepository;
import com.caosmos.directors.domain.model.CacheKey;
import com.caosmos.directors.domain.model.WisdomEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryWisdomCacheRepository implements WisdomCacheRepository {

  private final Map<CacheKey, WisdomEntry> cache = new ConcurrentHashMap<>();

  @Override
  public Optional<WisdomEntry> find(CacheKey key) {
    return Optional.ofNullable(cache.get(key));
  }

  @Override
  public void store(WisdomEntry entry) {
    cache.put(entry.key(), entry);
  }

  @Override
  public void evict(CacheKey key) {
    cache.remove(key);
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public Map<CacheKey, WisdomEntry> getAll() {
    return Collections.unmodifiableMap(cache);
  }
}
