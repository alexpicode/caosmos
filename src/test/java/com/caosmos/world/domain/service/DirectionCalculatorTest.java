package com.caosmos.world.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.caosmos.common.domain.model.world.Vector3;
import org.junit.jupiter.api.Test;

class DirectionCalculatorTest {

  private final DirectionCalculator directionCalculator = new DirectionCalculator();

  @Test
  void shouldReturnCorrectDirections() {
    Vector3 origin = new Vector3(0, 0, 0);

    // Cardinal directions
    assertEquals("East", directionCalculator.getCardinalDirection(origin, new Vector3(5, 0, 0)));
    assertEquals("North", directionCalculator.getCardinalDirection(origin, new Vector3(0, 0, 5)));
    assertEquals("West", directionCalculator.getCardinalDirection(origin, new Vector3(-5, 0, 0)));
    assertEquals("South", directionCalculator.getCardinalDirection(origin, new Vector3(0, 0, -5)));

    // Intercardinal directions
    assertEquals("Northeast", directionCalculator.getCardinalDirection(origin, new Vector3(5, 0, 5)));
    assertEquals("Northwest", directionCalculator.getCardinalDirection(origin, new Vector3(-5, 0, 5)));
    assertEquals("Southeast", directionCalculator.getCardinalDirection(origin, new Vector3(5, 0, -5)));
    assertEquals("Southwest", directionCalculator.getCardinalDirection(origin, new Vector3(-5, 0, -5)));
  }
}
