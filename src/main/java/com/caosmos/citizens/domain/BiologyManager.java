package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.perception.Status;
import lombok.Data;

/**
 * Manages biological and metabolic state of a citizen
 */
@Data
public class BiologyManager {

  private int vitality;
  private int hunger;
  private int energy;
  private int stress;

  public BiologyManager(Status initialStatus) {
    this.vitality = initialStatus.vitality();
    this.hunger = initialStatus.hunger();
    this.energy = initialStatus.energy();
    this.stress = initialStatus.stress();
  }

  public void decreaseVitality(int amount) {
    this.vitality -= amount;
    if (this.vitality < 0) {
      this.vitality = 0;
    }
  }

  public void increaseVitality(int amount) {
    this.vitality += amount;
    if (this.vitality > 100) {
      this.vitality = 100;
    }
  }

  public void increaseHunger(int amount) {
    this.hunger += amount;
    if (this.hunger > 100) {
      this.hunger = 100;
    }
  }

  public void decreaseHunger(int amount) {
    this.hunger -= amount;
    if (this.hunger < 0) {
      this.hunger = 0;
    }
  }

  public void decreaseEnergy(int amount) {
    this.energy -= amount;
    if (this.energy < 0) {
      this.energy = 0;
    }
  }

  public void increaseEnergy(int amount) {
    this.energy += amount;
    if (this.energy > 100) {
      this.energy = 100;
    }
  }

  public void increaseStress(int amount) {
    this.stress += amount;
    if (this.stress > 100) {
      this.stress = 100;
    }
  }

  public void decreaseStress(int amount) {
    this.stress -= amount;
    if (this.stress < 0) {
      this.stress = 0;
    }
  }

  public Status getStatus() {
    return new Status(vitality, hunger, energy, stress);
  }
}
