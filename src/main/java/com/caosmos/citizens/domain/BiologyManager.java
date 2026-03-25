package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.perception.Status;
import java.util.function.BiConsumer;
import lombok.Data;

/**
 * Manages biological and metabolic state of a citizen
 */
@Data
public class BiologyManager {

  private double vitality;
  private double hunger;
  private double energy;
  private double stress;

  public BiologyManager(Status initialStatus) {
    this.vitality = initialStatus.vitality();
    this.hunger = initialStatus.hunger();
    this.energy = initialStatus.energy();
    this.stress = initialStatus.stress();
  }

  public void decreaseVitality(double amount) {
    this.vitality = Math.max(0, this.vitality - amount);
  }

  public void increaseVitality(double amount) {
    this.vitality = Math.min(100.0, this.vitality + amount);
  }

  public void increaseHunger(double amount) {
    this.hunger = Math.min(100.0, this.hunger + amount);
  }

  public void decreaseHunger(double amount) {
    this.hunger = Math.max(0, this.hunger - amount);
  }

  public void decreaseEnergy(double amount) {
    this.energy = Math.max(0, this.energy - amount);
  }

  public void increaseEnergy(double amount) {
    this.energy = Math.min(100.0, this.energy + amount);
  }

  public void increaseStress(double amount) {
    this.stress = Math.min(100.0, this.stress + amount);
  }

  public void decreaseStress(double amount) {
    this.stress = Math.max(0, this.stress - amount);
  }

  /**
   * Applies a rate (per hour) over a period of time (seconds).
   */
  public void applyRate(double ratePerHour, double deltaSeconds, BiConsumer<BiologyManager, Double> applier) {
    double variation = ratePerHour * (deltaSeconds / 3600.0);
    applier.accept(this, variation);
  }

  public Status getStatus() {
    return new Status(vitality, hunger, energy, stress);
  }
}
