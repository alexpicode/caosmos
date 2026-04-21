package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.WorldConstants;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CraftActionHandler implements ActionHandler {

  private final WorldPort worldPort;
  private final CitizenPort citizenPort;

  @Override
  public String getActionType() {
    return "CRAFT";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String resultItemType = (String) request.parameters().getOrDefault("itemType", "iron_sword");
    String workstationId = (String) request.parameters().get("workstationId");

    if (workstationId == null || workstationId.isBlank()) {
      return ActionResult.failure("Workstation ID is required for CRAFT", getActionType());
    }

    // Check proximity to workstation
    if (!citizenPort.isNear(citizenId, workstationId, ActionThresholds.PROXIMITY_USE)) {
      return ActionResult.failure("You are too far from the workstation to craft.", getActionType());
    }

    // Check workstation ownership/worker status
    Set<String> tags = worldPort.getObjectTags(workstationId);
    if (!tags.contains(WorldConstants.TAG_WORKSTATION)) {
      return ActionResult.failure("Target is not a valid workstation.", getActionType());
    }

    String ownerTag = WorldConstants.PREFIX_OWNER + citizenId.toString();
    if (!tags.contains(ownerTag)) {
      return ActionResult.failure("You are not the registered worker of this workstation.", getActionType());
    }

    // Simplified crafting: Just spawn the item
    // TODO In a real scenario, this would consume materials from inventory

    ItemData newItem = new ItemData(
        resultItemType + "_" + UUID.randomUUID().toString().substring(0, 8),
        resultItemType.replace("_", " "),
        new HashSet<>(Set.of("crafted")),
        "EQUIPMENT",
        "A freshly crafted " + resultItemType,
        0.1, null, null, 1.0
    );

    String currentZoneId = citizenPort.getCurrentZoneId(citizenId);
    worldPort.spawnObject(citizenPort.getPosition(citizenId), currentZoneId, newItem);

    citizenPort.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_USE);

    log.info("Citizen {} crafted {} at workstation {}", citizenId, resultItemType, workstationId);
    return ActionResult.success("You have successfully crafted a " + newItem.name(), getActionType());
  }
}
