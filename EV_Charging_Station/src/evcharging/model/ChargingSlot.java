package evcharging.model;

/** Represents a charging slot at a station. */
public class ChargingSlot {
    public enum Status {
        AVAILABLE, BOOKED, CHARGING, COMPLETED, OUT_OF_SERVICE
    }

    private int slotId;
    private ChargingStation station;
    private String slotTime;
    private Status status;

    public ChargingSlot(int slotId, ChargingStation station,
                        String slotTime) {
        this.slotId = slotId;
        this.station = station;
        this.slotTime = slotTime;
        this.status = Status.AVAILABLE;
    }

    public int getSlotId() { return slotId; }
    public ChargingStation getStation() { return station; }
    public String getSlotTime() { return slotTime; }
    public Status getStatus() { return status; }

    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    public void book() { status = Status.BOOKED; }
    public void release() { status = Status.AVAILABLE; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "Slot " + slotId + " | " + slotTime + " | " + status;
    }
}