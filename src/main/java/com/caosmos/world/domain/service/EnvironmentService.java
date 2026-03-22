package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Environment;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

  private final TimeService timeService;

  public Environment getCurrentEnvironment() {
    int hour = timeService.getCurrentHour();
    String lightLevel = getLightLevel(hour);
    String terrainType = "Urban";

    List<String> tags = new ArrayList<>();
    tags.add("city");
    tags.add(timeService.getDayPeriod());

    return new Environment(terrainType, tags, lightLevel);
  }

  private String getLightLevel(int hour) {
    if (hour >= 6 && hour < 8) {
      return "Dawn";
    }
    if (hour >= 8 && hour < 17) {
      return "Full Day";
    }
    if (hour >= 17 && hour < 19) {
      return "Dusk";
    }
    return "Night";
  }
}
