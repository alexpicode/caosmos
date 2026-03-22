package com.caosmos.common.domain.model.actions;

import java.util.Map;

public record ActionRequest(
    String type,
    Map<String, Object> parameters
) {

}