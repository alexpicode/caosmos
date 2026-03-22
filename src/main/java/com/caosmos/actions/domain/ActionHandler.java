package com.caosmos.actions.domain;

import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;

public interface ActionHandler {

  String getActionType();

  ActionResult execute(UUID citizenId, ActionRequest request);
}