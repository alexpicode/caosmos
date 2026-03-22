package com.caosmos.actions.infrastructure;

import com.caosmos.actions.application.ActionDispatcher;
import com.caosmos.common.domain.contracts.ActionPort;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ActionAdapter implements ActionPort {

  private final ActionDispatcher actionDispatcher;

  public ActionAdapter(ActionDispatcher actionDispatcher) {
    this.actionDispatcher = actionDispatcher;
  }

  @Override
  public ActionResult dispatch(UUID citizenId, ActionRequest request) {
    return actionDispatcher.dispatch(citizenId, request);
  }
}
