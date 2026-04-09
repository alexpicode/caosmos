package com.caosmos.directors.domain.model;

import java.util.SortedSet;

public record ArbitrationRequest(
    String verb,
    SortedSet<String> toolTags,
    SortedSet<String> targetTags,
    SortedSet<String> environmentTags,
    String targetName,
    String targetCategory
) {

}
