package com.caosmos.common.domain.contracts;

import java.util.UUID;

public interface EconomyPort {

  double getCoins(UUID citizenId);

  void addCoins(UUID citizenId, double amount);

  /**
   * Subtracts coins from the citizen's balance.
   *
   * @return true if successful, false if insufficient funds.
   */
  boolean subtractCoins(UUID citizenId, double amount);
}
