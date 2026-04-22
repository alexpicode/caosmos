package com.caosmos.citizens.application.model;

import java.util.List;

/**
 * Represents a physiological event that might trigger a reflex or interruption.
 */
public record PhysiologicalReflex(
    boolean critical,
    String reason,
    String forcedActionType,
    List<String> events
) {

}
