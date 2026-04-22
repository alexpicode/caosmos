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

  public LastAction withType(String newType) {
    return new LastAction(newType, status, reasoningWas, resultMessage, parameters);
  }

  public LastAction withResultMessage(String newResultMessage) {
    return new LastAction(type, status, reasoningWas, newResultMessage, parameters);
  }

}
