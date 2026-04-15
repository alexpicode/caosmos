package com.caosmos.common.domain.model.items;

import java.util.List;

public record ItemData(
    String id,
    String name,
    List<String> tags,
    String category,
    Double radius,
    Double width,
    Double length,
    Double amount
) {

  public ItemData {
    tags = tags != null ? tags.stream().map(String::toLowerCase).toList() : List.of();
  }
}
