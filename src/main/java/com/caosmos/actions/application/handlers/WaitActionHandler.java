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
public class WaitActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "WAIT";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    boolean inSafeZone = citizenService.isInSafeZone(citizenId);
    citizenService.assignWaitTask(citizenId, inSafeZone);
    return ActionResult.success("Waiting...", getActionType());
  }
}
