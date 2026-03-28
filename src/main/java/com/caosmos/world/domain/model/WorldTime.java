package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.WorldDate;
import lombok.Getter;

@Getter
public class WorldTime {

  private int day;
  private int hour;
  private int minute;

  public WorldTime(int startDay, int startHour) {
    this.day = startDay;
    this.hour = startHour;
    this.minute = 0;
  }

  public double advanceByTick(double speedMultiplier) {
    double worldDeltaTimeInSeconds = 1.0 * speedMultiplier;
    addSeconds((long) worldDeltaTimeInSeconds);
    return worldDeltaTimeInSeconds;
  }

  private void addSeconds(long seconds) {
    long totalMinutes = minute + seconds / 60;
    minute = (int) (totalMinutes % 60);

    long totalHours = hour + totalMinutes / 60;
    hour = (int) (totalHours % 24);

    day += totalHours / 24;
  }

  public String getTimeString() {
    return String.format("%02d:%02d", hour, minute);
  }

  public WorldDate getWorldDate() {
    return new WorldDate(day, getTimeString());
  }

  public int getCurrentHour() {
    return hour;
  }
}
