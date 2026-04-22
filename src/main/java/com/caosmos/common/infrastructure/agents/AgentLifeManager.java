package com.caosmos.common.infrastructure.agents;

import com.caosmos.common.application.agents.LifeManager;
import com.caosmos.common.domain.contracts.AgentPulse;
import com.caosmos.common.domain.service.core.MasterClock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentLifeManager implements LifeManager {

  private final MasterClock clock;
  private final Map<String, AgentHeartbeat> activeHeartbeats = new ConcurrentHashMap<>();

  @Value("${caosmos.citizen.pulse-frequency}")
  private int pulseFrequency;

  @Override
  public void startLife(String id, AgentPulse mind) {
    AgentHeartbeat heartbeat = new AgentHeartbeat(id, clock, mind, pulseFrequency);
    activeHeartbeats.put(id, heartbeat);

    Thread.ofVirtual()
          .name("agent-heartbeat-" + id)
          .start(heartbeat);
  }

  @Override
  public void stopLife(String id) {
//    Optional.ofNullable(activeHeartbeats.remove(id))
//        .ifPresent(AgentHeartbeat::stop);
  }
}
