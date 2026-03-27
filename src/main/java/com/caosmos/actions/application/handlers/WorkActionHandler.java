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

    // Get citizen assignment
    String assignedJob = citizenService.getJob(citizenId);
    String assignedWorkplaceTag = citizenService.getWorkplaceTag(citizenId);

    // 1. Verify Job Assignment
    if (assignedJob != null && !isJobCompatible(assignedJob, workplaceType)) {
      return ActionResult.failure(
          "You are employed as a " + assignedJob + ". You can only perform your assigned job.",
          getActionType()
      );
    }

    // 2. Verify Workplace Tag (Physical Location)
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

    // 3. Verify Specific Workplace (if assigned)
    if (assignedWorkplaceTag != null && !citizenService.isInZoneWithTag(citizenId, assignedWorkplaceTag)) {
      return ActionResult.failure(
          "You are specifically assigned to work at: " + assignedWorkplaceTag + ". You cannot work here.",
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
          "You need a tool with the tag '" + requiredToolTag + "' to work here.",
          getActionType()
      );
    }

    citizenService.assignWorkTask(citizenId, workplaceType);

    return ActionResult.success("Started working at " + workplaceType, getActionType());
  }

  private boolean isJobCompatible(String job, String workplaceType) {
    if (job == null || workplaceType == null) {
      return false;
    }
    String normalizedJob = job.toLowerCase();
    String normalizedType = workplaceType.toLowerCase();

    // Exact match
    if (normalizedJob.equals(normalizedType)) {
      return true;
    }

    // Common mappings
    if (normalizedJob.equals("miner") && normalizedType.equals("mine")) {
      return true;
    }
    if (normalizedJob.equals("blacksmith") && normalizedType.equals("blacksmith")) {
      return true;
    }
    if ((normalizedJob.equals("shopkeeper") || normalizedJob.equals("vendor") || normalizedJob.equals("clerk"))
        && normalizedType.equals("shop")) {
      return true;
    }

    return false;
  }
}
