package player;

import model.MachineType;
import recipe.Resource;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the player's XP, current level, and notifies registered listeners
 * whenever a level-up event occurs.
 *
 * XP is earned by producing items (awarded from GameController's production callback).
 * Level thresholds and unlock tables are defined in LevelConfig.
 */
public class PlayerProfile {

    // ── XP per item produced ──────────────────────────────────────────────────
    public static final int XP_PER_ITEM_BASIC     = 5;   // raw ore, coal
    public static final int XP_PER_ITEM_PROCESSED = 10;  // iron plate, gear
    public static final int XP_PER_ITEM_ADVANCED  = 20;  // steel, circuit

    private int totalXP;
    private int currentLevel;
    private final Wallet wallet = new Wallet(500); // give 500 starting balance

    // Level-9 speed bonus: applied once when reaching level 9
    private boolean speedBonusApplied = false;

    // XP accumulated since the GUI last drained it — drives the "+XP" popup
    private int xpSincePopupDrain = 0;

    private final List<PlayerLevelListener> listeners = new ArrayList<>();

    public PlayerProfile() {
        this.totalXP      = 0;
        this.currentLevel = 1;
    }

    public Wallet getWallet() {
        return wallet;
    }

    // ── XP / Level ────────────────────────────────────────────────────────────

    /**
     * Awards XP for producing the given resource and checks for level-ups.
     * Fires listener callbacks on the EDT if a level-up occurred.
     *
     * @param resource the resource that was just produced
     */
    public synchronized void awardXP(Resource resource) {
        int amount = xpForResource(resource);
        totalXP += amount;
        xpSincePopupDrain += amount;
        checkLevelUp();
    }

    /** Awards a flat XP amount (used for bonus events). */
    public synchronized void awardXP(int amount) {
        totalXP += amount;
        xpSincePopupDrain += amount;
        checkLevelUp();
    }

    /**
     * Returns the XP gained since the last call, resetting the counter to 0.
     * The GUI tick callback calls this once per frame to decide whether to
     * show a floating "+N XP" popup — aggregating however many production
     * events landed within that tick instead of spamming one popup each.
     */
    public synchronized int drainPendingXPForPopup() {
        int v = xpSincePopupDrain;
        xpSincePopupDrain = 0;
        return v;
    }

    private void checkLevelUp() {
        while (currentLevel < LevelConfig.MAX_LEVEL
               && totalXP >= LevelConfig.xpForLevel(currentLevel + 1)) {
            currentLevel++;
            final int lvl  = currentLevel;
            final String name = LevelConfig.getEntry(lvl).title;
            SwingUtilities.invokeLater(() -> listeners.forEach(l -> l.onLevelUp(lvl, name)));
        }
    }

    private int xpForResource(Resource resource) {
        switch (resource) {
            case STEEL:
            case CIRCUIT:
                return XP_PER_ITEM_ADVANCED;
            case IRON_PLATE:
            case GEAR:
                return XP_PER_ITEM_PROCESSED;
            default:
                return XP_PER_ITEM_BASIC;
        }
    }

    // ── Unlock checks ─────────────────────────────────────────────────────────

    /** Returns true if the given machine type is available at the current level. */
    public boolean isUnlocked(MachineType type) {
        return LevelConfig.getEntry(currentLevel).unlockedMachines.contains(type);
    }

    /** Returns true if the given resource is extractable at the current level. */
    public boolean isResourceUnlocked(Resource resource) {
        return LevelConfig.getEntry(currentLevel).unlockedResources.contains(resource);
    }

    /**
     * Returns true if the Pattern Library should be visible (Level 7+).
     */
    public boolean isPatternsUnlocked() {
        return currentLevel >= 7;
    }

    /**
     * Returns true if the level-9 speed bonus should be applied.
     * Calling this marks the bonus as consumed so it is applied exactly once.
     */
    public synchronized boolean consumeSpeedBonus() {
        if (currentLevel >= 9 && !speedBonusApplied) {
            speedBonusApplied = true;
            return true;
        }
        return false;
    }

    // ── Listener management ───────────────────────────────────────────────────

    public void addLevelListener(PlayerLevelListener listener) {
        listeners.add(listener);
    }

    public void removeLevelListener(PlayerLevelListener listener) {
        listeners.remove(listener);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public synchronized int getTotalXP()      { return totalXP; }
    public synchronized int getCurrentLevel() { return currentLevel; }

    /** XP needed to reach the next level (0 if already at max). */
    public synchronized int getXPToNextLevel() {
        if (currentLevel >= LevelConfig.MAX_LEVEL) return 0;
        return LevelConfig.xpForLevel(currentLevel + 1) - totalXP;
    }

    /** XP accumulated within the current level (for progress bar). */
    public synchronized int getXPInCurrentLevel() {
        int base = LevelConfig.xpForLevel(currentLevel);
        return totalXP - base;
    }

    /** XP span of the current level (next threshold - current threshold). */
    public synchronized int getXPSpanOfCurrentLevel() {
        if (currentLevel >= LevelConfig.MAX_LEVEL) return 1;
        return LevelConfig.xpForLevel(currentLevel + 1) - LevelConfig.xpForLevel(currentLevel);
    }

    public String getLevelTitle() {
        return LevelConfig.getEntry(currentLevel).title;
    }

    public String getLevelDescription() {
        return LevelConfig.getEntry(currentLevel).description;
    }
}
