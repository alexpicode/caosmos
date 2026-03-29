package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
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
public class ExploreActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "EXPLORE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    try {
      String direction = (String) request.parameters().get("direction");

      if (direction == null || direction.isBlank()) {
        return ActionResult.failure("EXPLORE requires 'direction'.", getActionType());
      }

      Vector3 directionVector = parseDirection(direction);
      if (directionVector == null) {
        return ActionResult.failure("Invalid direction: " + direction, getActionType());
      }

      citizenService.assignExploreTask(citizenId, directionVector);
      log.debug("Citizen {} started Explore navigation in direction {}", citizenId, direction);
      return ActionResult.success("Started continuous exploration in direction " + direction + ".", getActionType());

    } catch (Exception e) {
      log.error("Failed to start exploration for {}: {}", citizenId, e.getMessage());
      return ActionResult.failure("Exploration failed: " + e.getMessage(), getActionType());
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
