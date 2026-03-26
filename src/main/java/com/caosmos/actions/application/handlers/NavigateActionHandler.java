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
public class NavigateActionHandler implements ActionHandler {

  private final CitizenPort citizenService;
  private final WorldPort worldService;

  @Override
  public String getActionType() {
    return "NAVIGATE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    try {
      String targetId = (String) request.parameters().get("targetId");
      String direction = (String) request.parameters().get("direction");

      Vector3 target = null;

      if (targetId != null && !targetId.isBlank()) {
        target = worldService.getObjectPosition(targetId).orElse(null);
        if (target == null) {
          return ActionResult.failure("Cannot find target object in world.", getActionType());
        }
      } else if (direction != null && !direction.isBlank()) {
        Vector3 currentPos = citizenService.getPosition(citizenId);
        target = calculateDistantPos(currentPos, direction);
      } else {
        return ActionResult.failure("NAVIGATE requires either 'targetId' or 'direction'.", getActionType());
      }

      citizenService.assignNavigationTask(citizenId, target, targetId);

      log.debug(
          "Citizen {} started navigation to {} (via {}; target={})",
          citizenId, target, direction != null ? direction : "id", targetId
      );
      return ActionResult.success("Started navigation.", getActionType());
    } catch (Exception e) {
      log.error("Failed to start navigation for {}: {}", citizenId, e.getMessage());
      return ActionResult.failure("Navigation failed: " + e.getMessage(), getActionType());
    }
  }

  private Vector3 calculateDistantPos(Vector3 currentPos, String direction) {
    if (currentPos == null || direction == null) {
      return currentPos;
    }

    double distance = 50.0; // Far enough to allow continuous travel
    double x = currentPos.x();
    double y = currentPos.y();
    double z = currentPos.z();

    switch (direction.toUpperCase()) {
      case "NORTH" -> z += distance;
      case "SOUTH" -> z -= distance;
      case "EAST" -> x += distance;
      case "WEST" -> x -= distance;
      case "NORTHEAST" -> {
        x += distance;
        z += distance;
      }
      case "NORTHWEST" -> {
        x -= distance;
        z += distance;
      }
      case "SOUTHEAST" -> {
        x += distance;
        z -= distance;
      }
      case "SOUTHWEST" -> {
        x -= distance;
        z -= distance;
      }
      //case "UP" -> y += distance;
      //case "DOWN" -> y -= distance;
      default -> {
      }
    }
    return new Vector3(x, y, z);
  }
}
