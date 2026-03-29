package com.caosmos.citizens.domain.model.perception;

/**
 * Result of the perception evaluation, containing the reflex results and any pending state mutations that should be
 * applied by the caller.
 */
public record PerceptionEvaluation(
    ReflexResult reflex,
    String newZoneId,
    String newZoneName
) {

  public boolean isCritical() {
    return reflex != null && reflex.critical();
  }

  public String reason() {
    return reflex != null ? reflex.reason() : null;
  }

  public boolean hasEnteredNewZone() {
    return newZoneId != null;
  }
}

