package com.caosmos.common.domain.contracts;

public interface SimulationClock {

  void update(long tick);

  long getCurrentTick();
}
