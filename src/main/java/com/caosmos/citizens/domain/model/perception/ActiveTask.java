package com.caosmos.citizens.domain.model.perception;

public record ActiveTask(
    String type,
    String goal,
    String target,
    boolean completed
) {

}
