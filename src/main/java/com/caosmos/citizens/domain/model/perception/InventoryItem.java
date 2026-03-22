package com.caosmos.citizens.domain.model.perception;

import java.util.List;

public record InventoryItem(
    String id,
    String name,
    List<String> tags,
    int quantity
) {

}
