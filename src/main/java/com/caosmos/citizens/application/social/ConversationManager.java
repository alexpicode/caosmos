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

  private long lastUpdatedTick = -1;

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

    // First check if target already has an open session with me.
    // If not, see if I have one with target. If not, create new.

    // Check if target is already in a session
    String targetSessionId = citizenToSession.get(targetId);
    if (targetSessionId != null) {
      ConversationSession targetSession = sessions.get(targetSessionId);
      if (targetSession != null &&
          (targetSession.getPartnerId().equals(citizenId) || targetSession.getInitiatorId().equals(citizenId))) {
        // Target is talking to me, join their session
        citizenToSession.put(citizenId, targetSessionId);
        log.debug("Citizen {} joined existing session {} with {}", citizenId, targetSessionId, targetId);
        return targetSession;
      }
    }

    // Check if I am already in a session with target
    String mySessionId = citizenToSession.get(citizenId);
    if (mySessionId != null) {
      ConversationSession mySession = sessions.get(mySessionId);
      if (mySession != null &&
          (mySession.getPartnerId().equals(targetId) || mySession.getInitiatorId().equals(targetId))) {
        // I am already talking to them
        return mySession;
      } else if (mySession != null) {
        // I was talking to someone else, end that session for me
        endSession(citizenId);
      }
    }

    // Create a new session
    String newSessionId = UUID.randomUUID().toString();
    ConversationSession newSession = new ConversationSession(newSessionId, citizenId, targetId, targetName, tick);
    sessions.put(newSessionId, newSession);
    citizenToSession.put(citizenId, newSessionId);
    // DO NOT force target to join until they actually respond or perceive the message, 
    // to avoid forcing them into a session they might ignore.

    log.debug("Citizen {} started new session {} with {}", citizenId, newSessionId, targetId);
    return newSession;
  }

  public void registerDialogue(String speakerId, String speakerName, String message, long tick) {
    String sessionId = citizenToSession.get(speakerId);
    if (sessionId != null) {
      ConversationSession session = sessions.computeIfPresent(
          sessionId, (k, s) -> {
            s.addDialogue(new DialogueLine(speakerId, speakerName, message, tick));
            return s;
          }
      );
      if (session != null) {
        log.debug("Added dialogue to session {} from {}", sessionId, speakerId);
        // If partner is not yet in the session map (because I initiated and this is my first message),
        // we can safely add them to the map now so they can find it easily when they check.
        String partnerId =
            session.getInitiatorId().equals(speakerId) ? session.getPartnerId() : session.getInitiatorId();
        citizenToSession.putIfAbsent(partnerId, sessionId);
      }
    }
  }

  public synchronized void tickUpdate(long currentTick) {
    if (currentTick <= lastUpdatedTick) {
      return;
    }
    lastUpdatedTick = currentTick;

    // Called once per pulse/tick cycle to advance stale sessions
    sessions.forEach((id, session) -> {
      if (session.getPhase() == ConversationPhase.ACTIVE || session.getPhase() == ConversationPhase.INITIATED) {
        session.incrementTurnsWithoutResponse();
        if (session.getTurnsWithoutResponse() >= 3) {
          session.setPhase(ConversationPhase.STALE);
          log.debug("Session {} became STALE", id);
        }
      } else if (session.getPhase() == ConversationPhase.STALE) {
        session.incrementTurnsWithoutResponse();
        if (session.getTurnsWithoutResponse() >= 4) { // one pulse after stale
          session.setPhase(ConversationPhase.ENDED);
          log.debug("Session {} ENDED due to inactivity", id);
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
        session.setPhase(ConversationPhase.ENDED);
        log.debug("Citizen {} ended session {}", citizenId, sessionId);
      }
    }
  }
}
