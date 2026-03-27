package com.caosmos.citizens.domain.model.perception;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record Identity(
    String name,
    String job,
    @JsonProperty("workplace_tag") String workplaceTag,
    List<String> traits,
    Map<String, Integer> skills
) {

}
