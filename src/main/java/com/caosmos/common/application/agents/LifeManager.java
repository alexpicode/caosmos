package com.caosmos.common.application.agents;

import com.caosmos.common.domain.contracts.AgentPulse;

public interface LifeManager {

  void startLife(String agentId, AgentPulse mind);

  void stopLife(String agentId);
}