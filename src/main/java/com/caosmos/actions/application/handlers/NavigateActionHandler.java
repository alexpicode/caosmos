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

      if (targetId != null && !targetId.isBlank()) {
        Vector3 target = worldService.getObjectPosition(targetId).orElse(null);
        if (target == null) {
          return ActionResult.failure("Cannot find target object in world.", getActionType());
        }
        citizenService.assignTravelToTask(citizenId, target, targetId);
        log.debug("Citizen {} started TravelTo navigation to {} (target={})", citizenId, target, targetId);
        return ActionResult.success("Started TravelTo navigation.", getActionType());
      } else if (direction != null && !direction.isBlank()) {
        Vector3 directionVector = parseDirection(direction);
        if (directionVector == null) {
          return ActionResult.failure("Invalid direction: " + direction, getActionType());
        }
        citizenService.assignExploreTask(citizenId, directionVector);
        log.debug("Citizen {} started Explore navigation in direction {}", citizenId, direction);
        return ActionResult.success("Started Explore navigation.", getActionType());
      } else {
        return ActionResult.failure("NAVIGATE requires either 'targetId' or 'direction'.", getActionType());
      }
    } catch (Exception e) {
      log.error("Failed to start navigation for {}: {}", citizenId, e.getMessage());
      return ActionResult.failure("Navigation failed: " + e.getMessage(), getActionType());
    }
  }

  private Vector3 parseDirection(String direction) {
    if (direction == null) {
      return null;
    }

    return switch (direction.toUpperCase()) {
      case "NORTH" -> new Vector3(0, 0, 1);
      case "SOUTH" -> new Vector3(0, 0, -1);
      case "EAST" -> new Vector3(1, 0, 0);
      case "WEST" -> new Vector3(-1, 0, 0);
      case "NORTHEAST" -> new Vector3(1, 0, 1).normalize();
      case "NORTHWEST" -> new Vector3(-1, 0, 1).normalize();
      case "SOUTHEAST" -> new Vector3(1, 0, -1).normalize();
      case "SOUTHWEST" -> new Vector3(-1, 0, -1).normalize();
      default -> null;
    };
  }
}
