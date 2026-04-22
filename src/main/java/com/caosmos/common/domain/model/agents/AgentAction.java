package com.caosmos.common.domain.model.agents;

import java.util.Map;

public record AgentAction(
    String type,
    String reasoning,
    Map<String, Object> params
) {

}