package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.actions.domain.ActionThresholds;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.CreativeObservationPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import com.caosmos.common.domain.service.SanityChecker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamineActionHandler implements ActionHandler {

  private final WorldPort worldPort;
  private final CitizenPort citizenPort;
  private final SanityChecker sanityChecker;
  private final CreativeObservationPort creativeObservationPort;

  @Override
  public String getActionType() {
    return "EXAMINE";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    if (targetId == null || targetId.isBlank()) {
      return ActionResult.failure("Target ID is required for EXAMINE", getActionType());
    }

    // 1. Physical Validation (Gatekeeping)
    ActionIntent intent = new ActionIntent(
        citizenId,
        getActionType(),
        targetId,
        null, null, null, // Context tags Resolved by the Adapter/Director
        citizenPort.getPosition(citizenId),
        worldPort.getObjectPosition(targetId).orElse(null)
    );

    var validationError = sanityChecker.validate(intent, citizenPort, worldPort);
    if (validationError.isPresent()) {
      return ActionResult.failure(validationError.get(), getActionType());
    }

    // 2. Resource Consumption (Physical effort)
    citizenPort.consumeEnergy(citizenId, ActionThresholds.ENERGY_COST_EXAMINE);

    // 3. Delegate Creative Resolution
    return creativeObservationPort.observe(citizenId, request);
  }
}
