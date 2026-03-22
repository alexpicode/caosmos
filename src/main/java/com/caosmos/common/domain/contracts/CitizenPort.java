package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.UUID;

public interface CitizenPort {

  Vector3 getPosition(UUID citizenId);

  void updatePosition(UUID citizenId, Vector3 newPos);

  void consumeEnergy(UUID citizenId, int amount);

  boolean addToInventory(UUID citizenId, String itemId, String itemName, java.util.List<String> tags, int quantity);

  boolean removeFromInventory(UUID citizenId, String itemId);

  boolean equipItem(UUID citizenId, String itemId, String hand);

  boolean unequipItem(UUID citizenId, String hand);

  void eat(UUID citizenId, int nutrition);

  void drink(UUID citizenId, int hydration);

  void sleep(UUID citizenId);

  void assignNavigationTask(UUID citizenId, Vector3 target, String targetId);
}