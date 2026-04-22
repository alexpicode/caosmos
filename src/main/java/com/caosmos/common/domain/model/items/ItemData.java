package com.caosmos.common.domain.model.items;

import java.util.Set;
import java.util.stream.Collectors;

public record ItemData(
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

  public ItemData {
    tags = tags != null ? tags.stream().map(String::toLowerCase).collect(Collectors.toSet()) : Set.of();
  }
}
