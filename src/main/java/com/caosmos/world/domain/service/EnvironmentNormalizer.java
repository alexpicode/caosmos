package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.EnvironmentImpactTag;
import com.caosmos.common.domain.model.world.WorldConstants;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentNormalizer {

  public SortedSet<EnvironmentImpactTag> normalize(Environment environment) {
    SortedSet<EnvironmentImpactTag> tags = new TreeSet<>();

    if (environment == null) {
      return tags;
    }

    // Map light levels
    if (WorldConstants.TAG_NIGHT.equalsIgnoreCase(environment.lightLevel())
        || "Dusk".equalsIgnoreCase(environment.lightLevel())) {

      tags.add(EnvironmentImpactTag.DARK_ENVIRONMENT);
    }

    // Map high-level weather states (e.g. "STORM") into base physical impacts (e.g. "WET_ENVIRONMENT")
    // This dramatically reduces the combinatorial explosion of the Wisdom Cache keys.
    // E.g. Both Rain and Storm will share the exact same cached veredict for "trying to start a fire".
    if (environment.tags() != null) {
      for (String tag : environment.tags()) {
        String upperTag = tag.toUpperCase();
        switch (upperTag) {
          case "RAINY":
          case "RAIN":
            tags.add(EnvironmentImpactTag.WET_ENVIRONMENT);
            break;
          case "STORM":
            tags.add(EnvironmentImpactTag.WET_ENVIRONMENT);
            tags.add(EnvironmentImpactTag.ACTIVE_WIND);
            break;
          case "FOG":
            tags.add(EnvironmentImpactTag.DARK_ENVIRONMENT);
            break;
          case "SNOW":
          case "WINTER":
            tags.add(EnvironmentImpactTag.FROZEN_ENVIRONMENT);
            break;
          case "HEATWAVE":
            tags.add(EnvironmentImpactTag.SWELTERING_ENVIRONMENT);
            break;
        }

      }
    }

    return tags;
  }
}
