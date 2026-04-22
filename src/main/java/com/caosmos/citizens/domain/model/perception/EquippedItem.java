package com.caosmos.citizens.domain.model.perception;

import java.util.Set;

public record EquippedItem(
    String id,
    String name,
    Set<String> tags,
    String category,
    String description,
    Double radius,
    Double width,
    Double length,
    Double amount
) {

}
