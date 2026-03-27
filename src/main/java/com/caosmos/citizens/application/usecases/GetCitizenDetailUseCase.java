package com.caosmos.citizens.application.usecases;

import com.caosmos.citizens.application.dto.CitizenDetailDto;
import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.common.application.telemetry.BiometricsEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
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

  public Optional<CitizenDetailDto> execute(UUID uuid) {
    return citizenRegistry.get(uuid)
                          .map(c -> {
                            Collection<BiometricsEntry> biometrics = telemetryService.getBiometricsHistory(uuid, false);
                            return new CitizenDetailDto(
                                c.getPerception(),
                                c.getLastAction(),
                                biometrics,
                                c.getCitizenProfile().manifestId()
                            );
                          });
  }
}
