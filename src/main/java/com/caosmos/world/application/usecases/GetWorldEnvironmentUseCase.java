package com.caosmos.world.application.usecases;

import com.caosmos.world.application.dto.WorldEnvironmentResponse;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.WorldTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldEnvironmentUseCase {

  private final WorldTimeService worldTimeService;
  private final EnvironmentService environmentService;

  public WorldEnvironmentResponse execute() {
    return new WorldEnvironmentResponse(
        worldTimeService.getWorldDate(),
        environmentService.getCurrentEnvironment()
    );
  }
}
