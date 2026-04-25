package com.caosmos.actions.domain;

import com.caosmos.common.domain.model.world.WorldConstants;


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
  public static final double AUDIBLE_RANGE = 20.0;
  public static final double PROXIMITY_USE = 2.5;
  public static final double PROXIMITY_EXAMINE = 2.5;
  public static final double DISTANT_NAVIGATION_STEP = 50.0;

  // Energy Costs (Maneuver Effort - Fixed metabolic pulse)
  public static final double ENERGY_COST_PICKUP = 0.1;
  public static final double ENERGY_COST_DROP = 0.05;
  public static final double ENERGY_COST_EAT = 0.1;
  public static final double ENERGY_COST_EQUIP = 0.1;
  public static final double ENERGY_COST_UNEQUIP = 0.05;
  public static final double ENERGY_COST_EXAMINE = 0.1;
  public static final double ENERGY_COST_TALK = 0.02;
  public static final double ENERGY_COST_USE = 0.5;

  // Physiological Recovery/Impact (Points)
  public static final double DRINK_HYDRATION_RECOVERY = 20.0;
  public static final double DRINK_STRESS_REDUCTION = 2.0;
  public static final double EAT_NUTRITION_RECOVERY = 25.0;

  // Workplace Tag Mapping
  public static final String TAG_MINING = WorldConstants.TAG_MINING;
  public static final String TAG_COMMERCE = WorldConstants.TAG_COMMERCE;
  public static final String TAG_FORGE = WorldConstants.TAG_FORGE;

  // Item Tags
  public static final String ITEM_TAG_TOOL = WorldConstants.TAG_TOOL;
  public static final String ITEM_TAG_MINING = WorldConstants.TAG_MINING;
  public static final String ITEM_TAG_CRAFTING = WorldConstants.TAG_CRAFTING;
  public static final String ITEM_TAG_WOODCUTTING = WorldConstants.TAG_WOODCUTTING;


}
