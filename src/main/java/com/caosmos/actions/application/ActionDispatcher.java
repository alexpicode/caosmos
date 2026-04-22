package com.caosmos.actions.application;

import com.caosmos.actions.domain.ActionHandler;
import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ActionDispatcher {

  private final Map<String, ActionHandler> handlers;

  public ActionDispatcher(List<ActionHandler> handlerList) {
    this.handlers = handlerList.stream().collect(Collectors.toMap(ActionHandler::getActionType, h -> h));
  }

  public ActionResult dispatch(UUID citizenId, ActionRequest request) {
    ActionHandler handler = handlers.get(request.type());
    if (handler != null) {
      return handler.execute(citizenId, request);
    }
    return ActionResult.failure(
        "Action '" + request.type() + "' is not yet implemented in this world.", request.type());
  }
}