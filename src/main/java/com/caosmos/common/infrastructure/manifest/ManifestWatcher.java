package com.caosmos.common.infrastructure.manifest;

import com.caosmos.common.application.config.CaosmosResourceProperties;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * File system watcher for detecting manifest changes in real-time.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ManifestWatcher {

  private final CaosmosResourceProperties resourceProperties;

  private WatchService watchService;
  private volatile boolean watching = false;
  private Consumer<String> changeListener;

  /**
   * Starts watching for file changes if the watch path exists and is accessible.
   *
   * @param changeListener Callback to handle file changes (receives manifest name)
   */
  public void startWatching(Consumer<String> changeListener) {
    if (watching) {
      log.warn("[WATCHER] Already watching for changes");
      return;
    }

    try {
      Path watchPath = resourceProperties.citizens().getFile().toPath().toAbsolutePath().normalize();

      if (!Files.exists(watchPath) || !Files.isDirectory(watchPath)) {
        log.info("[WATCHER] Watch path not found or not a directory: {}, hot-reload disabled", watchPath);
        return;
      }

      log.info("[WATCHER] Watching path: {}", watchPath);

      this.watchService = FileSystems.getDefault().newWatchService();
      watchPath.register(
          watchService,
          StandardWatchEventKinds.ENTRY_CREATE,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_DELETE
      );

      this.changeListener = changeListener;
      watching = true;

      Thread.ofVirtual()
          .name("manifest-watcher")
          .start(this::watchLoop);

      log.info("[WATCHER] Started watching for manifest changes");
    } catch (IOException e) {
      log.error("[WATCHER] Error initializing watch service: {}", e.getMessage());
    }
  }

  private void watchLoop() {
    log.info("[WATCHER] Watch loop started (blocking on take)...");
    while (watching) {
      try {
        WatchKey key = watchService.take();
        for (WatchEvent<?> event : key.pollEvents()) {
          log.info("[WATCHER] Event detected: {} on {}", event.kind(), event.context());
          handleWatchEvent(event);
        }
        key.reset();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        log.error("[WATCHER] Error in watch loop: {}", e.getMessage());
      }
    }
  }

  /**
   * Stops watching for file changes.
   */
  public void stopWatching() {
    watching = false;

    try {
      if (watchService != null) {
        watchService.close();
        log.debug("[WATCHER] Stopped watching for manifest changes");
      }
    } catch (IOException e) {
      log.error("[WATCHER] Error stopping watch service: {}", e.getMessage());
    }
  }

  /**
   * Main watch loop that processes file system events.
   */
  private void handleWatchEvent(WatchEvent<?> event) {
    Path changedFile = (Path) event.context();

    if (changedFile.toString().endsWith(".md")) {
      String manifestName = changedFile.getFileName().toString();
      log.info(
          "[WATCHER] Detected change for manifest: {} ({})",
          manifestName, event.kind()
      );

      if (changeListener != null) {
        try {
          changeListener.accept(manifestName);
        } catch (Exception e) {
          log.error(
              "[WATCHER] Error in change listener for {}: {}",
              manifestName, e.getMessage()
          );
        }
      }
    }
  }
}
