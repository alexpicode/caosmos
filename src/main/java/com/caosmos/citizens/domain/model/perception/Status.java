package com.caosmos.citizens.domain.model.perception;

public record Status(
    double vitality,
    double hunger,
    double energy,
    double stress
) {

  public Status {
    vitality = Math.round(vitality * 10.0) / 10.0;
    hunger = Math.round(hunger * 10.0) / 10.0;
    energy = Math.round(energy * 10.0) / 10.0;
    stress = Math.round(stress * 10.0) / 10.0;
  }
}
