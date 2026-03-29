package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.perception.Status;
import lombok.Data;

/**
 * Manages biological and metabolic state of a citizen. All values are clamped to the [0, 100] range.
 */
@Data
public class BiologyManager {

  private static final double MIN = 0.0;
  private static final double MAX = 100.0;

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
    this.vitality = Math.clamp(this.vitality - amount, MIN, MAX);
  }

  public void increaseVitality(double amount) {
    this.vitality = Math.clamp(this.vitality + amount, MIN, MAX);
  }

  public void increaseHunger(double amount) {
    this.hunger = Math.clamp(this.hunger + amount, MIN, MAX);
  }

  public void decreaseHunger(double amount) {
    this.hunger = Math.clamp(this.hunger - amount, MIN, MAX);
  }

  public void decreaseEnergy(double amount) {
    this.energy = Math.clamp(this.energy - amount, MIN, MAX);
  }

  public void increaseEnergy(double amount) {
    this.energy = Math.clamp(this.energy + amount, MIN, MAX);
  }

  public void increaseStress(double amount) {
    this.stress = Math.clamp(this.stress + amount, MIN, MAX);
  }

  public void decreaseStress(double amount) {
    this.stress = Math.clamp(this.stress - amount, MIN, MAX);
  }

  /**
   * Processes nutrition from eating. Decreases hunger and partially recovers energy.
   */
  public void processNutrition(double nutrition) {
    decreaseHunger(nutrition);
    increaseEnergy(nutrition / 2.0);
  }

  /**
   * Processes hydration from drinking. Decreases stress and partially restores vitality.
   */
  public void processHydration(double hydration) {
    decreaseStress(hydration);
    increaseVitality(hydration / 2.0);
  }

  /**
   * Full energy recharge from sleeping.
   */
  public void fullRecharge() {
    increaseEnergy(MAX);
  }

  public Status getStatus() {
    return new Status(vitality, hunger, energy, stress);
  }
}
