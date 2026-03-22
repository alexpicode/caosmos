package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickupActionHandler implements ActionHandler {

  private final WorldPort worldService;
  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "PICKUP";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for PICKUP", getActionType());
    }

    // Mock object retrieval mapping since WorldPort doesn't return object details yet
    // In a real system we'd get the actual item from World
    boolean success = citizenService.addToInventory(citizenId, targetId, "Item " + targetId, List.of("item"), 1);

    if (success) {
      worldService.removeObject(targetId);
      citizenService.consumeEnergy(citizenId, 2);
      return ActionResult.success("Picked up " + targetId, getActionType());
    } else {
      return ActionResult.failure("Inventory is full or could not pick up " + targetId, getActionType());
    }
  }
}
