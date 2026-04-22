package com.caosmos.citizens.application.social;

import com.caosmos.citizens.domain.model.social.ConversationPhase;
import com.caosmos.citizens.domain.model.social.ConversationSession;
import com.caosmos.citizens.domain.model.social.DialogueLine;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConversationManager {

  // Map of citizenId to their active sessionId
  private final ConcurrentHashMap<String, String> citizenToSession = new ConcurrentHashMap<>();
  // Map of sessionId to the actual ConversationSession
  private final ConcurrentHashMap<String, ConversationSession> sessions = new ConcurrentHashMap<>();

  private final ConversationConfigProperties configProperties;

  private long lastUpdatedTick = -1;

  public ConversationManager(ConversationConfigProperties configProperties) {
    this.configProperties = configProperties;
  }

  public ConversationSession initiateOrJoin(
      String citizenId,
      String citizenName,
      String targetId,
      String targetName,
      long tick
  ) {
    if (targetId == null) {
      return null;
    }

    int maxParticipants = configProperties.maxConversationParticipants();

    // Check if target has an active session
    String targetSessionId = citizenToSession.get(targetId);
    if (targetSessionId != null) {
      ConversationSession targetSession = sessions.get(targetSessionId);
      if (targetSession != null) {
        // Check if I'm already in this session
        if (targetSession.isParticipant(citizenId)) {
          return targetSession;
        }
        // Try to join it
        if (targetSession.addParticipant(citizenId, citizenName, maxParticipants)) {
          citizenToSession.put(citizenId, targetSessionId);
          log.debug("Citizen {} joined existing session {} with {}", citizenId, targetSessionId, targetId);
          return targetSession;
        }
        // If we reach here, session is full. We can't join.
      }
    }

    // Check if I have an active session
    String mySessionId = citizenToSession.get(citizenId);
    if (mySessionId != null) {
      ConversationSession mySession = sessions.get(mySessionId);
      if (mySession != null) {
        // If I have my own session, we try to add target
        if (mySession.isParticipant(targetId)) {
          return mySession;
        }
        // If I was talking to others, we can optionally just try to add new target if there's room
      }
    }

    // Create a new session with me and target
    String newSessionId = UUID.randomUUID().toString();
    ConversationSession newSession = new ConversationSession(newSessionId, citizenId, citizenName, tick);
    // Add target as well initially so it's a 2-person session
    newSession.addParticipant(targetId, targetName, maxParticipants);
    sessions.put(newSessionId, newSession);
    citizenToSession.put(citizenId, newSessionId);
    // DO NOT force target to join until they actually respond or perceive the message, 
    // to avoid forcing them into a session they might ignore.

    log.debug("Citizen {} started new session {} with {}", citizenId, newSessionId, targetId);
    return newSession;
  }

  public void registerDialogue(
      String speakerId,
      String speakerName,
      String targetId,
      String message,
      String tone,
      long tick
  ) {
    String sessionId = citizenToSession.get(speakerId);
    if (sessionId != null) {
      ConversationSession session = sessions.computeIfPresent(
          sessionId, (k, s) -> {
            s.addDialogue(new DialogueLine(speakerId, speakerName, message, tone, targetId, tick));
            return s;
          }
      );
      if (session != null) {
        log.debug("Added dialogue to session {} from {}", sessionId, speakerId);
        // Ensure all participants are linked
        session.getActiveParticipantIds().forEach(pid ->
            citizenToSession.putIfAbsent(pid, sessionId));
      }
    }
  }

  public synchronized void tickUpdate(long currentTick) {
    if (currentTick <= lastUpdatedTick) {
      return;
    }
    lastUpdatedTick = currentTick;

    // Called once per global tick to advance stale sessions
    sessions.forEach((id, session) -> {
      long idleTicks = currentTick - session.getLastActivityTick();

      // Always increment the counter so the AI prompt receives meaningful data
      session.incrementTurnsWithoutResponse();

      if (session.getPhase() == ConversationPhase.ACTIVE || session.getPhase() == ConversationPhase.INITIATED) {
        if (idleTicks >= 30) { // 30 seconds of inactivity
          session.setPhase(ConversationPhase.STALE);
          log.debug("Session {} became STALE after {} idle ticks", id, idleTicks);
        }
      } else if (session.getPhase() == ConversationPhase.STALE) {
        if (idleTicks >= 60) { // 60 seconds total inactivity
          session.setPhase(ConversationPhase.ENDED);
          log.debug("Session {} ENDED due to prolonged inactivity ({} ticks)", id, idleTicks);
        }
      }
    });

    // Clean up ended sessions
    sessions.entrySet().removeIf(entry -> {
      if (entry.getValue().getPhase() == ConversationPhase.ENDED) {
        // Remove from mapping
        citizenToSession.values().removeIf(val -> val.equals(entry.getKey()));
        return true;
      }
      return false;
    });
  }

  public Optional<ConversationSession> getActiveSession(String citizenId) {
    String sessionId = citizenToSession.get(citizenId);
    if (sessionId == null) {
      return Optional.empty();
    }

    ConversationSession session = sessions.get(sessionId);
    if (session == null || session.getPhase() == ConversationPhase.ENDED) {
      citizenToSession.remove(citizenId);
      return Optional.empty();
    }
    return Optional.of(session);
  }

  public void endSession(String citizenId) {
    String sessionId = citizenToSession.remove(citizenId);
    if (sessionId != null) {
      ConversationSession session = sessions.get(sessionId);
      if (session != null) {
        int remaining = session.removeParticipant(citizenId);
        if (remaining <= 1) {
          // Only 1 remains, close for all
          session.setPhase(ConversationPhase.ENDED);
          // Clean up the last participant from the map
          session.getActiveParticipantIds().forEach(citizenToSession::remove);
          log.debug("Citizen {} ended session {}, session is now closed for all", citizenId, sessionId);
        } else {
          log.debug("Citizen {} left session {}, {} participants remaining", citizenId, sessionId, remaining);
        }
      }
    }
  }
}
