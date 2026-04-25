package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.EconomyPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.WorldConstants;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PickupActionHandler implements ActionHandler {

  private final WorldPort worldService;
  private final CitizenPort citizenService;
  private final EconomyPort economyService;

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

    if (!citizenService.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_PICKUP)) {
      return ActionResult.failure("You are too far from " + targetId + " to pick it up.", getActionType());
    }

    // Check if the object is static
    java.util.Set<String> tags = worldService.getObjectTags(targetId);
    if (tags != null && tags.contains(WorldConstants.TAG_STATIC)) {

      return ActionResult.failure("The object " + targetId + " is fixed and cannot be picked up.", getActionType());
    }

    // First remove from world to get item details
    ItemData item = worldService.removeObject(targetId);

    if (item != null) {
      if (item.tags().contains(WorldConstants.TAG_COIN_CONTAINER)) {

        double amount = item.amount() != null ? item.amount() : 0.0;
        economyService.addCoins(citizenId, amount);
        citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_PICKUP);
        return ActionResult.success("Picked up " + amount + " coins", getActionType());
      }

      boolean success = citizenService.addToInventory(citizenId, item);
      if (success) {
        citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_PICKUP);
        return ActionResult.success("Picked up " + item.name(), getActionType());
      } else {
        // Return to world if inventory full
        String currentZoneId = citizenService.getCurrentZoneId(citizenId);
        worldService.spawnObject(citizenService.getPosition(citizenId), currentZoneId, item);
        return ActionResult.failure("Inventory is full or could not pick up " + item.name(), getActionType());
      }
    } else {
      return ActionResult.failure("Item " + targetId + " not found in the world", getActionType());
    }
  }
}
