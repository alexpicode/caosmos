package com.caosmos.common.domain.model.actions;

import java.util.List;

public record ResolutionResult(
    boolean success,
    String narration,
    List<StateMutation> mutations,
    boolean shouldCache
) {
}
