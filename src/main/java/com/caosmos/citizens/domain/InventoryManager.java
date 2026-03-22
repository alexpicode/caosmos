package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.perception.Equipment;
import com.caosmos.citizens.domain.model.perception.EquippedItem;
import com.caosmos.citizens.domain.model.perception.Inventory;
import com.caosmos.citizens.domain.model.perception.InventoryCapacity;
import com.caosmos.citizens.domain.model.perception.InventoryItem;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Manages inventory slots, equipment, and items for a citizen
 */
@Data
public class InventoryManager {

  private final int maxSlots;
  private final List<InventoryItem> items;
  private EquippedItem leftHand;
  private EquippedItem rightHand;

  public InventoryManager(int maxSlots) {
    this.maxSlots = maxSlots;
    this.items = new ArrayList<>();
  }

  public boolean addItem(InventoryItem item) {
    if (items.size() >= maxSlots) {
      return false;
    }
    items.add(item);
    return true;
  }

  public boolean removeItem(String itemId) {
    return items.removeIf(item -> item.id().equals(itemId));
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
    return new Inventory(capacity, new ArrayList<>(items));
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
