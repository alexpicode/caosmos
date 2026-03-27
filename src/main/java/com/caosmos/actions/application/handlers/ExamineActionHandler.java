package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamineActionHandler implements ActionHandler {

  private final WorldPort worldService;
  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "EXAMINE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for EXAMINE", getActionType());
    }

    // Check proximity
    if (!citizenService.isNear(citizenId, targetId, 4.0)) {
      return ActionResult.failure("You are too far from " + targetId + " to examine it closely.", getActionType());
    }

    String details = worldService.examineObject(targetId);
    citizenService.consumeEnergy(citizenId, 1);

    return ActionResult.success("Examined " + targetId + ": " + details, getActionType());
  }
}
