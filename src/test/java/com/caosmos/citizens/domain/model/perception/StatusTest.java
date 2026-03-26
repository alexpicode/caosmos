package com.caosmos.citizens.domain.model.perception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatusTest {

  @Test
  void testStatusRounding() {
    Status status = new Status(85.399999, 10.05, 5.01, 99.99);

    assertEquals(85.4, status.vitality());
    assertEquals(10.1, status.hunger());
    assertEquals(5.0, status.energy());
    assertEquals(100.0, status.stress());
  }
}
