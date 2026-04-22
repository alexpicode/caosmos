package com.caosmos.common.domain.model.actions;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.Set;
import java.util.UUID;

public record ActionIntent(
    UUID citizenId,
    String verb,
    String targetId,
    Set<String> toolTags,
    Set<String> targetTags,
    Set<String> environmentTags,
    Vector3 citizenPosition,
    Vector3 targetPosition
) {

}
