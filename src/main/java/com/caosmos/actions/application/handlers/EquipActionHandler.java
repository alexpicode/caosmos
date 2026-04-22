package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EquipActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "EQUIP";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");
    String hand = (String) request.parameters().get("hand");

    if (targetId == null || targetId.isBlank() || hand == null || hand.isBlank()) {
      return ActionResult.failure("targetId and hand are required for EQUIP", getActionType());
    }

    boolean success = citizenService.equipItem(citizenId, targetId, hand);

    if (success) {
      citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_EQUIP);
      return ActionResult.success("Equipped " + targetId + " in " + hand + " hand", getActionType());
    } else {
      return ActionResult.failure("Could not equip " + targetId + " in " + hand + " hand", getActionType());
    }
  }
}
