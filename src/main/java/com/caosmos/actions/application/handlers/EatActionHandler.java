package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.items.ItemData;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EatActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "EAT";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for EAT", getActionType());
    }

    ItemData item = citizenService.removeFromInventory(citizenId, targetId);

    if (item != null) {
      citizenService.eat(citizenId, ActionThresholds.EAT_NUTRITION_RECOVERY);
      citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_EAT);
      return ActionResult.success("Ate " + item.name(), getActionType());
    } else {
      return ActionResult.failure("Item " + targetId + " not found in inventory to eat", getActionType());
    }
  }
}
