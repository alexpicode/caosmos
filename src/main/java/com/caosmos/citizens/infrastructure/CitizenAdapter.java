package com.caosmos.citizens.infrastructure;

import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.domain.model.Hand;
import com.caosmos.citizens.domain.task.ExploreTask;
import com.caosmos.citizens.domain.task.RestTask;
import com.caosmos.citizens.domain.task.SleepTask;
import com.caosmos.citizens.domain.task.TravelToTask;
import com.caosmos.citizens.domain.task.WaitTask;
import com.caosmos.citizens.domain.task.WorkTask;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.contracts.WorldRegistry;
import com.caosmos.common.domain.model.items.ItemData;
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
  private final WorldPort worldPort;

  @Override
  public boolean isNear(UUID citizenId, String targetId, double maxDistance) {
    Vector3 pos = getPosition(citizenId);
    return worldPort.isNearObject(pos, targetId, maxDistance);
  }

  @Override
  public boolean isInZoneWithTag(UUID citizenId, String tag) {
    if (tag == null) {
      return false;
    }
    Vector3 pos = getPosition(citizenId);
    return worldPort.getZoneTagsAt(pos).contains(tag.toLowerCase());
  }

  @Override
  public boolean isItemEquippedWithTag(UUID citizenId, String tag) {
    if (tag == null) {
      return false;
    }
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.inventory().hasEquippedItemWithTag(tag))
        .orElse(false);
  }

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
  public String getCurrentZoneId(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.getCurrentState().getCurrentZoneId())
        .orElse(null);
  }

  @Override
  public void enterZone(UUID citizenId, String zoneId, String zoneName) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      citizen.enterZone(zoneId, zoneName);
    });
  }

  @Override
  public void consumeEnergy(UUID citizenId, double amount) {
    log.info("Consuming {} energy for citizen {}", amount, citizenId);
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().decreaseEnergy(amount));
  }

  @Override
  public boolean addToInventory(
      UUID citizenId, String itemId, String itemName, List<String> tags
  ) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      ItemData item = new ItemData(itemId, itemName, tags);
      result.set(citizen.inventory().addItem(item));
    });
    return result.get();
  }

  @Override
  public ItemData removeFromInventory(UUID citizenId, String itemId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.inventory().removeItem(itemId))
        .orElse(null);
  }

  @Override
  public boolean equipItem(UUID citizenId, String itemId, String hand) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      try {
        Hand h = Hand.valueOf(hand.toUpperCase());
        result.set(citizen.inventory().equipToHand(itemId, h));
      } catch (IllegalArgumentException e) {
        log.warn("Invalid hand for equipment: {}", hand);
      }
    });
    return result.get();
  }

  @Override
  public boolean unequipItem(UUID citizenId, String hand) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      try {
        Hand h = Hand.valueOf(hand.toUpperCase());
        result.set(citizen.inventory().unequipHand(h));
      } catch (IllegalArgumentException e) {
        log.warn("Invalid hand for equipment: {}", hand);
      }
    });
    return result.get();
  }

  @Override
  public void eat(UUID citizenId, double nutrition) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().processNutrition(nutrition));
  }

  @Override
  public void drink(UUID citizenId, double hydration) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().processHydration(hydration));
  }

  @Override
  public void sleep(UUID citizenId) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().fullRecharge());
  }

  @Override
  public void applyStress(UUID citizenId, double amount) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().increaseStress(amount));
  }

  @Override
  public void reduceStress(UUID citizenId, double amount) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().decreaseStress(amount));
  }

  @Override
  public void increaseHunger(UUID citizenId, double amount) {
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().increaseHunger(amount));
  }


  @Override
  public boolean isInSafeZone(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.getCurrentState().getCurrentZone())
        .map(zoneName -> zoneName != null && zoneName.contains("safe"))
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
  public void assignTravelToTask(UUID citizenId, Vector3 target, String targetId) {
    log.debug("Setting TravelTo task for citizen {} to {}", citizenId, target);
    taskRegistry.register(citizenId, new TravelToTask(target, targetId));
  }

  @Override
  public void assignExploreTask(UUID citizenId, Vector3 direction, String targetCategory, String reason) {
    log.debug(
        "Setting Explore task for citizen {} in direction {} with target {} - reason: {}",
        citizenId,
        direction,
        targetCategory,
        reason
    );
    taskRegistry.register(citizenId, new ExploreTask(direction, targetCategory, reason));
  }

  @Override
  public void continueTask(UUID citizenId) {
    log.debug("Citizen {} requested to continue current task. No action needed.", citizenId);
  }

  @Override
  public String getJob(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.getCitizenProfile().identity().job())
        .orElse(null);
  }

  @Override
  public String getWorkplaceTag(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.getCitizenProfile().identity().workplaceTag())
        .orElse(null);
  }
}
