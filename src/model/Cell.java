package model;

import recipe.Resource;
import java.util.ArrayDeque;

public class Cell {
    private MachineType machineType;
    private Direction outputDirection;
    private final ArrayDeque<Resource> buffer;
    private Resource extractorResource;
    private int tickCounter;
    private int processingProgress; // 0..ticksRequired
    private static final int MAX_BUFFER = 6;

    public Cell() {
        this.machineType = MachineType.EMPTY;
        this.outputDirection = Direction.EAST;
        this.buffer = new ArrayDeque<>();
        this.extractorResource = Resource.IRON_ORE;
        this.tickCounter = 0;
        this.processingProgress = 0;
    }

    // --- Thread-safe accessors ---

    public synchronized MachineType getMachineType() { return machineType; }

    public synchronized void setMachineType(MachineType t) {
        this.machineType = t;
        this.buffer.clear();
        this.tickCounter = 0;
        this.processingProgress = 0;
    }

    public synchronized Direction getOutputDirection() { return outputDirection; }
    public synchronized void setOutputDirection(Direction d) { this.outputDirection = d; }

    public synchronized Resource getExtractorResource() { return extractorResource; }
    public synchronized void setExtractorResource(Resource r) { this.extractorResource = r; }

    public synchronized int getTickCounter() { return tickCounter; }
    public synchronized void incrementTickCounter() { tickCounter++; }
    public synchronized void resetTickCounter() { tickCounter = 0; }

    public synchronized int getProcessingProgress() { return processingProgress; }
    public synchronized void setProcessingProgress(int p) { this.processingProgress = p; }

    public synchronized ArrayDeque<Resource> getBufferSnapshot() {
        return new ArrayDeque<>(buffer);
    }

    public synchronized int getBufferSize() { return buffer.size(); }
    public synchronized boolean isBufferFull() { return buffer.size() >= MAX_BUFFER; }
    public synchronized boolean isBufferEmpty() { return buffer.isEmpty(); }
    public static int getMaxBuffer() { return MAX_BUFFER; }

    public synchronized boolean addToBuffer(Resource r) {
        if (buffer.size() < MAX_BUFFER) {
            buffer.addLast(r);
            return true;
        }
        return false;
    }

    public synchronized Resource pollFromBuffer() { return buffer.pollFirst(); }
    public synchronized Resource peekBuffer() { return buffer.peekFirst(); }

    /**
     * Removes the first occurrence of the given resource from the buffer.
     */
    public synchronized boolean removeFromBuffer(Resource r) {
        return buffer.remove(r);
    }
}
