package com.caosmos.directors.domain.model;

import com.caosmos.common.domain.model.actions.ResolutionResult;
import java.time.Instant;

public record WisdomEntry(
    CacheKey key,
    ResolutionResult result,
    Instant createdAt,
    int hitCount
) {

}
