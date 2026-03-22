package com.caosmos.common.domain.model.actions;

import java.util.Map;

public record ActionResult(
    boolean success,
    String message,
    String actionType,
    Map<String, Object> changes
) {

  public static ActionResult success(String msg, String type) {
    return new ActionResult(true, msg, type, Map.of());
  }

  public static ActionResult failure(String msg, String type) {
    return new ActionResult(false, msg, type, Map.of());
  }
}