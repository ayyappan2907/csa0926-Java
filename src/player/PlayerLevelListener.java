package player;

/**
 * Listener notified when the player levels up.
 * Implemented by GUI components that need to react to level changes.
 */
public interface PlayerLevelListener {
    /**
     * Called on the Event Dispatch Thread after a level-up occurs.
     *
     * @param newLevel  the new level (2–10)
     * @param levelName the display name of the new level (e.g. "Engineer")
     */
    void onLevelUp(int newLevel, String levelName);
}
