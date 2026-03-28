package com.caosmos.common.domain.model.items;

import java.util.List;

public record ItemData(
    String id,
    String name,
    List<String> tags
) {

  public ItemData {
    tags = tags != null ? tags.stream().map(String::toLowerCase).toList() : List.of();
  }
}
