package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DropActionHandler implements ActionHandler {

  private final WorldPort worldService;
  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "DROP";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for DROP", getActionType());
    }

    boolean success = citizenService.removeFromInventory(citizenId, targetId);

    if (success) {
      Vector3 citizenPos = citizenService.getPosition(citizenId);
      worldService.spawnObject(citizenPos, targetId); // Mock spawn using ID as template
      citizenService.consumeEnergy(citizenId, 1);
      return ActionResult.success("Dropped " + targetId, getActionType());
    } else {
      return ActionResult.failure("Item " + targetId + " not found in inventory", getActionType());
    }
  }
}
