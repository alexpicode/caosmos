package com.caosmos.citizens.domain.model.perception;

import com.caosmos.citizens.domain.model.CitizenState;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.List;

public record CitizenPerception(
    Identity identity,
    Status status,
    CitizenState state,
    Equipment equipment,
    Inventory inventory,
    LastAction lastAction,
    ActiveTask activeTask,
    Vector3 position,
    MentalMap mentalMap,
    List<SpeechMessage> recentMessages,
    double coins
) {

}
