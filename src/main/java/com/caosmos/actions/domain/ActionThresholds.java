package com.caosmos.actions.domain;

/**
 * Centralizes all thresholds and constants related to citizen actions, such as proximity distances, energy costs, and
 * physiological recovery values.
 */
public final class ActionThresholds {

  private ActionThresholds() {
    // Prevent instantiation
  }

  // Proximity Distances (meters)
  public static final double PROXIMITY_PICKUP = 2.0;
  public static final double PROXIMITY_TALK = 3.0;
  public static final double PROXIMITY_USE = 2.5;
  public static final double PROXIMITY_EXAMINE = 4.0;
  public static final double DISTANT_NAVIGATION_STEP = 50.0;

  // Energy Costs (Energy units)
  public static final double ENERGY_COST_PICKUP = 2.0;
  public static final double ENERGY_COST_DROP = 1.0;
  public static final double ENERGY_COST_EAT = 0.5;
  public static final double ENERGY_COST_EQUIP = 1.0;
  public static final double ENERGY_COST_UNEQUIP = 1.0;
  public static final double ENERGY_COST_EXAMINE = 1.0;
  public static final double ENERGY_COST_TALK = 0.2;
  public static final double ENERGY_COST_USE = 4.0;

  // Physiological Recovery/Impact (Points)
  public static final double DRINK_HYDRATION_RECOVERY = 20.0;
  public static final double DRINK_STRESS_REDUCTION = 2.0;
  public static final double EAT_NUTRITION_RECOVERY = 25.0;
  public static final double TALK_STRESS_REDUCTION = 5.0;

  // Workplace Tag Mapping
  public static final String TAG_MINING = "MINING";
  public static final String TAG_COMMERCE = "COMMERCE";
  public static final String TAG_FORGE = "FORGE";

  // Item Tags
  public static final String ITEM_TAG_TOOL = "tool";
  public static final String ITEM_TAG_MINING = "mining";
  public static final String ITEM_TAG_CRAFTING = "crafting";
  public static final String ITEM_TAG_WOODCUTTING = "woodcutting";
}
