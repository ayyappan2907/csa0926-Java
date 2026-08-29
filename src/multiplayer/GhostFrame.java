package multiplayer;

/**
 * A sampled snapshot of a player's state at a given tick.
 * Sampled every ~10 ticks (NOT every tick) to keep replay data small.
 */
public record GhostFrame(int tick, long cash, int itemsProduced, double efficiencyPct) {}
