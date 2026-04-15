package com.caosmos.citizens.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.caosmos.common.domain.model.items.ItemData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InventoryManagerTest {

  private InventoryManager inventoryManager;

  @BeforeEach
  void setUp() {
    inventoryManager = new InventoryManager(5);
  }

  @Test
  void shouldAddItemToInventory() {
    ItemData item = new ItemData("item1", "Test Item", List.of("tag1"), "UNKNOWN", 0.1, null, null, null);
    assertTrue(inventoryManager.addItem(item));
    assertEquals(1, inventoryManager.getItems().size());
    assertTrue(inventoryManager.getItems().containsKey("item1"));
    assertEquals(item, inventoryManager.getItems().get("item1"));
  }

  @Test
  void shouldNotAddItemIfInventoryFull() {
    for (int i = 0; i < 5; i++) {
      inventoryManager.addItem(new ItemData("id" + i, "item" + i, List.of(), "UNKNOWN", 0.1, null, null, null));
    }
    ItemData extraItem = new ItemData("extra", "Extra Item", List.of(), "UNKNOWN", 0.1, null, null, null);
    assertFalse(inventoryManager.addItem(extraItem));
    assertEquals(5, inventoryManager.getItems().size());
  }

  @Test
  void shouldRemoveItemAndReturnIt() {
    ItemData item = new ItemData("item1", "Test Item", List.of("tag1"), "UNKNOWN", 0.1, null, null, null);
    inventoryManager.addItem(item);

    ItemData removedItem = inventoryManager.removeItem("item1");

    assertNotNull(removedItem);
    assertEquals("item1", removedItem.id());
    assertEquals("Test Item", removedItem.name());
    assertTrue(inventoryManager.getItems().isEmpty());
  }

  @Test
  void shouldReturnNullIfItemNotFound() {
    ItemData removedItem = inventoryManager.removeItem("nonexistent");
    assertNull(removedItem);
  }

  @Test
  void shouldHandleMultipleItemsRemoval() {
    ItemData item1 = new ItemData("item1", "Item 1", List.of(), "UNKNOWN", 0.1, null, null, null);
    ItemData item2 = new ItemData("item2", "Item 2", List.of(), "UNKNOWN", 0.1, null, null, null);
    inventoryManager.addItem(item1);
    inventoryManager.addItem(item2);

    ItemData removed = inventoryManager.removeItem("item1");

    assertEquals(item1, removed);
    assertEquals(1, inventoryManager.getItems().size());
    assertTrue(inventoryManager.getItems().containsKey("item2"));
  }

  @Test
  void shouldMaintainInsertionOrder() {
    ItemData item1 = new ItemData("item1", "Item 1", List.of(), "UNKNOWN", 0.1, null, null, null);
    ItemData item2 = new ItemData("item2", "Item 2", List.of(), "UNKNOWN", 0.1, null, null, null);
    ItemData item3 = new ItemData("item3", "Item 3", List.of(), "UNKNOWN", 0.1, null, null, null);

    inventoryManager.addItem(item1);
    inventoryManager.addItem(item2);
    inventoryManager.addItem(item3);

    var iter = inventoryManager.getItems().values().iterator();
    assertEquals(item1, iter.next());
    assertEquals(item2, iter.next());
    assertEquals(item3, iter.next());
  }
}
