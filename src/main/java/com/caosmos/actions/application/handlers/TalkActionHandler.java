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
public class TalkActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "TALK";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String targetId = (String) request.parameters().get("targetId");

    // Socializing reduces stress
    citizenService.reduceStress(citizenId, 5.0);
    citizenService.consumeEnergy(citizenId, 0.2);

    return ActionResult.success("Talking with " + (targetId != null ? targetId : "someone"), getActionType());
  }
}
