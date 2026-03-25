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
public class WorkActionHandler implements ActionHandler {

  private final CitizenPort citizenService;

  @Override
  public String getActionType() {
    return "WORK";
  }

  @Override
  public ActionResult execute(UUID citizenId, ActionRequest request) {
    String workplaceType = (String) request.parameters().getOrDefault("workplaceType", "shop");

    citizenService.assignWorkTask(citizenId, workplaceType);

    return ActionResult.success("Started working at " + workplaceType, getActionType());
  }
}
