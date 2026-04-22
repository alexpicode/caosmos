package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.agents.AgentAction;
import java.util.UUID;

public interface ThinkingProvider {

  AgentAction think(UUID entityId, String entityName, long tick, String systemPrompt, String userMessage);
}
