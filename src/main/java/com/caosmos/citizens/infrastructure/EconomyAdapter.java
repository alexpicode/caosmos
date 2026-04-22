package com.caosmos.citizens.infrastructure;

import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.common.domain.contracts.EconomyPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EconomyAdapter implements EconomyPort {

  private final CitizenRegistry citizenRegistry;

  @Override
  public double getCoins(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.economy().getCoins())
        .orElse(0.0);
  }

  @Override
  public void addCoins(UUID citizenId, double amount) {
    log.debug("Adding {} coins to citizen {}", amount, citizenId);
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.economy().addCoins(amount));
  }

  @Override
  public boolean subtractCoins(UUID citizenId, double amount) {
    log.debug("Subtracting {} coins from citizen {}", amount, citizenId);
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.economy().subtractCoins(amount))
        .orElse(false);
  }
}
