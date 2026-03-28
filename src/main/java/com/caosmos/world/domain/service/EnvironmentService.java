package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Environment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

  private final TimeService timeService;
  private final Random random = new Random();

  private String currentWeather = "CLEAR";
  private long lastWeatherChangeTick = -1;

  public Environment getCurrentEnvironment() {
    updateWeather();

    int hour = timeService.getCurrentHour();
    String lightLevel = getLightLevel(hour);
    String terrainType = "Urban";

    List<String> tags = new ArrayList<>();
    tags.add("city");
    tags.add(timeService.getDayPeriod());
    tags.add(currentWeather);

    return new Environment(terrainType, tags, lightLevel);
  }

  private void updateWeather() {
    long currentTick = timeService.getCurrentTick();
    // Change weather every 10000 ticks (approx every simulation day or so depending on speed)
    if (lastWeatherChangeTick == -1 || currentTick - lastWeatherChangeTick > 10000) {
      String[] weatherOptions = {"CLEAR", "CLOUDY", "RAINY", "STORM", "FOG"};
      currentWeather = weatherOptions[random.nextInt(weatherOptions.length)];
      lastWeatherChangeTick = currentTick;
    }
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
