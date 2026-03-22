package com.caosmos.citizens.domain.model;

import com.caosmos.citizens.domain.model.perception.Identity;
import com.caosmos.citizens.domain.model.perception.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CitizenProfile(
    Identity identity,
    Status status,
    @JsonProperty("base_location") BaseLocation baseLocation,
    String personality,
    String manifestId
) {

  public record BaseLocation(
      int x,
      int y,
      int z
  ) {

  }

}