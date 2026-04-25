package com.caosmos.citizens.application.usecases;

import com.caosmos.citizens.application.dto.CitizenCognitionDto;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCitizenCognitionUseCase {

  private final EntityTelemetryService telemetryService;

  public Collection<CitizenCognitionDto> execute(UUID uuid, Long sinceTick) {
    return telemetryService.getCognitionDelta(uuid, sinceTick).stream()
        .map(entry -> new CitizenCognitionDto(
            entry.entityId(),
            entry.tick(),
            entry.thoughtProcess(),
            entry.actionTarget()
        ))
        .toList();
  }
}
