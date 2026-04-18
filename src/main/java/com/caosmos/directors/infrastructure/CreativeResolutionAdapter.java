package com.caosmos.directors.infrastructure;

import com.caosmos.common.domain.contracts.CreativeResolutionPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.directors.application.DirectorArbitrator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreativeResolutionAdapter implements CreativeResolutionPort {

  private final DirectorArbitrator directorArbitrator;

  @Override
  public ActionResult resolve(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");
    String toolRef = (String) request.parameters().get("tool");

    return directorArbitrator.resolveInteraction(citizenId, targetId, request.type(), toolRef);
  }
}
