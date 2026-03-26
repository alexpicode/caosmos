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

  public Inventory getInventory() {
    InventoryCapacity capacity = getInventoryCapacity();
    return new Inventory(capacity, new ArrayList<>(items.values()));
  }

  private InventoryCapacity getInventoryCapacity() {
    String status = "vacío";
    double fillRatio = (double) items.size() / maxSlots;

    if (fillRatio >= 0.9) {
      status = "lleno";
    } else if (fillRatio >= 0.7) {
      status = "casi_lleno";
    } else if (fillRatio >= 0.4) {
      status = "mitad_lleno";
    } else if (fillRatio >= 0.1) {
      status = "casi_vacío";
    }

    return new InventoryCapacity(items.size(), maxSlots, status);
  }
}
