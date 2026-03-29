package com.caosmos.citizens.application.core;

import com.caosmos.citizens.application.model.PhysiologicalReflex;
import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.PhysiologicalThresholds;
import com.caosmos.citizens.domain.model.perception.Status;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/**
 * Manages passive biological processes and critical thresholds for citizens.
 */
@Component
public class PhysiologicalMotor {

  private record ThresholdRule(
      Predicate<Status> condition,
      String reason,
      String forcedActionType,
      String event
  ) {

    public Optional<PhysiologicalReflex> evaluate(Status status) {
      if (condition.test(status)) {
        return Optional.of(new PhysiologicalReflex(true, reason, forcedActionType, List.of(event)));
      }
      return Optional.empty();
    }
  }

  private static final List<ThresholdRule> RULES = List.of(
      new ThresholdRule(
          s -> s.energy() < PhysiologicalThresholds.ENERGY_COLLAPSE,
          "Energy Collapse", "SLEEP", "Collapse due to extreme exhaustion."
      ),
      new ThresholdRule(
          s -> s.stress() > PhysiologicalThresholds.STRESS_PANIC,
          "Panic Attack", "FLEE", "Panic crisis! You need to flee to a safe place."
      ),
      new ThresholdRule(
          s -> s.hunger() > PhysiologicalThresholds.HUNGER_FATAL,
          "Starvation", "EAT", "Extreme starvation! You must find food immediately or you will die."
      ),
      new ThresholdRule(
          s -> s.vitality() < PhysiologicalThresholds.VITALITY_NONE,
          "Physical Collapse", "WAIT", "Your body has given up. You collapsed."
      )
  );

  /**
   * Applies base metabolism and crisis effects on each simulation tick.
   */
  public void applyPassiveMetabolism(Citizen citizen, double dtSeconds) {
    double hours = dtSeconds / 3600.0;
    var biology = citizen.biology();

    // 1. Base metabolism
    biology.increaseHunger(PhysiologicalThresholds.PASSIVE_HUNGER_RATE * hours);
    biology.decreaseEnergy(PhysiologicalThresholds.PASSIVE_ENERGY_DECAY_RATE * hours);

    // 2. Hunger crisis (vitality drain)
    Status currentStatus = citizen.getPerception().status();
    if (currentStatus.hunger() > PhysiologicalThresholds.HUNGER_CRISIS) {
      biology.decreaseVitality(PhysiologicalThresholds.HUNGER_CRISIS_VITALITY_DRAIN_RATE * hours);
    }
  }

  /**
   * Evaluates if any physiological state has crossed a critical threshold requiring immediate action.
   */
  public Optional<PhysiologicalReflex> evaluateCriticalThresholds(Citizen citizen) {
    Status status = citizen.getPerception().status();

    return RULES.stream()
        .map(rule -> rule.evaluate(status))
        .flatMap(Optional::stream)
        .findFirst();
  }
}
