package com.caosmos.directors.domain.model;

import java.util.Set;
import lombok.Value;

@Value
public class ObservationRequest {

  String targetName;
  String targetCategory;
  Set<String> targetTags;
  Set<String> environmentTags;
  String possessionContext; // "GROUND", "INVENTORY", "EQUIPPED"
  String style;
}
