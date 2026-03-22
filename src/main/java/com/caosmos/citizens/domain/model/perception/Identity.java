package com.caosmos.citizens.domain.model.perception;

import java.util.List;
import java.util.Map;

public record Identity(
    String name,
    List<String> traits,
    Map<String, Integer> skills
) {

}
