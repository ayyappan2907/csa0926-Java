package simulation;

import controller.GameController;

/**
 * Module 2 — Tick Runnable.
 * Submitted to the ScheduledExecutorService each tick period.
 */
public class TickTask implements Runnable {

    private final GameController controller;

    public TickTask(GameController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            controller.processTick();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
