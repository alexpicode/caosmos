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
public class WorkActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "WORK";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String workplaceType = (String) request.parameters().getOrDefault("workplaceType", "shop");

    // Check if in correct zone
    String requiredTag =
        "mine".equalsIgnoreCase(workplaceType) ? ActionThresholds.TAG_MINING : ActionThresholds.TAG_COMMERCE;
    // Also allow FORGE for blacksmith
    if ("blacksmith".equalsIgnoreCase(workplaceType)) {
      requiredTag = ActionThresholds.TAG_FORGE;
    }

    if (!citizenService.isInZoneWithTag(citizenId, requiredTag)) {
      return ActionResult.failure(
          "You are not at an appropriate " + workplaceType + " workplace to start working.",
          getActionType()
      );
    }

    // Check for required tool
    String requiredToolTag = null;
    if ("mine".equalsIgnoreCase(workplaceType)) {
      requiredToolTag = ActionThresholds.ITEM_TAG_MINING;
    } else if ("blacksmith".equalsIgnoreCase(workplaceType)) {
      requiredToolTag = ActionThresholds.ITEM_TAG_CRAFTING;
    }

    if (requiredToolTag != null && !citizenService.isItemEquippedWithTag(citizenId, requiredToolTag)) {
      return ActionResult.failure(
          "You need a " + requiredToolTag + " tool equipped to work here.",
          getActionType()
      );
    }

    citizenService.assignWorkTask(citizenId, workplaceType);

    return ActionResult.success("Started working at " + workplaceType, getActionType());
  }
}
