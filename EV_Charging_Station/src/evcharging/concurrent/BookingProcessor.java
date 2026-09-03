package evcharging.concurrent;

import evcharging.collection.ChargingManager;
import evcharging.exception.InvalidBookingException;
import evcharging.exception.SlotUnavailableException;
import evcharging.model.*;
import java.time.LocalDate;

/**
 * Runnable task for processing a charging slot booking.
 * Each customer booking can execute in a separate thread.
 */
public class BookingProcessor implements Runnable {
    private ChargingManager manager;
    private Customer customer;
    private Vehicle vehicle;
    private int stationId;
    private int slotId;
    private int bookingId;

    public BookingProcessor(
            ChargingManager manager,
            Customer customer,
            Vehicle vehicle,
            int stationId,
            int slotId,
            int bookingId) {
        this.manager = manager;
        this.customer = customer;
        this.vehicle = vehicle;
        this.stationId = stationId;
        this.slotId = slotId;
        this.bookingId = bookingId;
    }

    @Override
    public void run() {
        try {
            Booking booking = manager.bookSlot(
                bookingId, stationId, slotId,
                customer, vehicle, LocalDate.now()
            );
            System.out.println(
                "Booking successful: " + booking.getBookingId()
            );
        } catch (InvalidBookingException | SlotUnavailableException e) {
            System.out.println(
                "Booking failed: " + e.getMessage()
            );
        }
    }
}