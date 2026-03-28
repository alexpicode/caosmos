package com.caosmos.citizens.domain;

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
 * Manages inventory slots, equipment, and items for a citizen
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

  public boolean addItem(ItemData item) {
    if (items.size() >= maxSlots) {
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

  public boolean equipLeftHand(EquippedItem item) {
    this.leftHand = item;
    return true;
  }

  public boolean equipRightHand(EquippedItem item) {
    this.rightHand = item;
    return true;
  }

  public EquippedItem unequipLeftHand() {
    EquippedItem item = leftHand;
    leftHand = null;
    return item;
  }

  public EquippedItem unequipRightHand() {
    EquippedItem item = rightHand;
    rightHand = null;
    return item;
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
    double fillRatio = (double) items.size() / maxSlots;

    if (fillRatio >= 0.9) {
      status = "full";
    } else if (fillRatio >= 0.7) {
      status = "almost_full";
    } else if (fillRatio >= 0.4) {
      status = "half_full";
    } else if (fillRatio >= 0.1) {
      status = "almost_empty";
    }

    return new InventoryCapacity(items.size(), maxSlots, status);
  }
}
