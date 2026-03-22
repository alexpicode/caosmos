package com.caosmos.citizens.domain.model.perception;

import java.util.List;

public record Inventory(
    InventoryCapacity capacity,
    List<InventoryItem> items
) {

}