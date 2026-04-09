package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.actions.ActionRequest;
import com.caosmos.common.domain.model.actions.ActionResult;
import java.util.UUID;

public interface CreativeResolutionPort {

  ActionResult resolve(UUID citizenId, ActionRequest request);
}
