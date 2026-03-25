package com.caosmos.citizens.infrastructure;

import com.caosmos.citizens.application.CitizenRegistry;
import com.caosmos.citizens.application.TaskRegistry;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.InventoryItem;
import com.caosmos.citizens.domain.model.task.MoveToTargetTask;
import com.caosmos.citizens.domain.model.task.RestTask;
import com.caosmos.citizens.domain.model.task.SleepTask;
import com.caosmos.citizens.domain.model.task.WaitTask;
import com.caosmos.citizens.domain.model.task.WorkTask;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldRegistry;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenAdapter implements CitizenPort {

  private final CitizenRegistry citizenRegistry;
  private final TaskRegistry taskRegistry;
  private final WorldRegistry spatialRegistry;

  @Override
  public Vector3 getPosition(UUID citizenId) {
    return citizenRegistry.get(citizenId)
                          .map(citizen -> citizen.getCurrentState().getPosition())
                          .orElseThrow(() -> new IllegalArgumentException("Citizen not found: " + citizenId));
  }

  @Override
  public void updatePosition(UUID citizenId, Vector3 newPos) {
    log.info("Updating position for citizen {}: {}", citizenId, newPos);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      citizen.getCurrentState().setPosition(newPos);
      spatialRegistry.updatePosition(citizen, newPos);
    });
  }

  @Override
  public void consumeEnergy(UUID citizenId, double amount) {
    log.info("Consuming {} energy for citizen {}", amount, citizenId);
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.consumeEnergy(amount));
  }

  @Override
  public boolean addToInventory(
      UUID citizenId, String itemId, String itemName, List<String> tags,
      int quantity
  ) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      InventoryItem item = new InventoryItem(itemId, itemName, tags, quantity);
      result.set(citizen.addToInventory(item));
    });
    return result.get();
  }

  @Override
  public boolean removeFromInventory(UUID citizenId, String itemId) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen ->
        result.set(citizen.removeFromInventory(itemId))
    );
    return result.get();
  }

  @Override
  public boolean equipItem(UUID citizenId, String itemId, String hand) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen ->
        result.set(citizen.equipItem(itemId, hand))
    );
    return result.get();
  }

  @Override
  public boolean unequipItem(UUID citizenId, String hand) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen ->
        result.set(citizen.unequipItem(hand))
    );
    return result.get();
  }

  @Override
  public void eat(UUID citizenId, double nutrition) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.eat(nutrition));
  }

  @Override
  public void drink(UUID citizenId, double hydration) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.drink(hydration));
  }

  @Override
  public void sleep(UUID citizenId) {
    citizenRegistry.get(citizenId).ifPresent(Citizen::sleep);
  }

  @Override
  public void applyStress(UUID citizenId, double amount) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.applyStress(amount));
  }

  @Override
  public void reduceStress(UUID citizenId, double amount) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.reduceStress(amount));
  }

  @Override
  public void increaseHunger(UUID citizenId, double amount) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.increaseHunger(amount));
  }

  @Override
  public boolean isInSafeZone(UUID citizenId) {
    return citizenRegistry.get(citizenId)
                          .map(citizen -> citizen.getCurrentState().getCurrentZone())
                          .map(zoneName -> zoneName != null && zoneName.contains("[seguro]"))
                          .orElse(false);
  }

  @Override
  public void assignSleepTask(UUID citizenId) {
    log.debug("Assigning SleepTask for citizen {}", citizenId);
    taskRegistry.register(citizenId, new SleepTask());
  }

  @Override
  public void assignWorkTask(UUID citizenId, String workplaceType) {
    log.debug("Assigning WorkTask ({}) for citizen {}", workplaceType, citizenId);
    taskRegistry.register(citizenId, new WorkTask(workplaceType));
  }

  @Override
  public void assignWaitTask(UUID citizenId, boolean inSafeZone) {
    log.debug("Assigning WaitTask (safe={}) for citizen {}", inSafeZone, citizenId);
    taskRegistry.register(citizenId, new WaitTask(inSafeZone));
  }

  @Override
  public void assignRestTask(UUID citizenId) {
    log.debug("Assigning RestTask for citizen {}", citizenId);
    taskRegistry.register(citizenId, new RestTask());
  }

  @Override
  public void assignNavigationTask(UUID citizenId, Vector3 target, String targetId) {
    log.debug("Setting navigation task for citizen {} to {}", citizenId, target);
    taskRegistry.register(citizenId, new MoveToTargetTask(target, targetId));
  }
}
