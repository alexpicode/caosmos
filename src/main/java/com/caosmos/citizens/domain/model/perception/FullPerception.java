package com.caosmos.citizens.domain.model.perception;

import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.List;

public record FullPerception(
    CitizenPerception citizen,
    WorldPerception world,
    ReflexResult reflex,
    List<SpeechMessage> recentMessages
) {

}
