package multiplayer;

import java.util.List;

/**
 * §4.2 — Full result of one player's match run.
 * Stored locally and (in a future networked version) uploaded to a REST endpoint.
 */
public class PlayerMatchResult {
    public final String          playerId;
    public final String          matchId;
    public final List<GhostFrame> timeline;     // periodic snapshots for ghost playback
    public final double          avgEfficiency;
    public final int             footprintTiles; // machines placed, excludes belts
    public final long            finalCash;
    public final double          score;          // computed by ScoreCalculator

    public PlayerMatchResult(String playerId, String matchId, List<GhostFrame> timeline,
                             double avgEfficiency, int footprintTiles,
                             long finalCash, double score) {
        this.playerId       = playerId;
        this.matchId        = matchId;
        this.timeline       = timeline;
        this.avgEfficiency  = avgEfficiency;
        this.footprintTiles = footprintTiles;
        this.finalCash      = finalCash;
        this.score          = score;
    }
}
