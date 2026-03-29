package com.caosmos.citizens.application.usecases;

import com.caosmos.citizens.application.core.CitizenSettings;
import com.caosmos.citizens.application.dto.CitizenConfigDto;
import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.common.application.telemetry.BiometricsEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import com.caosmos.common.domain.contracts.SimulationClock;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCitizenDetailUseCase {

  private final CitizenRegistry citizenRegistry;
  private final EntityTelemetryService telemetryService;
  private final CitizenSettings citizenSettings;
  private final SimulationClock simulationClock;

  public Optional<CitizenDetailDto> execute(UUID uuid) {
    return citizenRegistry.get(uuid)
        .map(c -> {
          Collection<BiometricsEntry> biometrics = telemetryService.getBiometricsHistory(uuid, false);
          double realWorldWalkingSpeed = citizenSettings.getWalkingSpeed() * simulationClock.getDeltaTime();
          CitizenConfigDto config = new CitizenConfigDto(realWorldWalkingSpeed);
          return new CitizenDetailDto(
              config,
              c.getPerception(),
              c.getLastAction(),
              biometrics,
              c.getCitizenProfile().manifestId(),
              c.getVisitedZoneIds()
          );
        });
  }
}
