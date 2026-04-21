package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimPropertyActionHandler implements ActionHandler {

  private final WorldPort worldPort;
  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "CLAIM";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for CLAIM", getActionType());
    }

    // Check proximity
    if (!citizenService.isNear(citizenId, targetId, ActionThresholds.PROXIMITY_USE)) {
      return ActionResult.failure("You are too far from " + targetId + " to claim it.", getActionType());
    }

    WorldElement element = worldPort.getElement(targetId).orElse(null);

    if (element == null) {
      return ActionResult.failure("Property " + targetId + " not found.", getActionType());
    }

    Set<String> tags = element.getTags();

    if (EntityType.ZONE == element.getType()) {
      return handleZoneClaim(citizenId, targetId, tags);
    } else {
      return handleObjectClaim(citizenId, targetId, tags, element.getZoneId());
    }
  }

  private ActionResult handleZoneClaim(UUID citizenId, String zoneId, Set<String> tags) {
    if (!tags.contains(WorldConstants.TAG_UNOWNED)) {
      return ActionResult.failure("This property is already owned.", getActionType());
    }

    worldPort.removeTag(zoneId, WorldConstants.TAG_UNOWNED);
    worldPort.addTag(zoneId, WorldConstants.PREFIX_OWNER + citizenId.toString());

    log.info("Citizen {} claimed zone {}", citizenId, zoneId);
    return ActionResult.success("You have successfully claimed this property.", getActionType());
  }

  private ActionResult handleObjectClaim(UUID citizenId, String objectId, Set<String> tags, String zoneId) {
    if (!tags.contains(WorldConstants.TAG_WORKSTATION)) {
      return ActionResult.failure("This object is not a workstation.", getActionType());
    }

    // Check if already owned
    boolean alreadyOwned = tags.stream().anyMatch(t -> t.startsWith(WorldConstants.PREFIX_OWNER));
    if (alreadyOwned) {
      return ActionResult.failure("This workstation is already claimed by someone else.", getActionType());
    }

    worldPort.addTag(objectId, WorldConstants.PREFIX_OWNER + citizenId.toString());

    checkFullyStaffed(zoneId);

    log.info("Citizen {} claimed workstation {}", citizenId, objectId);
    return ActionResult.success("You have successfully claimed this workstation.", getActionType());
  }

  private void checkFullyStaffed(String zoneId) {
    if (zoneId == null) {
      return;
    }

    var elements = worldPort.getElementsInZone(zoneId);
    var workstations = elements.stream()
        .filter(e -> e.getTags().contains(WorldConstants.TAG_WORKSTATION))
        .toList();

    if (workstations.isEmpty()) {
      return;
    }

    boolean allClaimed = workstations.stream()
        .allMatch(e -> e.getTags().stream().anyMatch(t -> t.startsWith(WorldConstants.PREFIX_OWNER)));

    if (allClaimed) {
      worldPort.addTag(zoneId, WorldConstants.TAG_FULLY_STAFFED);
      log.info("Zone {} is now fully staffed", zoneId);
    }
  }
}
