package com.caosmos.common.domain.model.items;

import java.util.List;

public record ItemData(
    String id,
    String name,
    List<String> tags,
    String category,
    Double radius,
    Double width,
    Double length
) {

  public ItemData {
    tags = tags != null ? tags.stream().map(String::toLowerCase).toList() : List.of();
  }

  // Helper constructor for basic items or legacy calls
  public ItemData(String id, String name, List<String> tags) {
    this(id, name, tags, "RESOURCE", 0.1, null, null);
  }
}
