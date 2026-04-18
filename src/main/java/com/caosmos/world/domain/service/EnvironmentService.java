package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.infrastructure.config.WeatherStateConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

  private final WorldTimeService worldTimeService;
  private final com.caosmos.world.infrastructure.config.WorldConfigProperties worldConfigProperties;
  private final Random random = new Random();

  private String currentWeather = "CLEAR";
  private double nextWeatherChangeTimeSeconds = -1;

  public Environment getCurrentEnvironment() {
    updateWeather();

    int hour = worldTimeService.getCurrentHour();
    String lightLevel = getLightLevel(hour);
    String terrainType = "Urban";

    List<String> tags = new ArrayList<>();
    tags.add("city");
    tags.add(hour >= 6 && hour <= 18 ? "day" : "night");
    tags.add(currentWeather);

    return new Environment(terrainType, tags, lightLevel);
  }

  public Environment getEffectiveEnvironment(ZoneType zoneType) {
    Environment global = getCurrentEnvironment();

    if (ZoneType.INTERIOR == zoneType) {
      // For interiors, we strip weather effects and set artificial light
      return new Environment(global.terrainType(), List.of(), "Artificial");
    }

    return global;
  }

  private void updateWeather() {
    double now = worldTimeService.getTotalSeconds();

    if (nextWeatherChangeTimeSeconds == -1 || now >= nextWeatherChangeTimeSeconds) {
      var states = worldConfigProperties.weatherStates();
      if (states == null || states.isEmpty()) {
        return;
      }

      // 1. Transition to next state
      String nextState = selectNextState(states);
      if (nextState != null) {
        currentWeather = nextState;
      }

      // 2. Schedule next change
      var config = states.get(currentWeather);
      if (config != null) {
        double durationMinutes = config.minDurationMinutes() +
            (config.maxDurationMinutes() - config.minDurationMinutes()) * random.nextDouble();
        nextWeatherChangeTimeSeconds = now + (durationMinutes * 60.0);
      } else {
        // Fallback if state is not configured
        nextWeatherChangeTimeSeconds = now + 3600.0;
      }
    }
  }

  private String selectNextState(Map<String, WeatherStateConfig> states) {
    var config = states.get(currentWeather);
    if (config == null || config.transitions() == null || config.transitions().isEmpty()) {
      // If no transitions defined, pick a random available state as fallback
      List<String> keys = new ArrayList<>(states.keySet());
      return keys.get(random.nextInt(keys.size()));
    }

    Map<String, Double> transitions = config.transitions();
    double totalWeight = transitions.values().stream().mapToDouble(Double::doubleValue).sum();
    double pick = random.nextDouble() * totalWeight;

    double cumulativeWeight = 0.0;
    for (Map.Entry<String, Double> entry : transitions.entrySet()) {
      cumulativeWeight += entry.getValue();
      if (pick <= cumulativeWeight) {
        return entry.getKey();
      }
    }

    return currentWeather; // Fallback
  }

  private String getLightLevel(int hour) {
    if (hour >= 6 && hour < 8) {
      return "Dawn";
    }
    if (hour >= 8 && hour < 18) {
      return "Day";
    }
    if (hour >= 18 && hour < 20) {
      return "Dusk";
    }
    return "Night";
  }
}
