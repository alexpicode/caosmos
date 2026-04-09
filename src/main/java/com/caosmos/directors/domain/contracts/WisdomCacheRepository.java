package com.caosmos.directors.domain.contracts;

import com.caosmos.directors.domain.model.CacheKey;
import com.caosmos.directors.domain.model.WisdomEntry;
import java.util.Map;
import java.util.Optional;

public interface WisdomCacheRepository {

  Optional<WisdomEntry> find(CacheKey key);

  void store(WisdomEntry entry);

  void evict(CacheKey key);

  int size();

  Map<CacheKey, WisdomEntry> getAll();
}
