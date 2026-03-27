package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.items.ItemData;
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

    // Check proximity
    if (!citizenService.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_PICKUP)) {
      return ActionResult.failure("You are too far from " + targetId + " to pick it up.", getActionType());
    }

    // First remove from world to get item details
    ItemData item = worldService.removeObject(targetId);

    if (item != null) {
      boolean success = citizenService.addToInventory(citizenId, item.id(), item.name(), item.tags());
      if (success) {
        citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_PICKUP);
        return ActionResult.success("Picked up " + item.name(), getActionType());
      } else {
        // Return to world if inventory full
        worldService.spawnObject(citizenService.getPosition(citizenId), item);
        return ActionResult.failure("Inventory is full or could not pick up " + item.name(), getActionType());
      }
    } else {
      return ActionResult.failure("Item " + targetId + " not found in the world", getActionType());
    }
  }
}
