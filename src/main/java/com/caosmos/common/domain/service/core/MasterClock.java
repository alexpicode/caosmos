package com.caosmos.common.domain.service.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class MasterClock {

  private final AtomicLong currentTick = new AtomicLong(0);
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition nextTickCondition = lock.newCondition();

  // Blocks current thread until next simulation tick
  public void waitForNextTick() throws InterruptedException {
    long targetTick = currentTick.get() + 1;
    lock.lock();
    try {
      while (currentTick.get() < targetTick) {
        nextTickCondition.await();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      lock.unlock();
    }
  }

  public void waitForTicks(int numberOfTicks) throws InterruptedException {
    long targetTick = currentTick.get() + numberOfTicks;
    while (currentTick.get() < targetTick) {
      waitForNextTick();
    }
  }

  // Advances simulation clock by one tick and wakes up all waiting threads
  public void advance() {
    lock.lock();
    try {
      currentTick.incrementAndGet();
      nextTickCondition.signalAll();
    } finally {
      lock.unlock();
    }
  }

  public long getCurrentTick() {
    return currentTick.get();
  }
}
