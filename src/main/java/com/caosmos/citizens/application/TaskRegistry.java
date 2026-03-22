package com.caosmos.citizens.application;

import com.caosmos.citizens.domain.model.task.Task;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TaskRegistry {

  private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();

  public void register(UUID id, Task task) {
    tasks.put(id, task);
  }

  public Optional<Task> get(UUID id) {
    return Optional.ofNullable(tasks.get(id));
  }

  public List<Task> getAll() {
    return new java.util.ArrayList<>(tasks.values());
  }

  public void remove(UUID id) {
    tasks.remove(id);
  }
}
