package multiplayer;

import java.time.Duration;
import java.util.List;

/**
 * §4.2 — Describes a competitive match session.
 * Identical mapSeed = identical starting resource layout for all participants.
 */
public class MatchSession {
    public final String       matchId;
    public final String       mapSeed;
    public final Duration     timeLimit;
    public final List<String> participantIds;

    public MatchSession(String matchId, String mapSeed, Duration timeLimit,
                        List<String> participantIds) {
        this.matchId        = matchId;
        this.mapSeed        = mapSeed;
        this.timeLimit      = timeLimit;
        this.participantIds = participantIds;
    }
}
