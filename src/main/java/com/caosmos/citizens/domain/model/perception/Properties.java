package com.caosmos.citizens.domain.model.perception;

import java.util.List;

public record Properties(
    String currentTask,
    List<EquippedItem> equippedItems,
    String inventorySpace
) {

}
