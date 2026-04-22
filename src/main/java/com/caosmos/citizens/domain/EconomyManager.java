package com.caosmos.citizens.domain;

import lombok.Data;

/**
 * Manages the economic state of a citizen.
 */
@Data
public class EconomyManager {

  private double coins;

  public EconomyManager(double initialCoins) {
    this.coins = Math.max(0.0, initialCoins);
  }

  public void addCoins(double amount) {
    if (amount > 0) {
      this.coins += amount;
    }
  }

  /**
   * Subtracts coins from the balance.
   *
   * @return true if successful, false if insufficient funds.
   */
  public boolean subtractCoins(double amount) {
    if (amount <= 0) {
      return true;
    }
    if (this.coins >= amount) {
      this.coins -= amount;
      return true;
    }
    return false;
  }
}
