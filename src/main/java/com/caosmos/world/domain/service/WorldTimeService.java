package com.caosmos.world.domain.service;

import com.caosmos.common.domain.contracts.SimulationClock;
import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.world.domain.contracts.WorldTimeSettings;
import com.caosmos.world.domain.model.WorldTime;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorldTimeService implements SimulationClock {

  private final WorldTimeSettings config;
  private WorldTime worldTime;
  private long currentTick;
  private double lastDeltaTime = 0.0; // In seconds

  @PostConstruct
  public void initialize() {
    this.worldTime = new WorldTime(config.getStartDay(), config.getStartHour());
  }

  @Override
  public void update(long tick) {
    this.currentTick = tick;
    this.lastDeltaTime = worldTime.advanceByTick(config.getTimeScale());
  }

  @Override
  public long getCurrentTick() {
    return currentTick;
  }

  public WorldDate getWorldDate() {
    return worldTime.getWorldDate();
  }

  public int getCurrentHour() {
    return worldTime.getCurrentHour();
  }

  @Override
  public double getDeltaTime() {
    return lastDeltaTime;
  }

  public double getTotalSeconds() {
    return worldTime.getTotalSeconds();
  }

}
