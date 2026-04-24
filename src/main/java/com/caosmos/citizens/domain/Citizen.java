package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.CurrentState;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.citizens.domain.model.perception.SpeechMessage;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Pure domain entity representing a citizen's mental state and profile.
 */
@Slf4j
public class Citizen implements WorldElement {

  private static final int MAX_RECENT_MESSAGES = 10;

  @Getter
  private final UUID uuid;

  @Getter
  private final CitizenProfile citizenProfile;

  private final BiologyManager biologyManager;
  private final InventoryManager inventoryManager;
  private final EconomyManager economyManager;
  private final ExplorationTracker explorationTracker;

  @Getter
  private CurrentState currentState;

  public Citizen(UUID uuid, CitizenProfile citizenProfile) {
    this.uuid = uuid;
    this.citizenProfile = citizenProfile;
    this.biologyManager = new BiologyManager(citizenProfile.status());
    this.inventoryManager = new InventoryManager(20);
    this.economyManager = new EconomyManager(citizenProfile.coins() != null ? citizenProfile.coins() : 0.0);
    this.explorationTracker = new ExplorationTracker();

    // Initialize position from BaseLocation
    Vector3 initialPosition = null;
    if (citizenProfile.baseLocation() != null) {
      CitizenProfile.BaseLocation base = citizenProfile.baseLocation();
      initialPosition = new Vector3(base.x(), base.y(), base.z());
    }

    this.currentState = new CurrentState(
        initialPosition,
        null,
        null,
        CitizenState.IDLE,
        null,
        null,
        null,
        new ArrayList<>()
    );
  }

  // --- Manager Accessors ---

  public BiologyManager biology() {
    return biologyManager;
  }

  public InventoryManager inventory() {
    return inventoryManager;
  }

  public EconomyManager economy() {
    return economyManager;
  }

  public ExplorationTracker exploration() {
    return explorationTracker;
  }

  // --- Convenience Methods ---

  public CitizenState getState() {
    return currentState.getState();
  }

  public Vector3 getPosition() {
    return currentState.getPosition();
  }

  public ActiveTask getActiveTask() {
    return currentState.getActiveTask();
  }

  public LastAction getLastAction() {
    return currentState.getLastAction();
  }

  // --- WorldElement Methods ---

  @Override
  public String getId() {
    return uuid.toString();
  }

  @Override
  public EntityType getType() {
    return EntityType.CITIZEN;
  }

  @Override
  public NearbyElement toNearbyElement(double distance, String direction) {
    return new NearbyElement(
        uuid.toString(),
        citizenProfile.identity().name(),
        getCategory(),
        EntityType.CITIZEN,
        null,
        Math.round(distance * 100.0) / 100.0,
        direction,
        getTags(),
        getZoneId(),
        null, null, null
    );
  }

  @Override
  public String getName() {
    return citizenProfile.identity().name();
  }

  @Override
  public String getZoneId() {
    return currentState.getCurrentZoneId();
  }

  @Override
  public String getCategory() {
    return "HUMAN";
  }

  @Override
  public Set<String> getTags() {
    Set<String> tags = new HashSet<>(citizenProfile.identity().traits());
    if (citizenProfile.identity().job() != null) {
      tags.add(citizenProfile.identity().job().toLowerCase());
    }
    return tags;
  }

  // --- State Transition Methods ---

  public synchronized void transitionTo(CitizenState newState, String reason) {
    log.debug(
        "[CITIZEN:{}] INTERNAL State transition: {} -> {} (Reason: {})",
        citizenProfile.identity().name(),
        currentState.getState(),
        newState,
        reason
    );
    currentState.setState(newState);
  }

  public synchronized void transitionTo(CitizenState newState, LastAction reasonAction) {
    log.debug(
        "[CITIZEN:{}] State transition: {} -> {} (Result: {})",
        citizenProfile.identity().name(),
        currentState.getState(),
        newState,
        reasonAction.status()
    );
    currentState.setState(newState);
    currentState.setLastAction(reasonAction);
  }

  public synchronized void clearTask(CitizenState nextState, String reason) {
    log.debug(
        "[CITIZEN:{}] Clearing task. Next state: {} (Reason: {})",
        citizenProfile.identity().name(),
        nextState,
        reason
    );
    currentState.setActiveTask(null);
    transitionTo(nextState, reason);
  }

  public synchronized void clearTask(CitizenState nextState, LastAction action) {
    log.debug(
        "[CITIZEN:{}] Clearing task with action. Next state: {} (Action: {})",
        citizenProfile.identity().name(),
        nextState,
        action.type()
    );
    currentState.setActiveTask(null);
    transitionTo(nextState, action);
  }

  public synchronized void updateTask(ActiveTask activeTask) {
    currentState.setActiveTask(activeTask);
  }

  public synchronized void updateMentalMap(MentalMap mentalMap) {
    currentState.setMentalMap(mentalMap);
  }

  public synchronized void updateRecentMessages(List<SpeechMessage> newMessages) {
    if (newMessages == null || newMessages.isEmpty()) {
      return;
    }

    List<SpeechMessage> current = currentState.getRecentMessages();
    if (current == null) {
      current = new ArrayList<>();
    }

    // Dedup and append new messages
    for (SpeechMessage msg : newMessages) {
      boolean exists = current.stream().anyMatch(m -> m.id().equals(msg.id()));
      if (!exists) {
        current.add(msg);
      }
    }

    // Limit to MAX_RECENT_MESSAGES (FIFO)
    if (current.size() > MAX_RECENT_MESSAGES) {
      current = new ArrayList<>(current.subList(current.size() - MAX_RECENT_MESSAGES, current.size()));
    }

    currentState.setRecentMessages(current);
  }

  public CitizenPerception getPerception() {
    return new CitizenPerception(
        citizenProfile.identity(),
        biologyManager.getStatus(),
        currentState.getState(),
        inventoryManager.getEquipment(),
        inventoryManager.getInventory(),
        currentState.getLastAction(),
        currentState.getActiveTask(),
        currentState.getPosition(),
        currentState.getMentalMap(),
        currentState.getRecentMessages(),
        economyManager.getCoins()
    );
  }

  public boolean isZoneVisited(String zoneId) {
    return explorationTracker.isZoneVisited(zoneId);
  }

  public Set<String> getVisitedZoneIds() {
    return explorationTracker.getVisitedZoneIds();
  }

  public synchronized void enterZone(String zoneId, String zoneName) {
    this.currentState.setCurrentZoneId(zoneId);
    this.currentState.setCurrentZone(zoneName);
  }
}
