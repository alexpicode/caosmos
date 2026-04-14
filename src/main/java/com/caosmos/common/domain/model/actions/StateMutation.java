package com.caosmos.common.domain.model.actions;

public record StateMutation(
    String targetId,
    String mutationType,
    String key,
    String value
) {
}
