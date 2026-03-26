package com.caosmos.citizens.domain.model.perception;

import com.caosmos.common.domain.model.items.ItemData;
import java.util.List;

public record Inventory(
    InventoryCapacity capacity,
    List<ItemData> items
) {

}