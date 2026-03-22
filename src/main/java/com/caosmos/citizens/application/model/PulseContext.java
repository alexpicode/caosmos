package com.caosmos.citizens.application.model;

import com.caosmos.citizens.domain.model.perception.LastAction;
import java.util.List;

/**
 * Immutable context object containing all information needed for a citizen pulse cycle. Provides a clean way to pass
 * pulse-related data between components.
 */
public record PulseContext(
    String citizenName,
    long tick,
    FullPerception fullPerception,
    List<String> informativeEvents,
    LastAction lastAction
) {

}


