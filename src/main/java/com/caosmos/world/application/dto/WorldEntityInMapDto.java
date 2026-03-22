package com.caosmos.world.application.dto;

import com.caosmos.common.domain.model.world.Vector3;

public record WorldEntityInMapDto(
    String id,
    String type,
    Vector3 position
) {

}
