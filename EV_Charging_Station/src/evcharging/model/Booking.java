package evcharging.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Represents a charging slot booking. */
public class Booking {
    public enum Status {
        BOOKED, CANCELLED, COMPLETED
    }

    private int bookingId;
    private Customer customer;
    private Vehicle vehicle;
    private ChargingStation station;
    private ChargingSlot slot;
    private LocalDate bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;

    public Booking(int bookingId, Customer customer,
                   Vehicle vehicle, ChargingStation station,
                   ChargingSlot slot, LocalDate bookingDate) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.vehicle = vehicle;
        this.station = station;
        this.slot = slot;
        this.bookingDate = bookingDate;
        this.status = Status.BOOKED;
    }

    public int getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Vehicle getVehicle() { return vehicle; }
    public ChargingStation getStation() { return station; }
    public ChargingSlot getSlot() { return slot; }
    public LocalDate getBookingDate() { return bookingDate; }
    public Status getStatus() { return status; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }

    public void cancel() {
        status = Status.CANCELLED;
        slot.release();
    }

    public void complete() {
        status = Status.COMPLETED;
    }
}