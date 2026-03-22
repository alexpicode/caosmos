package com.caosmos.world.application.usecases;

import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.world.application.dto.WorldEnvironmentResponse;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.TimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorldEnvironmentUseCase {

  private final TimeService timeService;
  private final EnvironmentService environmentService;

  public WorldEnvironmentResponse execute() {
    return new WorldEnvironmentResponse(
        new WorldDate(timeService.getCurrentDay(), timeService.getTime()),
        environmentService.getCurrentEnvironment()
    );
  }
}
