package com.caosmos.citizens.domain.model.perception;

import java.util.Map;

public record LastAction(
    String type,
    String status,
    String reasoningWas,
    String resultMessage,
    Map<String, Object> parameters
) {

  public LastAction withStatus(String newStatus) {
    return new LastAction(type, newStatus, reasoningWas, resultMessage, parameters);
  }

}
