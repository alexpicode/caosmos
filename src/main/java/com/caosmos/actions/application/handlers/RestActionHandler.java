package com.caosmos.actions.application.handlers;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RestActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "REST";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    try {
      citizenService.assignRestTask(citizenId);
      log.debug("Citizen {} started Resting/Relaxing", citizenId);
      return ActionResult.success("Started resting and relaxing.", getActionType());

    } catch (Exception e) {
      log.error("Failed to start rest for {}: {}", citizenId, e.getMessage());
      return ActionResult.failure("Rest failed: " + e.getMessage(), getActionType());
    }
  }
}
