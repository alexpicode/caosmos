package com.caosmos.citizens.domain.model.perception;

import java.util.List;

public record EquippedItem(
    String id,
    String name,
    List<String> tags
) {

}
