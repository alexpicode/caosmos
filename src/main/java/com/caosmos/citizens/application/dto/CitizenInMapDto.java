package com.caosmos.citizens.application.dto;

import java.util.UUID;

public record CitizenInMapDto(
    UUID uuid,
    double x,
    double z,
    String state
) {

}
