package com.caosmos.citizens.application.usecases;

import com.caosmos.common.application.telemetry.CognitionEntry;
import com.caosmos.common.application.telemetry.EntityTelemetryService;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCitizenCognitionUseCase {

  private final EntityTelemetryService telemetryService;

  public Collection<CognitionEntry> execute(UUID uuid, Long sinceTick) {
    return telemetryService.getCognitionDelta(uuid, sinceTick);
  }
}
