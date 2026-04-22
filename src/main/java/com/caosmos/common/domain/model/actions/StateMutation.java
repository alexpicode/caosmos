package com.caosmos.common.domain.model.actions;

public record StateMutation(
    String targetId,
    MutationType mutationType,
    String key,
    String value
) {

}
