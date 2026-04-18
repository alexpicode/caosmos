package com.caosmos.directors.infrastructure;

import com.caosmos.common.domain.contracts.CreativeObservationPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.directors.application.ObserverDirector;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreativeObservationAdapter implements CreativeObservationPort {

  private final ObserverDirector observerDirector;

  @Override
  public ActionResult observe(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");
    String description = observerDirector.orchestrateObservation(citizenId, targetId);
    return ActionResult.success(description, request.type());
  }
}
