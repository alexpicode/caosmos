package com.caosmos.citizens.domain.model.perception;

import com.caosmos.common.domain.model.world.WorldPerception;

public record FullPerception(
    CitizenPerception citizen,
    WorldPerception world,
    ReflexResult reflex
) {

}
