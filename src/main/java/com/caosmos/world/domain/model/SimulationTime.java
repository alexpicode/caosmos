package com.caosmos.world.domain.model;

import lombok.Data;

@Data
public class SimulationTime {

  private int day;
  private int hour;
  private int minute;
  private long lastRealTimeMillis;

  public SimulationTime(int startDay, int startHour) {
    this.day = startDay;
    this.hour = startHour;
    this.minute = 0;
    this.lastRealTimeMillis = System.currentTimeMillis();
  }

  public void advanceTime(double speedMultiplier) {
    long currentTimeMillis = System.currentTimeMillis();
    long elapsedRealMillis = currentTimeMillis - lastRealTimeMillis;
    double elapsedSimulationSeconds = (elapsedRealMillis / 1000.0) * speedMultiplier;

    addSeconds((long) elapsedSimulationSeconds);
    lastRealTimeMillis = currentTimeMillis;
  }

  private void addSeconds(long seconds) {
    long totalMinutes = minute + seconds / 60;
    minute = (int) (totalMinutes % 60);

    long totalHours = hour + totalMinutes / 60;
    hour = (int) (totalHours % 24);

    day += totalHours / 24;
  }

  public void addMinutes(int minutes) {
    addSeconds(minutes * 60L);
  }

  public void addHours(int hours) {
    addSeconds(hours * 3600L);
  }

  public void addDays(int days) {
    this.day += days;
  }

  public String getTimeString() {
    return String.format("%02d:%02d", hour, minute);
  }

  public int getCurrentHour() {
    return hour;
  }
}
