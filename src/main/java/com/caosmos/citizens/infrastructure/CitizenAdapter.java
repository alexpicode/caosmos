package com.caosmos.citizens.infrastructure;

import com.caosmos.citizens.application.handler.CitizenPerceptionHandler;
import com.caosmos.citizens.application.registry.CitizenRegistry;
import com.caosmos.citizens.application.registry.TaskRegistry;
import com.caosmos.citizens.application.social.ConversationManager;
import com.caosmos.citizens.domain.model.Hand;
import com.caosmos.citizens.domain.model.perception.LastAction;
import com.caosmos.citizens.domain.task.ConversationTask;
import com.caosmos.citizens.domain.task.ExploreTask;
import com.caosmos.citizens.domain.task.RestTask;
import com.caosmos.citizens.domain.task.SleepTask;
import com.caosmos.citizens.domain.task.Task;
import com.caosmos.citizens.domain.task.TravelToTask;
import com.caosmos.citizens.domain.task.WaitTask;
import com.caosmos.citizens.domain.task.WorkTask;
import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.contracts.WorldRegistry;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
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
  private final CitizenPerceptionHandler perceptionHandler;
  private final ConversationManager conversationManager;

  @Override
  public boolean isNear(UUID citizenId, String targetId, double maxDistance) {
    Vector3 pos = getPosition(citizenId);
    String currentZoneId = getCurrentZoneId(citizenId);
    return worldPort.isNearObject(pos, currentZoneId, targetId, maxDistance);
  }

  @Override
  public boolean isNearObjectWithTag(UUID citizenId, String tag, double maxDistance) {
    Vector3 pos = getPosition(citizenId);
    String currentZoneId = getCurrentZoneId(citizenId);
    return worldPort.isNearObjectWithTag(pos, currentZoneId, tag, maxDistance);
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
  public Set<String> getTagsByToolReference(UUID citizenId, String toolRef) {
    if (toolRef == null || toolRef.isBlank()) {
      return Collections.emptySet();
    }

    String normalizedRef = toolRef.trim().toLowerCase();

    return citizenRegistry.get(citizenId)
        .map(citizen -> {
          var inv = citizen.inventory();
          Set<String> tags = new java.util.HashSet<>();

          switch (normalizedRef) {
            case "left" -> {
              if (inv.getLeftHand() != null && inv.getLeftHand().tags() != null) {
                tags.addAll(inv.getLeftHand().tags());
              }
            }
            case "right" -> {
              if (inv.getRightHand() != null && inv.getRightHand().tags() != null) {
                tags.addAll(inv.getRightHand().tags());
              }
            }
            case "both" -> {
              if (inv.getLeftHand() != null && inv.getLeftHand().tags() != null) {
                tags.addAll(inv.getLeftHand().tags());
              }
              if (inv.getRightHand() != null && inv.getRightHand().tags() != null) {
                tags.addAll(inv.getRightHand().tags());
              }
            }
            default -> {
              // Fallback to UUID matching
              if (inv.getLeftHand() != null && normalizedRef.equals(inv.getLeftHand().id().toLowerCase())) {
                if (inv.getLeftHand().tags() != null) {
                  tags.addAll(inv.getLeftHand().tags());
                }
              } else if (inv.getRightHand() != null && normalizedRef.equals(inv.getRightHand().id().toLowerCase())) {
                if (inv.getRightHand().tags() != null) {
                  tags.addAll(inv.getRightHand().tags());
                }
              }
            }
          }
          return tags;
        })
        .orElse(Collections.emptySet());
  }

  @Override
  public Set<String> getEquippedItemTags(UUID citizenId, String itemId) {
    if (itemId == null) {
      return java.util.Collections.emptySet();
    }
    return citizenRegistry.get(citizenId)
        .map(citizen -> {
          var inv = citizen.inventory();
          if (inv.getLeftHand() != null && itemId.equals(inv.getLeftHand().id()) && inv.getLeftHand().tags() != null) {
            return new HashSet<>(inv.getLeftHand().tags());
          }
          if (inv.getRightHand() != null && itemId.equals(inv.getRightHand().id())
              && inv.getRightHand().tags() != null) {
            return new HashSet<>(inv.getRightHand().tags());
          }
          return Collections.<String>emptySet();
        })
        .orElse(Collections.emptySet());
  }

  @Override
  public boolean isItemEquipped(UUID citizenId, String itemId) {
    if (itemId == null) {
      return false;
    }
    return citizenRegistry.get(citizenId)
        .map(citizen -> {
          var inv = citizen.inventory();
          return (inv.getLeftHand() != null && itemId.equals(inv.getLeftHand().id())) ||
              (inv.getRightHand() != null && itemId.equals(inv.getRightHand().id()));
        })
        .orElse(false);
  }

  @Override
  public boolean isItemInInventory(UUID citizenId, String itemId) {
    if (itemId == null) {
      return false;
    }
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.inventory().getItem(itemId) != null)
        .orElse(false);
  }

  @Override
  public List<String> getEquippedItemsNames(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> {
          var inv = citizen.inventory();
          List<String> names = new ArrayList<>();
          if (inv.getLeftHand() != null) {
            names.add(inv.getLeftHand().name() + " (" + inv.getLeftHand().id() + ") [LEFT]");
          }
          if (inv.getRightHand() != null) {
            names.add(inv.getRightHand().name() + " (" + inv.getRightHand().id() + ") [RIGHT]");
          }
          return names;
        })
        .orElse(Collections.emptyList());
  }

  @Override
  public Vector3 getPosition(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.getCurrentState().getPosition())
        .orElseThrow(() -> new IllegalArgumentException("Citizen not found: " + citizenId));
  }

  @Override
  public void updatePosition(UUID citizenId, Vector3 newPos) {
    log.debug("Updating position for citizen {}: {}", citizenId, newPos);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      citizen.getCurrentState().setPosition(newPos);
      spatialRegistry.updatePosition(citizen, newPos);

      // Immediate Zone and MentalMap update for real-time monitoring
      perceptionHandler.synchronizeSpatialContext(citizen, newPos);
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
    log.debug("Consuming {} energy for citizen {}", amount, citizenId);
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().decreaseEnergy(amount));
  }

  @Override
  public void applyDamage(UUID citizenId, double amount) {
    log.debug("Applying {} damage to citizen {}", amount, citizenId);
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().decreaseVitality(amount));
  }

  @Override
  public void heal(UUID citizenId, double amount) {
    log.debug("Healing {} to citizen {}", amount, citizenId);
    citizenRegistry.get(citizenId).ifPresent(citizen -> citizen.biology().increaseVitality(amount));
  }

  @Override
  public boolean addToInventory(UUID citizenId, ItemData item) {
    AtomicBoolean result = new AtomicBoolean(false);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
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
    return isInZoneWithTag(citizenId, WorldConstants.TAG_SAFE);
  }


  @Override
  public void assignSleepTask(UUID citizenId) {
    log.debug("Assigning SleepTask for citizen {}", citizenId);
    registerAndSyncTask(citizenId, new SleepTask());
  }

  @Override
  public void assignWorkTask(UUID citizenId, String workplaceType) {
    log.debug("Assigning WorkTask ({}) for citizen {}", workplaceType, citizenId);
    registerAndSyncTask(citizenId, new WorkTask(workplaceType));
  }

  @Override
  public void assignWaitTask(UUID citizenId, boolean inSafeZone) {
    log.debug("Assigning WaitTask (safe={}) for citizen {}", inSafeZone, citizenId);
    registerAndSyncTask(citizenId, new WaitTask(inSafeZone));
  }

  @Override
  public void assignRestTask(UUID citizenId) {
    log.debug("Assigning RestTask for citizen {}", citizenId);
    registerAndSyncTask(citizenId, new RestTask());
  }

  @Override
  public void assignTravelToTask(UUID citizenId, Vector3 target, String targetId) {
    log.debug("Setting TravelTo task for citizen {} to {}", citizenId, target);
    registerAndSyncTask(citizenId, new TravelToTask(target, targetId, worldPort));
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
    registerAndSyncTask(citizenId, new ExploreTask(direction, targetCategory, reason, worldPort));
  }

  @Override
  public void assignConversationTask(UUID citizenId, String targetId, Vector3 targetPosition) {
    log.debug("Setting Conversation task for citizen {} with {}", citizenId, targetId);
    Supplier<Boolean> isSessionEnded = () -> conversationManager.getActiveSession(citizenId.toString()).isEmpty();
    registerAndSyncTask(citizenId, new ConversationTask(targetId, targetPosition, isSessionEnded));
  }

  private void registerAndSyncTask(UUID citizenId, Task task) {
    taskRegistry.register(citizenId, task);
    citizenRegistry.get(citizenId).ifPresent(citizen -> {
      LastAction assignmentAction = new LastAction(
          "Task Assigned",
          "SUCCESS",
          "Assigned external task: " + task.getClass().getSimpleName(),
          "Task received and accepted",
          java.util.Map.of()
      );
      citizen.updateTask(task.toActiveTask(citizen));
      citizen.transitionTo(task.getCitizenState(), assignmentAction);
    });
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

  @Override
  public String getName(UUID citizenId) {
    return citizenRegistry.get(citizenId)
        .map(citizen -> citizen.getCitizenProfile().identity().name())
        .orElse(null);
  }

  @Override
  public void initiateOrJoinConversation(
      String citizenId,
      String citizenName,
      String targetId,
      String targetName,
      long tick
  ) {
    conversationManager.initiateOrJoin(citizenId, citizenName, targetId, targetName, tick);
  }

  @Override
  public void registerDialogue(
      String speakerId,
      String speakerName,
      String targetId,
      String message,
      String tone,
      long tick
  ) {
    conversationManager.registerDialogue(speakerId, speakerName, targetId, message, tone, tick);
  }
}
