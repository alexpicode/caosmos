package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
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

    boolean hasItem = citizenService.removeFromInventory(citizenId, targetId);

    if (hasItem) {
      citizenService.drink(citizenId, 20.0);
      citizenService.reduceStress(citizenId, 2.0);
      return ActionResult.success("Drank " + targetId, getActionType());
    } else {
      return ActionResult.failure("Item " + targetId + " not found in inventory to drink", getActionType());
    }
  }
}
