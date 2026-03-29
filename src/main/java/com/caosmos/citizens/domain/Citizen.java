package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.CitizenProfile;
import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.citizens.domain.model.perception.ActiveTask;
import com.caosmos.citizens.domain.model.perception.CitizenPerception;
import com.caosmos.citizens.domain.model.perception.CurrentState;
import com.caosmos.citizens.domain.model.perception.Equipment;
import com.caosmos.citizens.domain.model.perception.EquippedItem;
import com.caosmos.citizens.domain.model.perception.Inventory;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.citizens.domain.model.perception.Status;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import java.util.HashMap;
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
    Map<String, Object> props = new HashMap<>();
    props.put("tags", citizenProfile.identity().traits());
    if (currentState.getActiveTask() != null) {
      props.put(
          "activeTask",
          Map.of("type", currentState.getActiveTask().type(), "goal", currentState.getActiveTask().goal())
      );
    }
    return props;
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

    // Get status from biology manager (no longer needs inventory capacity)
    Status status = biologyManager.getStatus();

    // Get equipment and inventory from inventory manager (inventory now includes
    // capacity)
    Equipment equipment = inventoryManager.getEquipment();
    Inventory inventory = inventoryManager.getInventory();

    return new CitizenPerception(
        citizenProfile.identity(),
        status,
        currentState.getState(),
        equipment,
        inventory,
        currentState.getLastAction(),
        currentState.getActiveTask(),
        currentState.getPosition()
    );
  }

  public void consumeEnergy(double amount) {
    biologyManager.decreaseEnergy(amount);
  }

  public void decayVitality(double amount) {
    biologyManager.decreaseVitality(amount);
  }

  public void increaseEnergy(double amount) {
    biologyManager.increaseEnergy(amount);
  }

  public void increaseVitality(double amount) {
    biologyManager.increaseVitality(amount);
  }

  public boolean addToInventory(ItemData item) {
    return inventoryManager.addItem(item);
  }

  public ItemData removeFromInventory(String itemId) {
    return inventoryManager.removeItem(itemId);
  }

  public boolean equipItem(String itemId, String hand) {
    ItemData itemToEquip = inventoryManager.getItem(itemId);

    if (itemToEquip == null) {
      return false;
    }

    EquippedItem eqItem = new EquippedItem(itemToEquip.id(), itemToEquip.name(), itemToEquip.tags());

    if ("left".equalsIgnoreCase(hand)) {
      return inventoryManager.equipLeftHand(eqItem);
    } else if ("right".equalsIgnoreCase(hand)) {
      return inventoryManager.equipRightHand(eqItem);
    }
    return false;
  }

  public boolean unequipItem(String hand) {
    if ("left".equalsIgnoreCase(hand)) {
      return inventoryManager.unequipLeftHand() != null;
    } else if ("right".equalsIgnoreCase(hand)) {
      return inventoryManager.unequipRightHand() != null;
    }
    return false;
  }

  public boolean hasEquippedItemWithTag(String tag) {
    return inventoryManager.hasEquippedItemWithTag(tag);
  }

  public void eat(double nutrition) {
    biologyManager.decreaseHunger(nutrition);
    biologyManager.increaseEnergy(nutrition / 2.0);
  }

  public void drink(double hydration) {
    biologyManager.decreaseStress(hydration);
    biologyManager.increaseVitality(hydration / 2.0);
  }

  public void sleep() {
    biologyManager.increaseEnergy(100.0);
  }

  public void applyStress(double amount) {
    biologyManager.increaseStress(amount);
  }

  public void reduceStress(double amount) {
    biologyManager.decreaseStress(amount);
  }

  public void increaseHunger(double amount) {
    biologyManager.increaseHunger(amount);
  }

  public void applyPhysiologicalRates(double dtSeconds, double hungerRate, double energyRate, double stressRate) {
    biologyManager.applyRate(hungerRate, dtSeconds, (bm, val) -> bm.increaseHunger((double) val));
    biologyManager.applyRate(energyRate, dtSeconds, (bm, val) -> bm.decreaseEnergy((double) val));
    biologyManager.applyRate(
        stressRate,
        dtSeconds,
        (bm, val) -> bm.decreaseStress((double) val)
    ); // wait, stressRate should be increaseStress?
  }

  public boolean isZoneVisited(String zoneId) {
    return zoneId != null && visitedZoneIds.contains(zoneId);
  }

  public void markZoneAsVisited(String zoneId) {
    if (zoneId != null) {
      visitedZoneIds.add(zoneId);
    }
  }
}
