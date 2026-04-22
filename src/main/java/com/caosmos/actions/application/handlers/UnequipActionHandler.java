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
public class UnequipActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "UNEQUIP";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String hand = (String) request.parameters().get("hand");

    if (hand == null || hand.isBlank()) {
      return ActionResult.failure("hand is required for UNEQUIP", getActionType());
    }

    boolean success = citizenService.unequipItem(citizenId, hand);

    if (success) {
      citizenService.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_UNEQUIP);
      return ActionResult.success("Unequipped item from " + hand + " hand", getActionType());
    } else {
      return ActionResult.failure("Could not unequip item from " + hand + " hand", getActionType());
    }
  }
}
