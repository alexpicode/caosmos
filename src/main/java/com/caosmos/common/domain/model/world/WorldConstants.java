package com.caosmos.common.domain.model.world;

/**
 * Global constants for the world model, including common semantic tags.
 */
public final class WorldConstants {

  private WorldConstants() {
  }

  // Property & Ownership
  public static final String TAG_UNOWNED = "unowned";
  public static final String PREFIX_OWNER = "owner:";

  // Security & Access
  public static final String TAG_LOCKED = "locked";
  public static final String TAG_SAFE = "safe";

  // Work & Industry
  public static final String TAG_WORKSTATION = "workstation";
  public static final String TAG_FULLY_STAFFED = "fully_staffed";
  public static final String TAG_MINING = "mining";
  public static final String TAG_COMMERCE = "commerce";
  public static final String TAG_FORGE = "forge";

  // Item Capabilities
  public static final String TAG_TOOL = "tool";
  public static final String TAG_CRAFTING = "crafting";
  public static final String TAG_WOODCUTTING = "woodcutting";
  public static final String TAG_COIN_CONTAINER = "coin_container";

  // Perception & Optimization
  public static final String TAG_STATIC = "static";

  // Environment & Weather
  public static final String TAG_CITY = "city";
  public static final String TAG_URBAN = "urban";
  public static final String TAG_DAY = "day";
  public static final String TAG_NIGHT = "night";
  public static final String TAG_CLEAR = "clear";
  public static final String TAG_RAINY = "rainy";
  public static final String TAG_STORM = "storm";
  public static final String TAG_FOG = "fog";
  public static final String TAG_SNOW = "snow";
  public static final String TAG_WINTER = "winter";
  public static final String TAG_HEATWAVE = "heatwave";
}
