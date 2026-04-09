package com.caosmos.directors.domain.contracts;

import com.caosmos.common.domain.model.actions.ResolutionResult;
import com.caosmos.directors.domain.model.ArbitrationRequest;

public interface ArbitrationProvider {

  ResolutionResult arbitrate(ArbitrationRequest request);
}
