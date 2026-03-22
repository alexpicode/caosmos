package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunicateActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "COMMUNICATE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");
    String message = (String) request.parameters().get("message");

    if (targetId == null || targetId.isBlank() || message == null || message.isBlank()) {
      return ActionResult.failure("targetId and message are required for COMMUNICATE", getActionType());
    }

    citizenService.consumeEnergy(citizenId, 1);
    return ActionResult.success("Said to " + targetId + ": " + message, getActionType());
  }
}
