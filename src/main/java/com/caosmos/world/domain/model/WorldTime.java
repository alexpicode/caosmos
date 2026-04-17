package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.WorldDate;
import lombok.Getter;

@Getter
public class WorldTime {

  private int day;
  private int hour;
  private int minute;

  private double remainingSeconds = 0.0;

  public WorldTime(int startDay, int startHour) {
    this.day = startDay;
    this.hour = startHour;
    this.minute = 0;
  }

  public double advanceByTick(double timeScale) {
    double worldDeltaTimeInSeconds = 1.0 * timeScale;
    addSeconds(worldDeltaTimeInSeconds);
    return worldDeltaTimeInSeconds;
  }

  private void addSeconds(double seconds) {
    remainingSeconds += seconds;

    // Only advance logical time when we accumulate whole minutes
    if (remainingSeconds >= 60.0) {
      long totalMinutesToAdd = (long) (remainingSeconds / 60.0);
      remainingSeconds %= 60.0;

      long totalMinutes = minute + totalMinutesToAdd;
      minute = (int) (totalMinutes % 60);

      long totalHours = hour + totalMinutes / 60;
      hour = (int) (totalHours % 24);

      day += (int) (totalHours / 24);
    }
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
