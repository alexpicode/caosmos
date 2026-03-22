package com.caosmos.citizens.domain.model.perception;

import com.caosmos.common.domain.model.world.Vector3;

public record CitizenPerception(
    Identity identity,
    Status status,
    Equipment equipment,
    Inventory inventory,
    LastAction lastAction,
    ActiveTask activeTask,
    Vector3 position
) {

}
