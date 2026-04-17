package com.caosmos.citizens.domain;

/**
 * Centralizes all physiological constants and thresholds. Alle rates are defined as positive magnitudes per hour.
 */
public final class PhysiologicalThresholds {

  private PhysiologicalThresholds() {
    // Prevent instantiation
  }

  // Thresholds (0-100 scale)
  public static final double HUNGER_CRISIS = 80.0;
  public static final double ENERGY_EXTREME_FATIGUE = 15.0;
  public static final double ENERGY_COLLAPSE = 5.0;
  public static final double ENERGY_RECOVERY_WAKE = 20.0;
  public static final double STRESS_PANIC = 95.0;
  public static final double STRESS_CRITICAL = 80.0;
  public static final double VITALITY_CRITICAL = 30.0;
  public static final double VITALITY_NONE = 5.0;
  public static final double HUNGER_FATAL = 95.0;

  // Perception Sensory thresholds
  public static final double ENTITY_PROXIMITY_ALERT_DISTANCE = 1.5;

  // Passive metabolic rates (per hour - positive magnitudes)
  public static final double PASSIVE_HUNGER_RATE = 0.5;
  public static final double PASSIVE_ENERGY_DECAY_RATE = 0.4;

  // Crisis effects
  public static final double HUNGER_CRISIS_VITALITY_DRAIN_RATE = 1.0;
  public static final double EXTREME_FATIGUE_SPEED_FACTOR = 0.5;

  // Movement costs (per hour)
  public static final double MOVE_ENERGY_COST_RATE = 1.5;
  public static final double MOVE_HUNGER_COST_RATE = 1.0;

  // Recovery rates (per hour)
  public static final double SLEEP_ENERGY_RECOVERY_RATE = 10.0;
  public static final double SLEEP_STRESS_REDUCTION_RATE = 5.0;
  public static final double SLEEP_VITALITY_RESTORATION_RATE = 4.0;
  public static final double SLEEP_HUNGER_INCREASE_RATE = 0.2;

  public static final double REST_ENERGY_RECOVERY_RATE = 5.0;
  public static final double REST_STRESS_REDUCTION_RATE = 1.5;

  public static final double WAIT_ENERGY_DECAY_RATE = 0.2;
  public static final double SAFE_ZONE_STRESS_REDUCTION_RATE = 1.0;

  // Work rates (per hour)
  public static final double MINE_ENERGY_DRAIN_RATE = 3.5;
  public static final double MINE_HUNGER_INCREASE_RATE = 1.8;
  public static final double MINE_STRESS_INCREASE_RATE = 0.6;

  public static final double SHOP_ENERGY_DRAIN_RATE = 1.2;
  public static final double SHOP_HUNGER_INCREASE_RATE = 0.6;
  public static final double SHOP_STRESS_INCREASE_RATE = 0.3;

  // Task Durations (minutes)
  public static final double DEFAULT_WAIT_DURATION_MINUTES = 1.0;
  public static final double DEFAULT_WORK_DURATION_HOURS = 8.0;
}
