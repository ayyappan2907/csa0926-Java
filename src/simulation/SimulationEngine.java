package simulation;

import controller.GameController;
import java.util.concurrent.*;

/**
 * Module 2 — Simulation Engine.
 * Owns the ScheduledExecutorService that fires TickTask at fixed intervals.
 */
public class SimulationEngine {

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> tickFuture;
    private final GameController controller;
    private volatile boolean running;
    private int tickSpeedMs;

    public SimulationEngine(GameController controller) {
        this.controller = controller;
        this.tickSpeedMs = 800;
        this.running = false;
    }

    public void start() {
        if (running) return;
        running = true;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SimulationThread");
            t.setDaemon(true);
            return t;
        });
        scheduleNextTick();
    }

    private void scheduleNextTick() {
        tickFuture = executor.scheduleAtFixedRate(
            new TickTask(controller),
            0, tickSpeedMs, TimeUnit.MILLISECONDS
        );
    }

    public void stop() {
        if (!running) return;
        running = false;
        if (tickFuture != null) tickFuture.cancel(false);
        if (executor != null) {
            executor.shutdown();
            try { executor.awaitTermination(2, TimeUnit.SECONDS); }
            catch (InterruptedException ignored) {}
        }
    }

    /** Changes tick speed; restarts the scheduler if currently running. */
    public void setTickSpeedMs(int ms) {
        this.tickSpeedMs = Math.max(200, ms);
        if (running) { stop(); start(); }
    }

    public boolean isRunning() { return running; }
    public int getTickSpeedMs() { return tickSpeedMs; }
}
