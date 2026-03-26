package com.caosmos.common.domain.model.items;

import java.util.List;

public record ItemData(
    String id,
    String name,
    List<String> tags
) {

}
