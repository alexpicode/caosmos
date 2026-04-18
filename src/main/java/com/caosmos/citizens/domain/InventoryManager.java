package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.Hand;
import com.caosmos.citizens.domain.model.perception.Equipment;
import com.caosmos.citizens.domain.model.perception.EquippedItem;
import com.caosmos.citizens.domain.model.perception.Inventory;
import com.caosmos.citizens.domain.model.perception.InventoryCapacity;
import com.caosmos.common.domain.model.items.ItemData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/**
 * Manages inventory slots, equipment, and items for a citizen.
 */
@Data
public class InventoryManager {

  private final int maxSlots;
  private final Map<String, ItemData> items;
  private EquippedItem leftHand;
  private EquippedItem rightHand;

  public InventoryManager(int maxSlots) {
    this.maxSlots = maxSlots;
    this.items = new LinkedHashMap<>();
  }

  public int getUsedSlotsCount() {
    int count = items.size();
    if (leftHand != null) {
      count++;
    }
    if (rightHand != null) {
      count++;
    }
    return count;
  }

  public boolean addItem(ItemData item) {
    if (getUsedSlotsCount() >= maxSlots) {
      return false;
    }
    items.put(item.id(), item);
    return true;
  }

  public ItemData removeItem(String itemId) {
    return items.remove(itemId);
  }

  public ItemData getItem(String itemId) {
    return items.get(itemId);
  }

  /**
   * Equips an item from the inventory or move it from another hand to the specified hand.
   *
   * @return true if the item was found and equipped successfully.
   */
  public boolean equipToHand(String itemId, Hand hand) {
    if (itemId == null || itemId.isBlank()) {
      return false;
    }

    ItemData itemToEquip = items.get(itemId);
    boolean fromBackpack = itemToEquip != null;

    if (!fromBackpack) {
      // Check if it's in the other hand
      Hand otherHand = (hand == Hand.LEFT) ? Hand.RIGHT : Hand.LEFT;
      EquippedItem inOther = (otherHand == Hand.LEFT) ? leftHand : rightHand;

      if (inOther != null && itemId.equals(inOther.id())) {
        itemToEquip = new ItemData(
            inOther.id(), inOther.name(), inOther.tags(), inOther.category(),
            inOther.description(),
            inOther.radius(), inOther.width(), inOther.length(), inOther.amount()
        );
        // Remove from other hand first
        if (otherHand == Hand.LEFT) {
          leftHand = null;
        } else {
          rightHand = null;
        }
      }
    } else {
      items.remove(itemId);
    }

    if (itemToEquip == null) {
      return false;
    }

    // If something was in the target hand, move it back to backpack
    if (hand == Hand.LEFT && leftHand != null) {
      unequipHand(Hand.LEFT);
    } else if (hand == Hand.RIGHT && rightHand != null) {
      unequipHand(Hand.RIGHT);
    }

    EquippedItem eqItem = new EquippedItem(
        itemToEquip.id(),
        itemToEquip.name(),
        itemToEquip.tags(),
        itemToEquip.category(),
        itemToEquip.description(),
        itemToEquip.radius(),
        itemToEquip.width(),
        itemToEquip.length(),
        itemToEquip.amount()
    );

    switch (hand) {
      case LEFT -> this.leftHand = eqItem;
      case RIGHT -> this.rightHand = eqItem;
    }

    return true;
  }

  /**
   * Unequips the item from the specified hand.
   *
   * @return true if the hand had an item that was unequipped.
   */
  public boolean unequipHand(Hand hand) {
    EquippedItem item = (hand == Hand.LEFT) ? leftHand : rightHand;
    if (item == null) {
      return false;
    }

    // Clear hand
    if (hand == Hand.LEFT) {
      leftHand = null;
    } else {
      rightHand = null;
    }

    // Move back to regular items (backpack)
    // Note: This won't fail capacity because the item was already counting while equipped
    items.put(
        item.id(), new ItemData(
            item.id(),
            item.name(),
            item.tags(),
            item.category(),
            item.description(),
            item.radius(),
            item.width(),
            item.length(),
            item.amount()
        )
    );
    return true;
  }

  public Equipment getEquipment() {
    return new Equipment(leftHand, rightHand);
  }

  public boolean hasEquippedItemWithTag(String tag) {
    if (tag == null) {
      return false;
    }
    String normalizedTag = tag.toLowerCase();
    if (leftHand != null && leftHand.tags() != null && leftHand.tags().contains(normalizedTag)) {
      return true;
    }
    return rightHand != null && rightHand.tags() != null && rightHand.tags().contains(normalizedTag);
  }

  public Inventory getInventory() {
    InventoryCapacity capacity = getInventoryCapacity();
    return new Inventory(capacity, new ArrayList<>(items.values()));
  }

  private InventoryCapacity getInventoryCapacity() {
    String status = "empty";
    int used = getUsedSlotsCount();
    double fillRatio = (double) used / maxSlots;

    if (fillRatio >= 0.9) {
      status = "full";
    } else if (fillRatio >= 0.7) {
      status = "almost_full";
    } else if (fillRatio >= 0.4) {
      status = "half_full";
    } else if (fillRatio >= 0.1) {
      status = "almost_empty";
    }

    return new InventoryCapacity(used, maxSlots, status);
  }
}
