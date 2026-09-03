package evcharging.collection;

import evcharging.exception.InvalidBookingException;
import evcharging.exception.SlotUnavailableException;
import evcharging.model.Booking;
import evcharging.model.ChargingSlot;
import evcharging.model.ChargingStation;
import evcharging.model.Customer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Central in-memory manager for charging stations and slots.
 *
 * COLLECTION FRAMEWORK: A Map is used for fast station/slot lookup
 * and a List view is used for displaying available slots.
 *
 * CONCURRENCY: Shared charging slots are modified only inside
 * synchronized methods so multiple users cannot book the same
 * slot simultaneously.
 */
public class ChargingManager {
    private final Map<Integer, ChargingStation> stations = new HashMap<>();
    private final Map<Integer, ChargingSlot> slots = new HashMap<>();

    public synchronized void addStation(ChargingStation station) {
        stations.put(station.getStationId(), station);
    }

    public synchronized void addSlot(ChargingSlot slot) {
        slots.put(slot.getSlotId(), slot);
    }

    public synchronized ChargingStation getStation(int stationId) {
        return stations.get(stationId);
    }

    public synchronized ChargingSlot getSlot(int slotId) {
        return slots.get(slotId);
    }

    /** Returns a snapshot list of all charging stations. */
    public synchronized List<ChargingStation> listStations() {
        List<ChargingStation> snapshot = new ArrayList<>();
        Collection<ChargingStation> values = stations.values();
        Iterator<ChargingStation> it = values.iterator();
        while (it.hasNext()) {
            snapshot.add(it.next());
        }
        return snapshot;
    }

    /** Returns a snapshot list of available charging slots. */
    public synchronized List<ChargingSlot> listAvailableSlots() {
        List<ChargingSlot> available = new ArrayList<>();
        for (ChargingSlot slot : slots.values()) {
            if (slot.isAvailable()) {
                available.add(slot);
            }
        }
        return available;
    }

    /**
     * Atomically checks and books a charging slot.
     * Synchronized to prevent double booking.
     */
    public synchronized Booking bookSlot(
            int bookingId,
            int stationId,
            int slotId,
            Customer customer,
            evcharging.model.Vehicle vehicle,
            java.time.LocalDate bookingDate)
            throws InvalidBookingException, SlotUnavailableException {
        ChargingStation station = stations.get(stationId);
        ChargingSlot slot = slots.get(slotId);
        if (station == null || slot == null) {
            throw new InvalidBookingException(
                "Invalid charging station or slot."
            );
        }
        if (!slot.isAvailable()) {
            throw new SlotUnavailableException(
                "Charging slot " + slotId + " is not available."
            );
        }
        slot.book();
        return new Booking(
            bookingId, customer, vehicle, station, slot, bookingDate
        );
    }

    /** Releases a slot after cancellation or completion. */
    public synchronized void releaseSlot(int slotId) {
        ChargingSlot slot = slots.get(slotId);
        if (slot != null) {
            slot.release();
            notifyAll();
        }
    }

    /** Updates the status of a charging slot. */
    public synchronized void updateSlotStatus(
            int slotId,
            ChargingSlot.Status status) {
        ChargingSlot slot = slots.get(slotId);
        if (slot != null) {
            slot.setStatus(status);
        }
    }
}