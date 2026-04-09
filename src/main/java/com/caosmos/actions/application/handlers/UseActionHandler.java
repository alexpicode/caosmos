package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CreativeResolutionPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UseActionHandler implements ActionHandler {

  private final CreativeResolutionPort creativeResolutionPort;

  @Override
  public String getActionType() {
    return "USE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    return creativeResolutionPort.resolve(citizenId, request);
  }
}
