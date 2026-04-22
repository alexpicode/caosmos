package com.caosmos.citizens.domain.model.perception;

public record InventoryCapacity(
    int usedSlots,
    int maxSlots,
    String status
) {

}
