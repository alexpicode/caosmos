package com.caosmos.citizens.application.core;

import com.caosmos.citizens.application.model.PhysiologicalReflex;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Manages passive biological processes and critical thresholds for citizens.
 */
@Component
public class PhysiologicalMotor {

  /**
   * Applies base metabolism and crisis effects on each simulation tick.
   */
  public void applyPassiveMetabolism(Citizen citizen, double dtSeconds) {
    // 1. Base metabolism
    citizen.increaseHunger(PhysiologicalThresholds.PASSIVE_HUNGER_RATE * (dtSeconds / 3600.0));
    citizen.consumeEnergy(PhysiologicalThresholds.PASSIVE_ENERGY_DECAY_RATE * (dtSeconds / 3600.0));

    // 2. Hunger crisis (vitality drain)
    Status currentStatus = citizen.getPerception().status();
    if (currentStatus.hunger() > PhysiologicalThresholds.HUNGER_CRISIS) {
      citizen.decayVitality(PhysiologicalThresholds.HUNGER_CRISIS_VITALITY_DRAIN_RATE * (dtSeconds / 3600.0));
    }
  }

  /**
   * Evaluates if any physiological state has crossed a critical threshold requiring immediate action.
   */
  public Optional<PhysiologicalReflex> evaluateCriticalThresholds(Citizen citizen) {
    Status status = citizen.getPerception().status();
    List<String> events = new ArrayList<>();

    // 1. Energy Collapse (Reflex)
    if (status.energy() < PhysiologicalThresholds.ENERGY_COLLAPSE) {
      events.add("Collapse due to extreme exhaustion.");
      return Optional.of(new PhysiologicalReflex(true, "Energy Collapse", "SLEEP", events));
    }

    // 2. Stress Panic (Reflex)
    if (status.stress() > PhysiologicalThresholds.STRESS_PANIC) {
      events.add("Panic crisis! You need to flee to a safe place.");
      return Optional.of(new PhysiologicalReflex(true, "Panic Attack", "FLEE", events));
    }

    // 3. Fatal Hunger (Reflex)
    if (status.hunger() > PhysiologicalThresholds.HUNGER_FATAL) {
      events.add("Extreme starvation! You must find food immediately or you will die.");
      return Optional.of(new PhysiologicalReflex(true, "Starvation", "EAT", events));
    }

    // 4. Physical Collapse (Reflex)
    if (status.vitality() < PhysiologicalThresholds.VITALITY_NONE) {
      events.add("Your body has given up. You collapsed.");
      return Optional.of(new PhysiologicalReflex(true, "Physical Collapse", "WAIT", events));
    }

    return Optional.empty();
  }
}
