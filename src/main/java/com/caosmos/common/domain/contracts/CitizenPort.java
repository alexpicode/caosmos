package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Set;
import java.util.UUID;

public interface CitizenPort {

  Vector3 getPosition(UUID citizenId);

  void updatePosition(UUID citizenId, Vector3 newPos);

  String getCurrentZoneId(UUID citizenId);

  void enterZone(UUID citizenId, String zoneId, String zoneName);

  void consumeEnergy(UUID citizenId, double amount);

  boolean addToInventory(UUID citizenId, ItemData item);

  ItemData removeFromInventory(UUID citizenId, String itemId);

  boolean equipItem(UUID citizenId, String itemId, String hand);

  boolean unequipItem(UUID citizenId, String hand);

  void eat(UUID citizenId, double nutrition);

  void drink(UUID citizenId, double hydration);

  void sleep(UUID citizenId);

  void applyStress(UUID citizenId, double amount);

  void reduceStress(UUID citizenId, double amount);

  void increaseHunger(UUID citizenId, double amount);

  boolean isInSafeZone(UUID citizenId);

  void assignSleepTask(UUID citizenId);

  void assignWorkTask(UUID citizenId, String workplaceType);

  void assignWaitTask(UUID citizenId, boolean inSafeZone);

  void assignRestTask(UUID citizenId);

  void assignTravelToTask(UUID citizenId, Vector3 target, String targetId);

  void assignExploreTask(UUID citizenId, Vector3 direction, String targetCategory, String reason);

  void assignConversationTask(UUID citizenId, String targetId, Vector3 targetPosition);

  void continueTask(UUID citizenId);

  boolean isNear(UUID citizenId, String targetId, double maxDistance);

  boolean isItemEquippedWithTag(UUID citizenId, String tag);

  Set<String> getEquippedItemTags(UUID citizenId, String itemId);

  boolean isItemEquipped(UUID citizenId, String itemId);

  boolean isInZoneWithTag(UUID citizenId, String tag);

  String getJob(UUID citizenId);

  String getWorkplaceTag(UUID citizenId);

  String getName(UUID citizenId);

  void initiateOrJoinConversation(String citizenId, String citizenName, String targetId, String targetName, long tick);

  void registerDialogue(String speakerId, String speakerName, String targetId, String message, String tone, long tick);
}