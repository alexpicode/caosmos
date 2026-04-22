package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TravelToActionHandler implements ActionHandler {

  private final CitizenPort citizenService;
  private final WorldPort worldService;

  @Override
  public String getActionType() {
    return "TRAVEL_TO";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    try {
      String targetId = (String) request.parameters().get("targetId");

      if (targetId == null || targetId.isBlank()) {
        return ActionResult.failure("TRAVEL_TO requires 'targetId'.", getActionType());
      }

      Vector3 target = worldService.getObjectPosition(targetId).orElse(null);
      if (target == null) {
        return ActionResult.failure("Cannot find target object in world: " + targetId, getActionType());
      }

      citizenService.assignTravelToTask(citizenId, target, targetId);
      log.debug("Citizen {} started TravelTo navigation to {} (target={})", citizenId, target, targetId);
      return ActionResult.success("Started continuous travel to " + targetId + ".", getActionType());

    } catch (Exception e) {
      log.error("Failed to start travel for {}: {}", citizenId, e.getMessage());
      return ActionResult.failure("Travel failed: " + e.getMessage(), getActionType());
    }
  }
}
