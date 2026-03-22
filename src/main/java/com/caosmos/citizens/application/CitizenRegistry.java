package com.caosmos.citizens.application;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.common.domain.contracts.WorldRegistry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CitizenRegistry {

  private final Map<UUID, Citizen> citizens = new ConcurrentHashMap<>();
  private final WorldRegistry spatialRegistry;

  public void register(UUID id, Citizen citizen) {
    citizens.put(id, citizen);
    spatialRegistry.register(citizen);
  }

  public Optional<Citizen> get(UUID id) {
    return Optional.ofNullable(citizens.get(id));
  }

  public List<Citizen> getAll() {
    return new java.util.ArrayList<>(citizens.values());
  }
}
