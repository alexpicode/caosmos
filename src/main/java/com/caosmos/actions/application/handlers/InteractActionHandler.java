package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.GatewayTransition;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InteractActionHandler implements ActionHandler {

  private final WorldPort worldService;
  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "INTERACT";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for INTERACT", getActionType());
    }

    // Check proximity
    if (!citizenService.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_USE)) {
      return ActionResult.failure("You are too far from " + targetId + " to interact with it.", getActionType());
    }

    Optional<WorldElement> objOpt = worldService.getObject(targetId);
    if (objOpt.isEmpty()) {
      return ActionResult.failure("Object doesn't exist.", getActionType());
    }

    WorldElement obj = objOpt.get();

    // Base interaction (triggers visual effects, usage generic, etc.)
    worldService.interactWithObject(targetId);
    citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_USE);

    // GATEWAY LOGIC (Switching Zone)
    String currentZoneId = citizenService.getCurrentZoneId(citizenId);
    Optional<GatewayTransition> transitionOpt = worldService.getGatewayTransition(targetId, currentZoneId);

    if (transitionOpt.isPresent()) {
      String destZoneId = transitionOpt.get().destinationZoneId();
      String destName = worldService.getZoneName(destZoneId);
      citizenService.enterZone(citizenId, destZoneId, destName);
      return ActionResult.success(
          "You interacted with " + obj.getName() + " and entered " + destName,
          getActionType()
      );
    }

    return ActionResult.success("Interacted with " + obj.getName(), getActionType());
  }
}
