package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
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
    if (!citizenService.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_EXAMINE)) {
      return ActionResult.failure("You are too far from " + targetId + " to examine it closely.", getActionType());
    }

    String details = worldService.examineObject(targetId);
    citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_EXAMINE);

    return ActionResult.success("Examined " + targetId + ": " + details, getActionType());
  }
}
