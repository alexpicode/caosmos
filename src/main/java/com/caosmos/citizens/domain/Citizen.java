package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.CurrentState;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Pure domain entity representing a citizen's mental state and profile.
 */
@Slf4j
public class Citizen implements WorldEntity {

  @Getter
  private final UUID uuid;

  @Getter
  private final CitizenProfile citizenProfile;

  private final BiologyManager biologyManager;
  private final InventoryManager inventoryManager;

  @Getter
  private final Set<String> visitedZoneIds = new HashSet<>();

  @Getter
  @Setter
  private CurrentState currentState;

  public Citizen(UUID uuid, CitizenProfile citizenProfile) {
    this.uuid = uuid;
    this.citizenProfile = citizenProfile;
    this.biologyManager = new BiologyManager(citizenProfile.status());
    this.inventoryManager = new InventoryManager(20);

    // Initialize position from BaseLocation
    Vector3 initialPosition = null;
    if (citizenProfile.baseLocation() != null) {
      CitizenProfile.BaseLocation base = citizenProfile.baseLocation();
      initialPosition = new Vector3(base.x(), base.y(), base.z());
    }

    this.currentState = new CurrentState(initialPosition, null, null, CitizenState.IDLE, null, null);
  }

  // --- Manager Accessors ---

  public BiologyManager biology() {
    return biologyManager;
  }

  public InventoryManager inventory() {
    return inventoryManager;
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

  // --- WorldEntity Methods ---

  @Override
  public String getId() {
    return uuid.toString();
  }

  @Override
  public String getType() {
    return "CITIZEN";
  }

  @Override
  public String getDisplayName() {
    return citizenProfile.identity().name();
  }

  @Override
  public String getZoneId() {
    return currentState.getCurrentZoneId();
  }

  @Override
  public Map<String, Object> getProperties() {
    return Map.of(
        "tags", citizenProfile.identity().traits(),
        "activeTask",
        currentState.getActiveTask() != null
            ? Map.of("type", currentState.getActiveTask().type(), "goal", currentState.getActiveTask().goal())
            : Map.of()
    );
  }

  // --- State Transition Methods ---

  public void transitionTo(CitizenState newState, String reason) {
    log.debug(
        "[CITIZEN:{}] INTERNAL State transition: {} -> {} (Reason: {})",
        citizenProfile.identity().name(),
        currentState.getState(),
        newState,
        reason
    );
    currentState.setState(newState);
  }

  public void transitionTo(CitizenState newState, LastAction reasonAction) {
    log.info(
        "[CITIZEN:{}] State transition: {} -> {} (Result: {})",
        citizenProfile.identity().name(),
        currentState.getState(),
        newState,
        reasonAction.status()
    );
    currentState.setState(newState);
    currentState.setLastAction(reasonAction);
  }

  public void clearTask(CitizenState nextState, String reason) {
    log.info(
        "[CITIZEN:{}] Clearing task. Next state: {} (Reason: {})",
        citizenProfile.identity().name(),
        nextState,
        reason
    );
    currentState.setActiveTask(null);
    transitionTo(nextState, reason);
  }

  public void clearTask(CitizenState nextState, LastAction action) {
    log.info(
        "[CITIZEN:{}] Clearing task with action. Next state: {} (Action: {})",
        citizenProfile.identity().name(),
        nextState,
        action.type()
    );
    currentState.setActiveTask(null);
    transitionTo(nextState, action);
  }

  public void updateTask(ActiveTask activeTask) {
    currentState.setActiveTask(activeTask);
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
        currentState.getPosition()
    );
  }

  public boolean isZoneVisited(String zoneId) {
    return zoneId != null && visitedZoneIds.contains(zoneId);
  }

  public void enterZone(String zoneId, String zoneName) {
    if (zoneId != null) {
      this.currentState.setCurrentZoneId(zoneId);
      this.currentState.setCurrentZone(zoneName);
      this.visitedZoneIds.add(zoneId.toLowerCase());
    }
  }
}
