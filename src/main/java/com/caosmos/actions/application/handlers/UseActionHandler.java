package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.CreativeResolutionPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.service.SanityChecker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UseActionHandler implements ActionHandler {

  private final WorldPort worldPort;
  private final CitizenPort citizenPort;
  private final SanityChecker sanityChecker;
  private final CreativeResolutionPort creativeResolutionPort;

  @Override
  public String getActionType() {
    return "USE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    // 1. Resolve Target parameters
    String targetId = (String) request.parameters().get("targetId");
    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for USE", getActionType());
    }

    // 2. Tool Validation (Citizen State Rule)
    String toolRef = (String) request.parameters().get("tool");
    if (toolRef != null) {
      var toolTags = citizenPort.getTagsByToolReference(citizenId, toolRef);
      if (toolTags.isEmpty()) {
        if (citizenPort.isItemInInventory(citizenId, toolRef)) {
          return ActionResult.failure(
              "ERROR: Tool '" + toolRef + "' is in your inventory but not equipped. You must EQUIP it before using it.",
              getActionType()
          );
        }
        var equipped = citizenPort.getEquippedItemsNames(citizenId);
        return ActionResult.failure(
            "The specified tool reference '" + toolRef + "' did not match any equipped item. " +
                "Currently equipped: " + (equipped.isEmpty() ? "nothing" : String.join(", ", equipped)),
            getActionType()
        );
      }
    }

    // 3. Physical Validation (Gatekeeping)
    ActionIntent intent = new ActionIntent(
        citizenId,
        getActionType(),
        targetId,
        null, null, null, // Context tags Resolved by the Adapter/Director
        citizenPort.getPosition(citizenId),
        worldPort.getObjectPosition(targetId).orElse(null)
    );

    var validationError = sanityChecker.validate(intent, citizenPort, worldPort);
    if (validationError.isPresent()) {
      return ActionResult.failure(validationError.get(), getActionType());
    }

    // 4. Resource Consumption (Physical effort)
    citizenPort.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_USE);

    // 5. Delegate Creative Resolution
    return creativeResolutionPort.resolve(citizenId, request);
  }
}
