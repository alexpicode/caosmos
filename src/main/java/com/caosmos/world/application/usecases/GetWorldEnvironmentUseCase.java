package com.caosmos.world.application.usecases;

import com.caosmos.world.application.dto.WorldEnvironmentResponseDto;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.WorldTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldEnvironmentUseCase {

  private final WorldTimeService worldTimeService;
  private final EnvironmentService environmentService;

  public WorldEnvironmentResponseDto execute() {
    return new WorldEnvironmentResponseDto(
        worldTimeService.getWorldDate(),
        environmentService.getCurrentEnvironment()
    );
  }
}
