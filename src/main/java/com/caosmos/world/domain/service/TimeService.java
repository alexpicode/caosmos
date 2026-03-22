package com.caosmos.world.domain.service;

import com.caosmos.common.domain.contracts.SimulationClock;
import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.world.domain.contracts.WorldTimeSettings;
import com.caosmos.world.domain.model.SimulationTime;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeService implements SimulationClock {

  private final WorldTimeSettings config;
  private SimulationTime simulationTime;

  @PostConstruct
  public void initialize() {
    this.simulationTime = new SimulationTime(config.getStartDay(), config.getStartHour());
  }

  public WorldDate getCurrentWorldDate() {
    updateSimulationTime();
    return new WorldDate(simulationTime.getDay(), simulationTime.getTimeString());
  }

  public int getCurrentHour() {
    updateSimulationTime();
    return simulationTime.getCurrentHour();
  }

  public int getCurrentDay() {
    updateSimulationTime();
    return simulationTime.getDay();
  }

  public String getTime() {
    updateSimulationTime();
    return simulationTime.getTimeString();
  }

  public String getDayPeriod() {
    int hour = getCurrentHour();
    if (hour >= 6 && hour <= 18) {
      return "day";
    }
    return "night";
  }

  @Override
  public long getCurrentTick() {
    return getCurrentDay() * 24000L;
  }

  private void updateSimulationTime() {
    simulationTime.advanceTime(config.getSpeed());
  }
}
