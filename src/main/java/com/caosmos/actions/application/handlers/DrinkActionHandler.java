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
public class DrinkActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "DRINK";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for DRINK", getActionType());
    }

    ItemData item = citizenService.removeFromInventory(citizenId, targetId);

    if (item != null) {
      citizenService.drink(citizenId, ActionThresholds.DRINK_HYDRATION_RECOVERY);
      citizenService.reduceStress(citizenId, ActionThresholds.DRINK_STRESS_REDUCTION);
      return ActionResult.success("Drank " + item.name(), getActionType());
    } else {
      return ActionResult.failure("Item " + targetId + " not found in inventory to drink", getActionType());
    }
  }
}
